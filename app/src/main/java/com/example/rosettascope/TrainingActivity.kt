package com.example.rosettascope

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.adapters.ChallengeTrainingRecyclerViewAdapter
import com.example.rosettascope.helpers.LockableLinearLayoutManager
import com.example.rosettascope.models.User
import com.google.gson.Gson

class TrainingActivity : AppCompatActivity() {
    private var trainingWords = hashMapOf<String, Double>()
    private var translationMap = mutableMapOf<String, String>()
    private var feedbackMap = mutableMapOf<String, String>()
    private var completedExercises = mutableMapOf<String, Boolean>()
    private val originalMap = mutableMapOf<String, Double>()
    private val finalMap = mutableMapOf<String, Double>()
    private lateinit var layoutManager: LockableLinearLayoutManager
    private val tvCounter: TextView by lazy { findViewById(R.id.textView_challenge_tcounter) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar_challenge_tprogbar) }
    private var user: User? = null
    private val queue by lazy { Volley.newRequestQueue(this) }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_training)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        retrieveUser()

        trainingWords = intent.getSerializableExtra("trainingWords") as HashMap<String, Double>
        Log.d("JavaDB", trainingWords.toString())

        translationMap.put("1", "")
        translationMap.put("2", "")
        translationMap.put("3", "")

        tvCounter.text = "0/${trainingWords.size}"
        progressBar.max = trainingWords.size

        completedExercises.put("speaking", false)
        completedExercises.put("listening", false)
        completedExercises.put("reading", false)
        setupRecyclerView()
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
        val rvChallengeTraining: RecyclerView = findViewById(R.id.rv_challenge_training)

        val adapter = ChallengeTrainingRecyclerViewAdapter(translationMap,
            feedbackMap,
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
            .setTitle("Select an exercise")
            .setView(view)
            .create()

        dialog.show()

        btnSpeaking.setOnClickListener {
            completedExercises["speaking"] = true
            btnSpeaking.setBackgroundResource(R.drawable.bg_text4)
            checkAllExercisesDone(dialog)
        }
        btnListening.setOnClickListener {
            completedExercises["listening"] = true
            btnListening.setBackgroundResource(R.drawable.bg_text4)
            checkAllExercisesDone(dialog)
        }
        btnReading.setOnClickListener {
            completedExercises["reading"] = true
            btnReading.setBackgroundResource(R.drawable.bg_text4)
            checkAllExercisesDone(dialog)
        }
    }

    private fun checkAllExercisesDone(parentDialog: AlertDialog) {
        val allDone = completedExercises.values.all { it }

        if (allDone) {
            parentDialog.dismiss()

            layoutManager.isScrollEnabled = true

            val rv: RecyclerView = findViewById(R.id.rv_challenge_training)
            rv.post {
                rv.smoothScrollToPosition(progressBar.progress)
            }
            completedExercises["speaking"] = false
            completedExercises["listening"] = false
            completedExercises["reading"] = false

            progressBar.progress += 1
            tvCounter.text = "${progressBar.progress}/${trainingWords.size}"

            if (progressBar.progress >= trainingWords.size) {
                toResultScreen()
            }
        }
    }

    private fun toResultScreen() {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("new_confidence_scores", finalMap as HashMap<String, Double>)
        intent.putExtra("user_confidence_scores", originalMap as HashMap<String, Double>)
        startActivity(intent)
    }
}