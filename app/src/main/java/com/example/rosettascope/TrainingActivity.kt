package com.example.rosettascope

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.adapters.ChallengeTrainingRecyclerViewAdapter
import com.example.rosettascope.helpers.LockableLinearLayoutManager

class TrainingActivity : AppCompatActivity() {
    private var trainingWords = hashMapOf<String, Double>()
    private var translationMap = mutableMapOf<String, String>()
    private var feedbackMap = mutableMapOf<String, String>()
    private lateinit var layoutManager: LockableLinearLayoutManager
    private val tvCounter: TextView by lazy { findViewById(R.id.textView_challenge_tcounter) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar_challenge_tprogbar) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_training)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        trainingWords = intent.getSerializableExtra("trainingWords") as HashMap<String, Double>
        Log.d("JavaDB", trainingWords.toString())
        tvCounter.text = "0/${trainingWords.size}"
        progressBar.max = trainingWords.size
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val rvChallengeTraining: RecyclerView = findViewById(R.id.rv_challenge_training)

        val adapter = ChallengeTrainingRecyclerViewAdapter(translationMap, feedbackMap)
        rvChallengeTraining.adapter = adapter

        layoutManager = LockableLinearLayoutManager(this)
        layoutManager.isScrollEnabled = false
        rvChallengeTraining.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvChallengeTraining)
    }
}