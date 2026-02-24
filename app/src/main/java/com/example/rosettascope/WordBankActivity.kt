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
    private var user: User? = null
    private val wordBank = mutableListOf<String>()
    private var recyclerView: RecyclerView? = null
    private var userRetrieved = false
    private var wordBankRetrieved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_word_bank)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.rv_word_bank)

        val email = applicationContext?.getSharedPreferences("USER", Context.MODE_PRIVATE)
            ?.getString("email", "").toString()

        retrieveUser(email)
        retrieveWordBank()
    }

    private fun retrieveWordBank() {

        val queue = Volley.newRequestQueue(applicationContext)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/get-word-bank"

        val retrieveWordBankRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val array: JSONArray = JSONArray(response)
                for (i in 0 until array.length()) {
                    wordBank.add(array.getString(i))
                }
                Log.d("JavaDB", "Word Bank retrieved")
                wordBankRetrieved = true
                trySetupRecyclerView()
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
    }

    private fun retrieveUser(email: String) {
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
                trySetupRecyclerView()
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

    private fun trySetupRecyclerView() {
        if (userRetrieved && wordBankRetrieved) {
            val adapter = WordRecyclerViewAdapter(wordBank, user!!.confidenceScores, user!!.targetLanguage)
            recyclerView!!.adapter = adapter
        }
    }
}