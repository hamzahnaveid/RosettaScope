package com.example.rosettascope

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.epoxy.EpoxyRecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.epoxy.WordEpoxyController
import com.example.rosettascope.helpers.WordFilter
import com.example.rosettascope.helpers.getFilter
import com.example.rosettascope.models.User
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import org.json.JSONArray

class WordBankActivity : AppCompatActivity() {
    private var user: User? = null
    private val wordBank = mutableListOf<String>()
    private var epoxyRecyclerView: EpoxyRecyclerView? = null
    private var userRetrieved = false
    private var wordBankRetrieved = false
    private var selectedFilter = WordFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_word_bank)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        epoxyRecyclerView = findViewById(R.id.rv_word_bank)

        val cgFilters = findViewById<ChipGroup>(R.id.filterGroup)

        cgFilters.setOnCheckedChangeListener { _, checkedId ->
            val chip = findViewById<Chip>(checkedId)

            selectedFilter = when (chip.text) {
                "Undiscovered" -> WordFilter.UNDISCOVERED
                "Beginner" -> WordFilter.BEGINNER
                "Intermediate" -> WordFilter.INTERMEDIATE
                "Advanced" -> WordFilter.ADVANCED
                "Discovered" -> WordFilter.DISCOVERED
                "Mastered" -> WordFilter.MASTERED
                else -> WordFilter.ALL
            }

            trySetupRecyclerView()
        }

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

            val filteredWords = wordBank.filter { word ->
                when (selectedFilter) {
                    WordFilter.ALL -> true

                    WordFilter.UNDISCOVERED ->
                        !user!!.confidenceScores.containsKey(word + "/" + user!!.targetLanguage)

                    WordFilter.DISCOVERED ->
                        user!!.confidenceScores.containsKey(word + "/" + user!!.targetLanguage)

                    else ->
                        getFilter(word + "/" + user!!.targetLanguage, user!!.confidenceScores) == selectedFilter
                }
            }

            val controller = WordEpoxyController()
            epoxyRecyclerView!!.setController(controller)
            controller.setData(filteredWords, user!!.confidenceScores, user!!.targetLanguage)
        }
    }
}