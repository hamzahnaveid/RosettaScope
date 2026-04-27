package com.example.rosettascope

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrainerActivity : AppCompatActivity() {
    var trainingWords = hashMapOf<String, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trainer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val ivAuto = findViewById<ImageView>(R.id.imageView_auto)
        val ivManual = findViewById<ImageView>(R.id.imageView_manual)

        ivAuto.setOnClickListener {
            retrieveWords(true.toString())
        }
        ivManual.setOnClickListener {
            retrieveWords(false.toString())
        }
    }

    fun retrieveWords(isAuto: String) {
        val email = getSharedPreferences("USER", MODE_PRIVATE).getString("email", "")

        val queue = Volley.newRequestQueue(this)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/trainer/$email/$isAuto"

        val getTrainingWordsRequest = object :  StringRequest(
            Request.Method.GET, url,
            { response ->
                val json = response.toString()
                trainingWords = Gson().fromJson(json, object : TypeToken<HashMap<String, Double>>() {}.type)
                Log.d("JavaDB", trainingWords.toString())

                if (isAuto == "true") {
                    val intent = Intent(this@TrainerActivity, TrainingActivity::class.java)
                    intent.putExtra("trainingWords", trainingWords)
                    startActivity(intent)
                }
                else {
                    val intent = Intent(this@TrainerActivity, ManualSelectionActivity::class.java)
                    intent.putExtra("trainingWords", trainingWords)
                    startActivity(intent)
                }
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
        queue.add(getTrainingWordsRequest)
    }
}