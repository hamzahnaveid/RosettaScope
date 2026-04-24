package com.example.rosettascope

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.adapters.ChallengeDrillsRecyclerViewAdapter
import com.example.rosettascope.adapters.ScoreRecyclerViewAdapter
import com.example.rosettascope.helpers.LockableLinearLayoutManager
import com.example.rosettascope.models.Score
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.GradingViewModel
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.io.path.createTempFile

class PPDrillsActivity : AppCompatActivity() {
    private var currentIndex = 0
    private var attemptsCount = 0
    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    var painPoints = listOf<Score>()
    var painPointsAudioBase64 = mutableMapOf<String, String>()
    private val resultsMap = mutableMapOf<String, Boolean>()
    private val originalMap = mutableMapOf<String, Double>()
    private val finalMap = mutableMapOf<String, Double>()
    private val gradingViewModel: GradingViewModel by viewModels()
    private lateinit var layoutManager: LockableLinearLayoutManager
    private val tvCounter: TextView by lazy { findViewById(R.id.textView_challenge_dcounter) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar_challenge_dprogbar) }
    private var user: User? = null
    private var jsonResult: String? = null
    private var feedback: String? = null
    private val queue by lazy { Volley.newRequestQueue(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ppdrills)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        observeGradingViewModel()
        retrievePainPoints()
        retrieveUser()
    }

    private fun retrievePainPoints() {
        val email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "")

        val gson = Gson()
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/pain-points/$email"

        val getPainPointsRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                val json = response.toString()
                painPoints = gson.fromJson(json, Array<Score>::class.java).toList()
                Log.d("JavaDB", painPoints.toString())

                tvCounter.text = "0/${painPoints.size}"
                progressBar.max = painPoints.size

                prefetchAudio(0)
                setupRecyclerView()
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                val utf8String = String(response.data, Charsets.UTF_8)
                return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response))
            }
        }
        queue.add(getPainPointsRequest)
    }

    private fun prefetchAudio(startIndex: Int, count: Int = 2) {
        for (i in startIndex until (startIndex + count).coerceAtMost(painPoints.size)) {
            val index = i
            if (painPointsAudioBase64.containsKey(painPoints[index].word)) continue

            val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/get-audio-base64/${painPoints[index].word}/${painPoints[index].language}"
            val getAudioBase64Request = StringRequest(
                Request.Method.GET, url,
                { response ->
                    painPointsAudioBase64.put(painPoints[i].word, response)
                    Log.d("PainPointAudio", painPointsAudioBase64.toString())

                    val rv = findViewById<RecyclerView>(R.id.rv_challenge_drill)
                    rv.adapter?.notifyItemChanged(index)
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
            getAudioBase64Request.retryPolicy = DefaultRetryPolicy(
                    300000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            queue.add(getAudioBase64Request)
        }
    }

    private fun retrieveUser() {
        val email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "")

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
        val rvChallengeDrill: RecyclerView = findViewById(R.id.rv_challenge_drill)

        val adapter = ChallengeDrillsRecyclerViewAdapter(painPoints,
            playPronunAudio = { refText ->
                playAudioFromBase64(painPointsAudioBase64[refText]!!)
            },
            recordPronunAudio = { word, engWord, isRecording ->
                if (isRecording) {
                    startRecording()
                } else {
                    stopRecording(word, engWord)
                }
            },
            isAudioReady = { word ->
                painPointsAudioBase64.containsKey(word)
            },
            getResult = { word ->
                resultsMap[word]
            },
            displayFeedbackDialog = {
                showGradeDialog(jsonResult.toString(), feedback.toString())
            })
        rvChallengeDrill.adapter = adapter

        layoutManager = LockableLinearLayoutManager(this)
        layoutManager.isScrollEnabled = false
        rvChallengeDrill.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvChallengeDrill)
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

    private fun getRecordingFilePath(): String {
        val dir = this.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val mp3File = File(dir, "recording.mp3")
        return mp3File.absolutePath
    }

    private fun observeGradingViewModel() {
        gradingViewModel.gradingResult.observe(this) { response ->
            Log.d("GradeResult", response.result)
            Log.d("BKTResult", response.new_confidence_mastered.toString())

            jsonResult = response.result
            feedback = response.feedback

            val word = painPoints[currentIndex].word
            val engWord = painPoints[currentIndex].engWord
            playResultAudio(response.is_correct)

            if (response.is_correct == "True") {
                resultsMap[word] = true
                finalMap[engWord] = response.new_confidence_mastered
                val rv = findViewById<RecyclerView>(R.id.rv_challenge_drill)
                val layoutManager = rv.layoutManager as LockableLinearLayoutManager
                rv.adapter?.notifyItemChanged(currentIndex)

                attemptsCount = 0
                currentIndex++

                prefetchAudio(currentIndex)

                tvCounter.text = "${currentIndex}/${painPoints.size}"
                progressBar.progress = currentIndex

                layoutManager.isScrollEnabled = true
                rv.postDelayed({
                    rv.smoothScrollToPosition(currentIndex)
                }, 3000)

                rv.postDelayed({
                    layoutManager.isScrollEnabled = false
                }, 3300)
            } else {
                resultsMap[word] = false
                // Don't penalize the user for incorrect answers in this activity
                finalMap[engWord] = originalMap[engWord]!!
                val rv = findViewById<RecyclerView>(R.id.rv_challenge_drill)
                val layoutManager = rv.layoutManager as LockableLinearLayoutManager
                rv.adapter?.notifyItemChanged(currentIndex)

                attemptsCount++

                if (attemptsCount >= 3) {
                    Toast.makeText(
                        this,
                        "We'll skip this one for now and come back to it later.",
                        Toast.LENGTH_SHORT
                    ).show()

                    attemptsCount = 0
                    currentIndex++

                    prefetchAudio(currentIndex)

                    tvCounter.text = "${currentIndex}/${painPoints.size}"
                    progressBar.progress = currentIndex

                    layoutManager.isScrollEnabled = true
                    rv.postDelayed({
                        rv.smoothScrollToPosition(currentIndex)
                    }, 3000)

                    rv.postDelayed({
                        layoutManager.isScrollEnabled = false
                    }, 3300)
                }
            }

            if (currentIndex >= painPoints.size) {
                toResultScreen()
            }
        }

        gradingViewModel.errorMessage.observe(this) { error ->
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun playResultAudio(isCorrect: String) {
        var resId: Int = 0

        mediaPlayer?.release()

        if (isCorrect == "True") {
            resId = R.raw.correct
        }
        else {
            resId = R.raw.incorrect
        }
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()
    }

    private fun showGradeDialog(jsonResult: String, feedback: String) {
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

        val dialog = AlertDialog.Builder(this)
            .setTitle("Pronunciation Assessment")
            .setView(view)
            .setNegativeButton("Continue", DialogInterface.OnClickListener() { dialog, _ ->
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

    private fun toResultScreen() {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("new_confidence_scores", finalMap as HashMap<String, Double>)
        intent.putExtra("user_confidence_scores", originalMap as HashMap<String, Double>)
        startActivity(intent)
    }
}