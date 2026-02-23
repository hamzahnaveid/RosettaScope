package com.example.rosettascope

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.adapters.WordRecyclerViewAdapter
import com.example.rosettascope.models.User
import com.google.gson.Gson
import org.json.JSONArray

class WordBankActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val email = applicationContext?.getSharedPreferences("USER", Context.MODE_PRIVATE)
            ?.getString("email", "").toString()

        val user = retrieveUser(email)
        val wordBank = retrieveWordBank()

        setContentView(R.layout.activity_word_bank)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = WordRecyclerViewAdapter(wordBank, user!!.confidenceScores)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_word_bank)
        recyclerView.adapter = adapter
    }

    private fun retrieveWordBank() : List<String> {
        val wordBank = mutableListOf<String>()

        val queue = Volley.newRequestQueue(applicationContext)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/get-word-bank"

        val retrieveWordBankRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val array: JSONArray = JSONArray(response)
                for (i in 0 until array.length()) {
                    wordBank.add(array.getString(i))
                }
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
        queue.add(retrieveWordBankRequest)
        return wordBank
    }

    private fun retrieveUser(email: String) : User? {
        var user: User? = null
        val gson = Gson()
        val queue = Volley.newRequestQueue(applicationContext)
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
                    applicationContext,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            })
        queue.add(getUserRequest)
        return user
    }
}