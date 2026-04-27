package com.example.rosettascope

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.helpers.SparkLineStyle
import com.example.rosettascope.models.Score
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.FeedbackViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import org.json.JSONArray

class WordActivity : AppCompatActivity() {

    var chartStyle = SparkLineStyle(this)

    val feedbackViewModel: FeedbackViewModel by viewModels()

    private var user: User? = null
    private var userRetrieved = false
    private var word: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_word)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val email = applicationContext?.getSharedPreferences("USER", Context.MODE_PRIVATE)
            ?.getString("email", "").toString()
        word = intent.getStringExtra("word")

        val chart = findViewById<LineChart>(R.id.line_chart)

        observeFeedbackViewModel()
        retrieveUserAndPopulateChart(email, chart)
    }

    private fun retrieveUserAndPopulateChart(email: String, chart: LineChart) {
        val gson = Gson()
        val queue = Volley.newRequestQueue(applicationContext)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/user/$email"

        val getUserRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val json = response.toString()
                user = gson.fromJson(json, User::class.java)
                Log.d("JavaDB", "User retrieved")
                userRetrieved = true
                populateChart(chart)

                val tvWord = findViewById<TextView>(R.id.textView_word_display)
                setTextViewToTranslation(word!!, user!!.targetLanguage.toString(), tvWord)

                retrieveFeedback()
            },
            { error ->
                Toast.makeText(
                    applicationContext,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            })
        queue.add(getUserRequest)
    }

    private fun populateChart(chart: LineChart) {
        val entries = mutableListOf<Entry>()
        val scores = user?.scores
        val wordScores = getWordScores(scores!!, word!!)


        for (i in 0 until wordScores!!.size) {
            entries.add(Entry(i.toFloat(), wordScores[i].score.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Scores")
        val lineData = LineData(dataSet)

        chartStyle.styleChart(chart)
        chartStyle.styleLineDataSet(dataSet)

        chart.data = lineData
        chart.invalidate()
        }

    private fun getWordScores(scores: List<Score>, word: String): List<Score> {
        val wordScores = mutableListOf<Score>()

        for (i in 0 until scores.size) {
            if (scores[i].engWord == word) {
                wordScores.add(scores[i])
            }
        }
        return wordScores
    }

    private fun setTextViewToTranslation(word: String, targetLanguage: String, textView: TextView) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/translate-to-target/${word}/${targetLanguage}"

        val translateWordRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                textView.text = response.replace("\"", "")
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
        queue.add(translateWordRequest)
    }

    private fun retrieveFeedback() {
        val feedbackList = mutableListOf<String>()

        for (i in 0 until user!!.scores.size) {
            val score = user!!.scores[i]
            if (score.engWord == word && score.language == user!!.targetLanguage) {
                feedbackList.add(score.feedback)
            }
        }

        val jsonArray = JSONArray(feedbackList)
        val feedbackJsonArray = jsonArray.toString()

        feedbackViewModel.getFeedback(feedbackJsonArray)
    }

    private fun observeFeedbackViewModel() {
        feedbackViewModel.feedbackResult.observe(this) { response ->
            val tvFeedback = findViewById<TextView>(R.id.textView_feedback_display)
            Log.d("OllamaFeedback", response.feedback)
            tvFeedback.text = response.feedback
        }

        feedbackViewModel.errorMessage.observe(this) { error ->
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(error ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

}