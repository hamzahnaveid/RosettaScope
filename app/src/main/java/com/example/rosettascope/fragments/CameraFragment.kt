/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.rosettascope.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.res.Configuration
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.R
import com.example.rosettascope.adapters.SyllableRecyclerViewAdapter
import com.example.rosettascope.ar.OverlayView
import com.example.rosettascope.databinding.FragmentCameraBinding
import com.example.rosettascope.helpers.ObjectDetectorHelper
import com.example.rosettascope.models.Syllable
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.CameraViewModel
import com.example.rosettascope.viewmodels.GradingViewModel
import com.example.rosettascope.viewmodels.TranslationViewModel
import com.google.gson.Gson
import com.google.mediapipe.tasks.vision.core.RunningMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private val TAG = "ObjectDetection"

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private var currentDialog: AlertDialog? = null

    private var currentAudioBase64: String? = null
    private var translatedWord: String = ""

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null

    private lateinit var objectDetectorHelper: ObjectDetectorHelper

    private val viewModel: CameraViewModel by activityViewModels()
    private val translationViewModel: TranslationViewModel by viewModels()
    private val gradingViewModel: GradingViewModel by viewModels()

    private var user: User? = null

    private var preview: Preview? = null

    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(),
                R.id.fragment_container
            )
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }

        backgroundExecutor.execute {
            if (objectDetectorHelper.isClosed()) {
                objectDetectorHelper.setupObjectDetector()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // save ObjectDetector settings
        if(this::objectDetectorHelper.isInitialized) {
            viewModel.setModel(objectDetectorHelper.currentModel)
            viewModel.setDelegate(objectDetectorHelper.currentDelegate)
            viewModel.setThreshold(objectDetectorHelper.threshold)
            viewModel.setMaxResults(objectDetectorHelper.maxResults)
            // Close the object detector and release resources
            backgroundExecutor.execute { objectDetectorHelper.clearObjectDetector() }
        }

    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor.
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE,
            TimeUnit.NANOSECONDS
        )

        mediaPlayer?.release()
        mediaPlayer = null

        mediaRecorder?.release()
        mediaRecorder = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = context?.getSharedPreferences("USER", Context.MODE_PRIVATE)
            ?.getString("email", "").toString()
        retrieveUser(email)

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Create the ObjectDetectionHelper that will handle the inference
        backgroundExecutor.execute {
            objectDetectorHelper =
                ObjectDetectorHelper(
                    context = requireContext(),
                    threshold = viewModel.currentThreshold,
                    currentDelegate = viewModel.currentDelegate,
                    currentModel = viewModel.currentModel,
                    maxResults = viewModel.currentMaxResults,
                    objectDetectorListener = this,
                    runningMode = RunningMode.LIVE_STREAM
                )

            // Wait for the views to be properly laid out
            fragmentCameraBinding.viewFinder.post {
                // Set up the camera and its use cases
                setUpCamera()
            }
        }

        // Attach listeners to UI control widgets
        fragmentCameraBinding.overlay.setRunningMode(RunningMode.LIVE_STREAM)
        fragmentCameraBinding.overlay.setOnBoxTapListener(object : OverlayView.OnBoxTapListener {
            override fun onBoxTapped(word: String) {
                translationViewModel.translateWord(word, user?.targetLanguage.toString())
                showTranslationLoadingDialog(word)
            }
        })

        observeTranslationViewModel()
        observeGradingViewModel()
    }
    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(
                        backgroundExecutor,
                        objectDetectorHelper::detectLivestreamFrame
                    )
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    override fun onResults(resultBundle: ObjectDetectorHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                // Pass necessary information to OverlayView for drawing on the canvas
                val detectionResult = resultBundle.results[0]
                if (detectionResult.detections().isEmpty()) {
                    fragmentCameraBinding.overlay.clear()
                }
                else if (isAdded) {
                    fragmentCameraBinding.overlay.setResults(
                        detectionResult,
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        resultBundle.inputImageRotation
                    )
                }

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeTranslationViewModel() {
        translationViewModel.translationResult.observe(viewLifecycleOwner) { response ->
            hideLoadingDialog()
            currentAudioBase64 = response.pronunciation_audio_base64
            translatedWord = response.translated_word
            val originalText = response.original_text
            showTranslationDialog(translatedWord, originalText)
        }

        translationViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            hideLoadingDialog()
            AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(error ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun observeGradingViewModel() {
        gradingViewModel.gradingResult.observe(viewLifecycleOwner) { response ->
            hideLoadingDialog()
            Log.d("GradeResult", response.result)
            showGradeDialog(response.result, response.feedback)
        }

        gradingViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(error ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showTranslationLoadingDialog(word: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Translating \"$word\"...")
        builder.setCancelable(false)
        currentDialog = builder.create()
        currentDialog?.show()
    }

    private fun showGradeLoadingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Grading pronunciation...")
        builder.setCancelable(false)
        currentDialog = builder.create()
        currentDialog?.show()
    }

    private fun hideLoadingDialog() {
        currentDialog?.dismiss()
        currentDialog = null
    }

    private fun showTranslationDialog(translatedWord: String, originalText: String) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_translation, null)
        val tvTranslated: TextView = view.findViewById(R.id.textview_translation)
        val tvOriginal: TextView = view.findViewById(R.id.textview_originaltext)
        val btnPlay: Button = view.findViewById(R.id.button_play)
        val btnRecord: Button = view.findViewById(R.id.button_record)

        tvTranslated.text = translatedWord
        tvOriginal.text = originalText

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Translation")
            .setView(view)
            .setNegativeButton("Close", null)
            .create()

        btnPlay.setOnClickListener {
            currentAudioBase64?.let { playAudioFromBase64(it) }
        }
        btnRecord.setOnClickListener {
            if (btnRecord.text == "Stop Recording") {
                stopRecording()
                btnRecord.text = "Record Pronunciation"
            }
            else {
                startRecording()
                btnRecord.text = "Stop Recording"
            }
        }

        dialog.show()
    }

    private fun showGradeDialog(jsonResult: String, feedback: String) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_grade, null)

        val tvTranslated: TextView = view.findViewById(R.id.textview_grade_translation)
        val btnPlay: Button = view.findViewById(R.id.button_grade_play)

        val tvFeedback: TextView = view.findViewById(R.id.textview_feedback)

        val pbAccuracy: ProgressBar = view.findViewById(R.id.pb_accuracy)
        val pbFluency: ProgressBar = view.findViewById(R.id.pb_fluency)
        val pbCompleteness: ProgressBar = view.findViewById(R.id.pb_completeness)
        val pbPron: ProgressBar = view.findViewById(R.id.pb_pron)

        val tvAccuracyScore: TextView = view.findViewById(R.id.textview_accuracyscore)
        val tvFluencyScore: TextView = view.findViewById(R.id.textview_fluencyscore)
        val tvCompletenessScore: TextView = view.findViewById(R.id.textview_completenessscore)
        val tvPronScore: TextView = view.findViewById(R.id.textview_pronscore)

        val rvSyllables: RecyclerView = view.findViewById(R.id.rv_syllables)

        val result: JSONObject = JSONObject(jsonResult)
        val nBest: JSONArray = result.getJSONArray("NBest")
        val scores: JSONObject = nBest.getJSONObject(0).getJSONObject("PronunciationAssessment")

        val words: JSONArray = nBest.getJSONObject(0).getJSONArray("Words")
        val syllables: List<Syllable> = populateSyllablesList(words)
        rvSyllables.adapter = SyllableRecyclerViewAdapter(syllables)

        val accuracyScore: Int = scores.getDouble("AccuracyScore").toInt()
        val fluencyScore: Int = scores.getDouble("FluencyScore").toInt()
        val completenessScore: Int = scores.getDouble("CompletenessScore").toInt()
        val pronScore: Int = scores.getDouble("PronScore").toInt()

        tvTranslated.text = result.getString("DisplayText")
        btnPlay.setOnClickListener {
            currentAudioBase64?.let { playAudioFromBase64(it) }
        }

        tvFeedback.text = feedback

        when (accuracyScore) {
            in 0..50 -> {
                pbAccuracy.progressDrawable = resources.getDrawable(R.drawable.custom_progress_red)
            }
            in 51..89 -> {
                pbAccuracy.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_yellow)
            }
            else -> {
                pbAccuracy.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_green)
            }
        }

        when (fluencyScore) {
            in 0..50 -> {
                pbFluency.progressDrawable = resources.getDrawable(R.drawable.custom_progress_red)
            }
            in 51..89 -> {
                pbFluency.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_yellow)
            }
            else -> {
                pbFluency.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_green)
            }
        }

        when (completenessScore) {
            in 0..50 -> {
                pbCompleteness.progressDrawable = resources.getDrawable(R.drawable.custom_progress_red)
            }
            in 51..89 -> {
                pbCompleteness.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_yellow)
            }
            else -> {
                pbCompleteness.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_green)
            }
        }

        when (pronScore) {
            in 0..50 -> {
                pbPron.progressDrawable = resources.getDrawable(R.drawable.custom_progress_red)
            }
            in 51..89 -> {
                pbPron.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_yellow)
            }
            else -> {
                pbPron.progressDrawable =
                    resources.getDrawable(R.drawable.custom_progress_green)
            }
        }

        pbAccuracy.setProgress(accuracyScore.toString().toInt(), true)
        pbFluency.setProgress(fluencyScore.toString().toInt(), true)
        pbCompleteness.setProgress(completenessScore.toString().toInt(), true)
        pbPron.setProgress(pronScore.toString().toInt(), true)

        tvAccuracyScore.text = "Accuracy Score: $accuracyScore"
        tvFluencyScore.text = "Fluency Score: $fluencyScore"
        tvCompletenessScore.text = "Completeness Score: $completenessScore"
        tvPronScore.text = "Overall Score: $pronScore"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Pronunciation Assessment")
            .setView(view)
            .setNegativeButton("Close", DialogInterface.OnClickListener() { dialog, _ ->
                //Save scores to Java DB
                dialog.dismiss()
            })
            .create()
            .show()
    }

    private fun populateSyllablesList(words: JSONArray) : List<Syllable> {
        val syllables = mutableListOf<Syllable>()

        for (i in 0 until words.length()) {
            val word = words.getJSONObject(i)
            val syllablesArray = word.getJSONArray("Syllables")

            for (j in 0 until syllablesArray.length()) {
                val syllable = syllablesArray.getJSONObject(j)
                val grapheme = syllable.getString("Grapheme")
                val accuracyScore =
                    syllable.getJSONObject("PronunciationAssessment").getDouble("AccuracyScore")
                        .toInt()
                syllables.add(Syllable(grapheme, accuracyScore))
            }
        }
        return syllables
    }

    private fun playAudioFromBase64(base64Audio: String) {
        val audioBytes = android.util.Base64.decode(base64Audio, android.util.Base64.DEFAULT)
        val tempFile = createTempFile(suffix = ".mp3").toFile()
        tempFile.writeBytes(audioBytes)

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.apply {
            setDataSource(tempFile.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                tempFile.delete()
            }
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(getRecordingFilePath())
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setMaxDuration(10000)

            try {
                prepare()
                Log.d("MediaRecorder", "Recording started")

            } catch (e: IOException) {
                Log.e("MediaRecorder", "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        val contextWrapper = ContextWrapper(requireContext())
        val dir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(dir, "recording.mp3")
        val bytes = file.readBytes()
        Log.d("MediaRecorder", "Recording saved to ${file.absolutePath}")
        val bytesString = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        gradingViewModel.gradeSpeech(translatedWord, user?.targetLanguage.toString(), bytesString)
        showGradeLoadingDialog()
    }

    //creating mp3 file for demo purposes
    private fun getRecordingFilePath(): String {
        val contextWrapper = ContextWrapper(requireContext())
        val dir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val mp3File = File(dir, "recording.mp3")
        return mp3File.absolutePath
    }

    private fun retrieveUser(email: String) {
        val gson = Gson()
        val queue = Volley.newRequestQueue(context)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/user/$email"

        val getUserRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val json = response.toString()
                user = gson.fromJson(json, User::class.java)
                Log.d("JavaDB", "User retrieved")
            },
            { error ->
                Toast.makeText(
                    context,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            })
        queue.add(getUserRequest)
    }
}