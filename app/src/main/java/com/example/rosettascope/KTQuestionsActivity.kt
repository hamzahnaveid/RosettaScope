package com.example.rosettascope

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.example.rosettascope.adapters.ChallengeQuestionsRecyclerViewAdapter
import com.example.rosettascope.models.Score
import com.example.rosettascope.models.User
import com.google.gson.Gson

class KTQuestionsActivity : AppCompatActivity() {
    var questionBank = listOf<Score>()
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ktquestions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        retrieveQuestions()
        retrieveUser()
    }

    fun startTimer() {
        val tvTimer: TextView = findViewById(R.id.textView_challenge_timer)
        val timer = object : CountDownTimer(300000, 1000) {
            override fun onFinish() {
                toResultScreen()
            }

            override fun onTick(millisUntilFinished: Long) {
                val minutes = ((millisUntilFinished / 1000) % 3600) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                tvTimer.setText(timeFormatted)
            }

        }.start()
    }

    fun retrieveQuestions() {
        val email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "")

        val gson = Gson()
        val queue = Volley.newRequestQueue(this)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/knowledgeTest/$email"

        val getQuestionBankRequest = object :  StringRequest(
            Request.Method.GET, url,
            { response ->
                val json = response.toString()
                questionBank = gson.fromJson(json, Array<Score>::class.java).toList()
                Log.d("JavaDB", questionBank.toString())
                setupRecyclerView()
                startTimer()
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
        queue.add(getQuestionBankRequest)
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

//    fun saveUserProgress() {
//        val gson = Gson()
//        val queue = Volley.newRequestQueue(this)
//        val url = "https://gaston-distant-unamicably.ngrok-free.dev/save-progress"
//
//        val saveUserRequest = JsonObjectRequest(
//            Request.Method.POST, url, JSONObject(gson.toJson(user)),
//            { response ->
//                Log.d("JavaDB", "Progress saved")
//            },
//            { error ->
//                Toast.makeText(
//                    this,
//                    "Error connecting to server",
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//                Log.e("VolleyRequest", error.toString())
//            })
//        queue.add(saveUserRequest)
//    }

    fun setupRecyclerView() {
        val rvChallengeQuestion: RecyclerView = findViewById(R.id.rv_challenge_question)
        val tvChallengeQuestionCounter: TextView = findViewById(R.id.textView_challenge_qcounter)
        val progressBarChallenge: ProgressBar = findViewById(R.id.progressBar_challenge_qprogbar)

        val adapter = ChallengeQuestionsRecyclerViewAdapter(questionBank,
            tvChallengeQuestionCounter,
            progressBarChallenge,
            onNextClicked = { pos ->
                val nextPos = pos + 1
                if (nextPos < questionBank.size) {
                    rvChallengeQuestion.smoothScrollToPosition(nextPos)
                }
            },
            updateConfidenceScore = { answerData ->
                val queue = Volley.newRequestQueue(this)
                val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/update-bkt-score-challenge/${user!!.confidenceScores[answerData.engWord]}/${answerData.correct}"

                val checkAnswerRequest = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        user!!.confidenceScores[answerData.engWord] = response.toDouble()
                        Log.d("ChallengeRecyclerView", response)
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
            },
            completeChallenge = {
                toResultScreen()
            }
        )
        rvChallengeQuestion.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvChallengeQuestion.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvChallengeQuestion)
    }

    fun toResultScreen() {
//        saveUserProgress()
        val intent = Intent(this@KTQuestionsActivity, HomeActivity::class.java)
        startActivity(intent)
    }
}