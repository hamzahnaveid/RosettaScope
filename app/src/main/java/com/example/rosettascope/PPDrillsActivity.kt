package com.example.rosettascope

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.adapters.ChallengeDrillsRecyclerViewAdapter
import com.example.rosettascope.models.Score
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.GradingViewModel
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import kotlin.io.path.createTempFile

class PPDrillsActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    var painPoints = listOf<Score>()
    var painPointsAudioBase64 = mutableMapOf<String, String>()
    private val gradingViewModel: GradingViewModel by viewModels()
    private var user: User? = null

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

    fun retrievePainPoints() {
        val email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "")

        val gson = Gson()
        val queue = Volley.newRequestQueue(this)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/pain-points/$email"

        val getPainPointsRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                val json = response.toString()
                painPoints = gson.fromJson(json, Array<Score>::class.java).toList()
                Log.d("JavaDB", painPoints.toString())
                populateAudioBase64List()
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

    fun populateAudioBase64List() {
        val queue = Volley.newRequestQueue(this)

        for (painPoint in painPoints) {
            val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/get-audio-base64/${painPoint.word}/${painPoint.language}"
            val getAudioBase64Request = StringRequest(
                Request.Method.GET, url,
                { response ->
                    painPointsAudioBase64.put(painPoint.word, response)
                    Log.d("JavaDB", painPointsAudioBase64.toString())
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
            queue.add(getAudioBase64Request)
        }
    }

    fun retrieveUser() {
        val email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "")

        val gson = Gson()
        val queue = Volley.newRequestQueue(this)
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
                    this,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            })
        queue.add(getUserRequest)
    }

    fun setupRecyclerView() {
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
            })
        rvChallengeDrill.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
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
//            showGradeDialog(response.result, response.feedback, response.new_confidence_mastered)
        }

        gradingViewModel.errorMessage.observe(this) { error ->
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    fun toResultScreen() {
//        saveUserProgress()
        val intent = Intent(this@PPDrillsActivity, HomeActivity::class.java)
        startActivity(intent)
    }
}