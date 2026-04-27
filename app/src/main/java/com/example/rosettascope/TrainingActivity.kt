package com.example.rosettascope

import android.content.Context
import android.content.DialogInterface
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.adapters.ChallengeTrainingRecyclerViewAdapter
import com.example.rosettascope.adapters.ScoreRecyclerViewAdapter
import com.example.rosettascope.helpers.LockableLinearLayoutManager
import com.example.rosettascope.helpers.ScoreRequest
import com.example.rosettascope.models.Score
import com.example.rosettascope.models.TrainingItem
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.FeedbackViewModel
import com.example.rosettascope.viewmodels.GradingViewModel
import com.example.rosettascope.viewmodels.TrainingViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
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
    private val trainingItems = mutableListOf<TrainingItem>()
    private val trainingQueue: Queue<TrainingItem> = LinkedList<TrainingItem>()
    private var loadingDialog: AlertDialog? = null
    private var speakingDialog: AlertDialog? = null
    private lateinit var speakingResultCallback: ((Boolean) -> Unit)
    private val feedbackRequestQueue: Queue<String> = LinkedList()
    private var isFeedbackLoading = false
    private var currentFeedbackWord: String? = null
    private var fetchIndex = 0

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

        retrieveUser()

        tvCounter.text = "${currentIndex}/${trainingWords.size}"
        progressBar.max = trainingWords.size
        setupRecyclerView()
    }

    private fun fetchNextBatchWords() {
        val targetLanguage = getSharedPreferences("USER", Context.MODE_PRIVATE)
            .getString("target_language", "").toString()

        if (fetchIndex >= trainingWordsList.size) return

        val end = minOf(fetchIndex + 2, trainingWordsList.size)
        val wordBatch = trainingWordsList.subList(fetchIndex, end)
        val confidenceBatch = wordBatch.map { trainingWords[it] }

        fetchIndex = end

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
                fetchNextBatchWords()
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

        val adapter = ChallengeTrainingRecyclerViewAdapter(trainingItems,
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
                playResultAudio(isComplete)
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
                playResultAudio(isComplete)
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
                playResultAudio(isComplete)
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

        var recording = false

        speakingResultCallback = onResult

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_speaking, null)

        val tvWord = view.findViewById<TextView>(R.id.textview_translation_speaking)
        val btnPlay = view.findViewById<ImageButton>(R.id.button_play_speaking)
        val btnRecord = view.findViewById<ImageButton>(R.id.button_record_speaking)

        tvWord.text = currentItem.speakingText

        speakingDialog = AlertDialog.Builder(this)
            .setTitle("Listen and repeat")
            .setView(view)
            .create()


        btnPlay.setOnClickListener {
            playAudioFromBase64(currentItem.speakingAudio!!)
        }

        btnRecord.setOnClickListener {
            recording = !recording
            if (!recording) {
                stopRecording(currentItem.speakingText,  currentItem.word)
                btnRecord.setBackgroundResource(R.drawable.bg_text2)
                btnRecord.setImageResource(R.drawable.microphone)
                btnRecord.setPadding(16)
            }
            else {
                startRecording()
                btnRecord.setBackgroundResource(R.drawable.bg_text3)
                btnRecord.setImageResource(R.drawable.recording)
                btnRecord.setPadding(16)

            }
        }

        speakingDialog?.show()
    }

    private fun displayListeningDialog(onResult: (Boolean) -> Unit) {
        val currentItem = trainingQueue.peek()

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_listening, null)

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup_listening)
        val btnPlay = view.findViewById<ImageButton>(R.id.button_play_speaking)
        val btnSubmit = view.findViewById<Button>(R.id.button_submit_listening)

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
            onResult(isCorrect)

            if (isCorrect) {
                updateConfidenceScoreWhenCorrect(currentItem.word)
                dialog.dismiss()
            }
            else {
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    chip.isChecked = false
                }
            }
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
            val isCorrect = etAnswer.text.toString().lowercase() == currentItem.readingAnswer.lowercase()
            onResult(isCorrect)

            if (isCorrect) {
                updateConfidenceScoreWhenCorrect(currentItem.word)
                dialog.dismiss()
            }
            else {
                etAnswer.text.clear()
                layoutAnswer.visibility = View.VISIBLE
                layoutInput.visibility = View.GONE
            }
        }

        dialog.show()
    }

    private fun showGradeLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Grading pronunciation...")
        builder.setView(R.layout.dialog_circular_progress)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun showGradeDialog(jsonResult: String, feedback: String) {
        val currentItem = trainingQueue.peek()

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_grade, null)

        val layoutAudio: LinearLayout = view.findViewById(R.id.layout_word_pronun)
        val layoutMastery: LinearLayout = view.findViewById(R.id.layout_mastery)
        val labelProgress: TextView = view.findViewById(R.id.label_progress)

        layoutAudio.visibility = View.GONE
        layoutMastery.visibility = View.GONE
        labelProgress.visibility = View.GONE

        val tvFeedback: TextView = view.findViewById(R.id.textview_feedback)

        val pbAccuracy: ProgressBar = view.findViewById(R.id.pb_accuracy)
        val pbFluency: ProgressBar = view.findViewById(R.id.pb_fluency)
        val pbCompleteness: ProgressBar = view.findViewById(R.id.pb_completeness)
        val pbPron: ProgressBar = view.findViewById(R.id.pb_pron)

        val tvAccuracyScore: TextView = view.findViewById(R.id.textview_accuracyscore)
        val tvFluencyScore: TextView = view.findViewById(R.id.textview_fluencyscore)
        val tvCompletenessScore: TextView = view.findViewById(R.id.textview_completenessscore)
        val tvPronScore: TextView = view.findViewById(R.id.textview_pronscore)

        val rvSyllables: RecyclerView = view.findViewById(R.id.rv_scores)

        val result: JSONObject = JSONObject(jsonResult)
        val nBest: JSONArray = result.getJSONArray("NBest")
        val scores: JSONObject = nBest.getJSONObject(0).getJSONObject("PronunciationAssessment")

        val words: JSONArray = nBest.getJSONObject(0).getJSONArray("Words")
        val scoreList: List<Score> = populateScoreList(words)
        rvSyllables.adapter = ScoreRecyclerViewAdapter(scoreList)

        val accuracyScore: Int = scores.getDouble("AccuracyScore").toInt()
        val fluencyScore: Int = scores.getDouble("FluencyScore").toInt()
        val completenessScore: Int = scores.getDouble("CompletenessScore").toInt()
        val pronScore: Int = scores.getDouble("PronScore").toInt()

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

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Pronunciation Assessment")
            .setView(view)
            .setNegativeButton("Continue", DialogInterface.OnClickListener() { dialog, _ ->
                saveScoreToDB(currentItem.speakingText, pronScore, feedback)
                dialog.dismiss()
            })
            .create()
            .show()
    }

    private fun populateScoreList(words: JSONArray) : List<Score> {
        val scores = mutableListOf<Score>()

        for (i in 0 until words.length()) {
            val wordObject = words.getJSONObject(i)
            val word = wordObject.getString("Word")
            var score = try {
                wordObject.getJSONObject("PronunciationAssessment").getDouble("AccuracyScore").toInt()
            } catch (e: JSONException) {
                0
            }
            scores.add(
                Score(
                    null,
                    word,
                    user!!.targetLanguage,
                    score,
                    "",
                    System.currentTimeMillis(),
                    ""
                )
            )
        }
        return scores
    }

    private fun checkAllExercisesDone(parentDialog: AlertDialog) {
        val allDone = completedExercises.values.all { it }

        if (allDone) {
            finalMap.put(trainingQueue.peek().word  + "/" + user!!.targetLanguage, user!!.confidenceScores[trainingQueue.peek().word + "/" + user!!.targetLanguage]!!)

            parentDialog.dismiss()

            trainingQueue.remove()
            currentIndex++

            val nextItem = trainingQueue.peek()
            if (nextItem != null) {
                retrieveFeedback(nextItem.word)
            }

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
        feedbackRequestQueue.add(word)
        processNextFeedback()
    }

    private fun processNextFeedback() {
        if (isFeedbackLoading) return
        if (feedbackRequestQueue.isEmpty()) return

        isFeedbackLoading = true

        val word = feedbackRequestQueue.poll()
        currentFeedbackWord = word

        val feedbackList = mutableListOf<String>()

        for (i in 0 until user!!.scores.size) {
            val score = user!!.scores[i]
            if (score.engWord == word) {
                feedbackList.add(score.feedback)
            }
        }

        val jsonArray = JSONArray(feedbackList)
        feedbackViewModel.getFeedback(jsonArray.toString())
    }

    private fun observeFeedbackViewModel() {
        feedbackViewModel.feedbackResult.observe(this) { response ->
            val word = currentFeedbackWord

            feedbackMap[word.toString()] = response.feedback
            findViewById<RecyclerView>(R.id.rv_challenge_training).adapter?.notifyDataSetChanged()

            findViewById<CardView>(R.id.layoutLoading).visibility = View.GONE

            isFeedbackLoading = false
            currentFeedbackWord = null
            processNextFeedback()
            Log.d("OllamaFeedback", response.feedback)
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
                val item = TrainingItem(
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
                trainingQueue.add(item)
                trainingItems.add(item)
            }
            findViewById<RecyclerView>(R.id.rv_challenge_training).adapter?.notifyDataSetChanged()
            response.results.forEach {
                retrieveFeedback(it.word)
            }
        }
    }

    private fun observeGradingViewModel() {
        gradingViewModel.gradingResult.observe(this) { response ->
            hideLoadingDialog()
            val isCorrect = response.is_correct == "True"
            speakingResultCallback?.invoke(isCorrect)
            if (isCorrect) {
                speakingDialog?.dismiss()
                user!!.confidenceScores[trainingQueue.peek().word + "/" + user!!.targetLanguage] = response.new_confidence_mastered
                Log.d("UserConfidenceScore", user!!.confidenceScores[trainingQueue.peek().word + "/" + user!!.targetLanguage].toString())
            }
            showGradeDialog(response.result, response.feedback)
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

    private fun saveScoreToDB(word: String, score: Int, feedback: String) {
        val currentItem = trainingQueue.peek()

        val gson = Gson()
        val queue = Volley.newRequestQueue(this)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/add-score"

        val scoreRequest = ScoreRequest(
            user!!.email,
            word,
            user!!.targetLanguage,
            score,
            currentItem.word,
            System.currentTimeMillis(),
            feedback,
            originalMap[currentItem.word + "/" + user!!.targetLanguage]!!
        )

        val json = JSONObject(gson.toJson(scoreRequest))

        val saveScoreRequest = JsonObjectRequest(
            Request.Method.POST, url, json,
            { response ->
                Log.d("JavaDB", "Score saved")
            }, {
                    error ->
                Toast.makeText(
                    this,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            })
        queue.add(saveScoreRequest)
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
            user?.confidenceScores[engWord + "/" + user!!.targetLanguage]!!
        )
        showGradeLoadingDialog()
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

    private fun updateConfidenceScoreWhenCorrect(word: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/update-bkt-score-challenge/${user!!.confidenceScores[word + "/" + user!!.targetLanguage]}/${true}"

        val checkAnswerRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                user!!.confidenceScores[word + "/" + user!!.targetLanguage] = response.toDouble()
                Log.d("UserConfidenceScore", user!!.confidenceScores[word + "/" + user!!.targetLanguage].toString())
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
        queue.add(checkAnswerRequest)
    }

    private fun toResultScreen() {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("new_confidence_scores", finalMap as HashMap<String, Double>)
        intent.putExtra("user_confidence_scores", originalMap as HashMap<String, Double>)
        startActivity(intent)
    }
}