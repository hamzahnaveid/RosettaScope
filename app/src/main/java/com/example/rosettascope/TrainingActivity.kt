package com.example.rosettascope

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.adapters.ChallengeTrainingRecyclerViewAdapter
import com.example.rosettascope.helpers.LockableLinearLayoutManager
import com.example.rosettascope.models.TrainingItem
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.FeedbackViewModel
import com.example.rosettascope.viewmodels.GradingViewModel
import com.example.rosettascope.viewmodels.TrainingViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.util.LinkedList
import java.util.Queue
import kotlin.getValue
import kotlin.io.path.createTempFile

class TrainingActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var trainingWords = hashMapOf<String, Double>()
    private var translationMap = mutableMapOf<String, String>()
    private var feedbackMap = mutableMapOf<String, String>()
    private var completedExercises = mutableMapOf<String, Boolean>()
    private val originalMap = mutableMapOf<String, Double>()
    private val finalMap = mutableMapOf<String, Double>()
    private lateinit var layoutManager: LockableLinearLayoutManager
    private val gradingViewModel: GradingViewModel by viewModels()
    private val feedbackViewModel: FeedbackViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private val tvCounter: TextView by lazy { findViewById(R.id.textView_challenge_tcounter) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar_challenge_tprogbar) }
    private var user: User? = null
    private val queue by lazy { Volley.newRequestQueue(this) }
    private lateinit var email: String
    private var currentIndex = 0
    private lateinit var trainingWordsList: List<String>
    private val trainingQueue: Queue<TrainingItem> = LinkedList<TrainingItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_training)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "").toString()

        trainingWords = intent.getSerializableExtra("trainingWords") as HashMap<String, Double>
        Log.d("JavaDB", trainingWords.toString())

        trainingWordsList = trainingWords.keys.toList()

        completedExercises.put("speaking", false)
        completedExercises.put("listening", false)
        completedExercises.put("reading", false)

        observeFeedbackViewModel()
        observeTrainingViewModel()
        observeGradingViewModel()

        fetchNextBatchWords()
        retrieveUser()

        tvCounter.text = "${currentIndex}/${trainingWords.size}"
        progressBar.max = trainingWords.size
        setupRecyclerView()
    }

    private fun fetchNextBatchWords() {
        val targetLanguage = getSharedPreferences("USER", Context.MODE_PRIVATE)
            .getString("target_language", "").toString()

        if (currentIndex >= trainingWordsList.size) return

        val wordBatch = trainingWordsList.subList(
            currentIndex,
            minOf(currentIndex + 2, trainingWordsList.size)
        )

        val confidenceBatch = wordBatch.map { trainingWords[it] }

        trainingViewModel.getTraining(wordBatch, targetLanguage, confidenceBatch as List<Double>)
    }

    private fun retrieveUser() {
        val gson = Gson()
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/user/$email"

        val getUserRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val json = response.toString()
                user = gson.fromJson(json, User::class.java)
                originalMap.putAll(user!!.confidenceScores)
                Log.d("JavaDB", "User retrieved")
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            })
        queue.add(getUserRequest)
    }

    private fun setupRecyclerView() {
        val rvChallengeTraining: RecyclerView = findViewById(R.id.rv_challenge_training)

        val adapter = ChallengeTrainingRecyclerViewAdapter(translationMap,
            feedbackMap,
            isDataReady = {
                trainingQueue.isNotEmpty() && feedbackMap.isNotEmpty()
            },
            displayExerciseListDialog = {
               displayExerciseListDialog()
            })
        rvChallengeTraining.adapter = adapter

        layoutManager = LockableLinearLayoutManager(this)
        layoutManager.isScrollEnabled = false
        rvChallengeTraining.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvChallengeTraining)
    }

    private fun displayExerciseListDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_exercises, null)

        val btnSpeaking: Button = view.findViewById(R.id.button_speaking)
        val btnListening: Button = view.findViewById(R.id.button_listening)
        val btnReading: Button = view.findViewById(R.id.button_reading)

        // restore state in case user accidentally taps out of dialog
        if (completedExercises["speaking"] == true) {
         btnSpeaking.isEnabled = false
         btnSpeaking.setBackgroundResource(R.drawable.bg_text4)
        }

        if (completedExercises["listening"] == true) {
            btnListening.isEnabled = false
            btnListening.setBackgroundResource(R.drawable.bg_text4)
        }

        if (completedExercises["reading"] == true) {
            btnReading.isEnabled = false
            btnReading.setBackgroundResource(R.drawable.bg_text4)
        }


        val dialog = AlertDialog.Builder(this)
            .setTitle("Training Areas")
            .setView(view)
            .create()

        dialog.show()

        btnSpeaking.setOnClickListener {
            displaySpeakingDialog { isComplete ->
                if (isComplete) {
                    completedExercises["speaking"] = true
                    btnSpeaking.isEnabled = false
                    btnSpeaking.setBackgroundResource(R.drawable.bg_text4)
                    checkAllExercisesDone(dialog)
                }
            }
        }
        btnListening.setOnClickListener {
            displayListeningDialog { isComplete ->
                if (isComplete) {
                    completedExercises["listening"] = true
                    btnListening.isEnabled = false
                    btnListening.setBackgroundResource(R.drawable.bg_text4)
                    checkAllExercisesDone(dialog)
                }
            }
        }
        btnReading.setOnClickListener {
            displayReadingDialog { isComplete ->
                if (isComplete) {
                    completedExercises["reading"] = true
                    btnReading.isEnabled = false
                    btnReading.setBackgroundResource(R.drawable.bg_text4)
                    checkAllExercisesDone(dialog)
                }
            }
        }
    }

    private fun displaySpeakingDialog(onResult: (Boolean) -> Unit) {
        val currentItem = trainingQueue.peek()

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_speaking, null)

        val tvWord = view.findViewById<TextView>(R.id.textview_translation_speaking)
        val btnPlay = view.findViewById<ImageButton>(R.id.button_play_speaking)
        val btnRecord = view.findViewById<ImageButton>(R.id.button_record_speaking)

        tvWord.text = currentItem.speakingText

        val dialog = AlertDialog.Builder(this)
            .setTitle("Listen and repeat")
            .setView(view)
            .create()

        btnPlay.setOnClickListener {
            playAudioFromBase64(currentItem.speakingAudio!!)
        }

        btnRecord.setOnClickListener {
            startRecording()
            stopRecording(currentItem.speakingText, currentItem.word)
        }

        dialog.show()
    }

    private fun displayListeningDialog(onResult: (Boolean) -> Unit) {
        val currentItem = trainingQueue.peek()

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_listening, null)

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup_listening)
        val btnPlay = view.findViewById<ImageButton>(R.id.button_play_speaking)
        val btnSubmit = view.findViewById<Button>(R.id.button_submit_listening)

        val test: String = "bomba, claart"
        test.split(",")

        val correctWords = currentItem.listeningText.split(" ")
        val fluffWords = currentItem.listeningFluffWords

        val options = (correctWords + fluffWords).shuffled()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select the words you hear")
            .setView(view)
            .create()

        btnPlay.setOnClickListener {
            playAudioFromBase64(currentItem.listeningAudio!!)
        }

        options.forEach { word ->
            val chip = Chip(this)
            chip.text = word
            chip.isCheckable = true

            chip.chipBackgroundColor = ContextCompat.getColorStateList(
                this,
                R.color.chip_selector2
            )

            chip.chipStrokeWidth = 2f
            chip.chipStrokeColor = ContextCompat.getColorStateList(
                this,
                R.color.rosetta_yellow
            )

            chipGroup.addView(chip)
        }

        btnSubmit.setOnClickListener {
            val selected = mutableListOf<String>()

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.isChecked) {
                    selected.add(chip.text.toString())
                }
            }

            val isCorrect = selected.size == correctWords.size && selected.containsAll(correctWords)

            if (isCorrect) {
                onResult(true)
                dialog.dismiss()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun displayReadingDialog(onResult: (Boolean) -> Unit) {
        val currentItem = trainingQueue.peek()

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_reading, null)

        val layoutAnswer = view.findViewById<LinearLayout>(R.id.layoutAnswer_reading)
        val layoutInput = view.findViewById<LinearLayout>(R.id.layoutInput_reading)

        val tvTranslation = view.findViewById<TextView>(R.id.textview_translation_reading)
        val tvEnglish = view.findViewById<TextView>(R.id.textview_originaltext_reading)
        val btnNext = view.findViewById<Button>(R.id.button_next_reading)

        val tvQuestionTranslation = view.findViewById<TextView>(R.id.textview_translation_reading_exercise)
        val etAnswer = view.findViewById<EditText>(R.id.editText_reading_input)
        val btnSubmit = view.findViewById<Button>(R.id.button_submit_reading)

        tvTranslation.text = currentItem.readingText
        tvEnglish.text = currentItem.readingAnswer

        tvQuestionTranslation.text = currentItem.readingText

        val dialog = AlertDialog.Builder(this)
            .setTitle("Read and answer")
            .setView(view)
            .create()

        btnNext.setOnClickListener {
            layoutAnswer.visibility = View.GONE
            layoutInput.visibility = View.VISIBLE
        }

        btnSubmit.setOnClickListener {
            if (etAnswer.text.toString() == currentItem.readingAnswer) {
                onResult(true)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun checkAllExercisesDone(parentDialog: AlertDialog) {
        val allDone = completedExercises.values.all { it }

        if (allDone) {
            parentDialog.dismiss()

            trainingQueue.remove()
            currentIndex++

            layoutManager.isScrollEnabled = true

            val rv: RecyclerView = findViewById(R.id.rv_challenge_training)
            rv.postDelayed({
                rv.smoothScrollToPosition(progressBar.progress)
            }, 1000)
            rv.postDelayed({
                layoutManager.isScrollEnabled = false
            }, 1500)
            completedExercises["speaking"] = false
            completedExercises["listening"] = false
            completedExercises["reading"] = false

            if (trainingQueue.size < 2) {
                fetchNextBatchWords()
            }

            progressBar.progress = currentIndex
            tvCounter.text = "${currentIndex}/${trainingWords.size}"

            if (progressBar.progress >= trainingWords.size) {
                toResultScreen()
            }
        }
    }

    private fun retrieveFeedback(word: String) {
        val feedbackList = mutableListOf<String>()

        for (i in 0 until user!!.scores.size) {
            val score = user!!.scores[i]
            if (score.engWord == word) {
                feedbackList.add(score.feedback)
            }
        }

        val jsonArray = JSONArray(feedbackList)
        val feedbackJsonArray = jsonArray.toString()

        feedbackViewModel.getFeedback(feedbackJsonArray)
    }

    private fun observeFeedbackViewModel() {
        feedbackViewModel.feedbackResult.observe(this) { response ->
            Log.d("OllamaFeedback", response.feedback)
            feedbackMap.put(trainingWordsList[currentIndex], response.feedback)
            findViewById<RecyclerView>(R.id.rv_challenge_training).adapter?.notifyDataSetChanged()
        }

        feedbackViewModel.errorMessage.observe(this) { error ->
            android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun observeTrainingViewModel() {
        trainingViewModel.trainingResult.observe(this) { response ->
            Log.d("TrainingResult", response.results.toString())
            response.results.forEach {
                trainingQueue.add(
                    TrainingItem(
                        it.word,
                        it.translation,
                        it.speaking_text,
                        it.speaking_pronunciation_audio_base64,
                        it.listening_text,
                        it.listening_fluff_words,
                        it.listening_pronunciation_audio_base64,
                        it.reading_text,
                        it.reading_answer
                    )
                )
            }
            updateRecyclerView()
        }
    }

    private fun observeGradingViewModel() {
        gradingViewModel.gradingResult.observe(this) { response ->
            Log.d("GradeResult", response.result)
            Log.d("BKTResult", response.new_confidence_mastered.toString())
        }

        gradingViewModel.errorMessage.observe(this) { error ->
            android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun updateRecyclerView() {
        val currentItem = trainingQueue.peek()

        translationMap.clear()
        translationMap[currentItem.word] = currentItem.translation
        findViewById<RecyclerView>(R.id.rv_challenge_training).adapter?.notifyDataSetChanged()

        retrieveFeedback(currentItem.word)
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

    private fun getRecordingFilePath(): String {
        val dir = this.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val mp3File = File(dir, "recording.mp3")
        return mp3File.absolutePath
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

    private fun stopRecording(refText: String, engWord: String) {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        val dir = this.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(dir, "recording.mp3")
        val bytes = file.readBytes()
        Log.d("MediaRecorder", "Recording saved to ${file.absolutePath}")
        val bytesString = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

        gradingViewModel.gradeSpeech(refText,
            user?.targetLanguage.toString(),
            bytesString,
            user?.confidenceScores[engWord]!!
        )
    }

    private fun playResultAudio(isCorrect: Boolean) {
        var resId: Int = 0

        mediaPlayer?.release()

        if (isCorrect) {
            resId = R.raw.correct
        }
        else {
            resId = R.raw.incorrect
        }
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()
    }

    private fun toResultScreen() {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("new_confidence_scores", finalMap as HashMap<String, Double>)
        intent.putExtra("user_confidence_scores", originalMap as HashMap<String, Double>)
        startActivity(intent)
    }
}