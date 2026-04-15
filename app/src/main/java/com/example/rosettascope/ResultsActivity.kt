package com.example.rosettascope

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.adapters.ResultsRecyclerViewAdapter
import com.example.rosettascope.helpers.ConfidenceUpdateRequests
import com.google.gson.Gson
import org.json.JSONObject

class ResultsActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_results)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.success_jingle)
        mediaPlayer?.start()

        val resultsMap = intent.getSerializableExtra("new_confidence_scores") as Map<String, Double>
        val userMap = intent.getSerializableExtra("user_confidence_scores") as Map<String, Double>

        val rvResults = findViewById<RecyclerView>(R.id.rv_results)
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvResults.adapter = ResultsRecyclerViewAdapter(resultsMap, userMap)

        val buttonContinueHome = findViewById<Button>(R.id.button_continue_home)
        buttonContinueHome.setOnClickListener {
            saveUserProgress(resultsMap)
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserProgress(map: Map<String, Double>) {
        val email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "")

        val gson = Gson()
        val queue = Volley.newRequestQueue(this)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/update-confidence-scores"

        val requestBody = ConfidenceUpdateRequests(email.toString(), map)

        val saveProgressRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            JSONObject(gson.toJson(requestBody)),
            { response ->
                Log.d("JavaDB", "Confidence updated")
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            }
        )

        queue.add(saveProgressRequest)
    }
}