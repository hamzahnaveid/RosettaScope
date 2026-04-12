package com.example.rosettascope

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.epoxy.EpoxyRecyclerView
import com.example.rosettascope.epoxy.TrainingWordEpoxyController
import com.example.rosettascope.epoxy.WordEpoxyController
import com.example.rosettascope.helpers.WordFilter
import com.example.rosettascope.helpers.getFilter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ManualSelectionActivity : AppCompatActivity() {
    private var epoxyRecyclerView: EpoxyRecyclerView? = null
    var discoveredWords = hashMapOf<String, Double>()
    private var selectedFilter = WordFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manual_selection)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        discoveredWords = intent.getSerializableExtra("trainingWords") as HashMap<String, Double>
        Log.d("JavaDB", discoveredWords.toString())

        epoxyRecyclerView = findViewById(R.id.rv_training_words)
        setupRecyclerView()

        val cgFilters = findViewById<ChipGroup>(R.id.ms_filterGroup)
        cgFilters.setOnCheckedChangeListener { _, checkedId ->
            val chip = findViewById<Chip>(checkedId)

            selectedFilter = when (chip.text) {
                "Beginner" -> WordFilter.BEGINNER
                "Intermediate" -> WordFilter.INTERMEDIATE
                "Advanced" -> WordFilter.ADVANCED
                else -> WordFilter.ALL
            }
            setupRecyclerView()
        }
    }

    fun setupRecyclerView() {
        val filteredWords = discoveredWords.keys.filter { word ->
            when (selectedFilter) {
                WordFilter.ALL -> true
                else -> getFilter(word, discoveredWords) == selectedFilter
            }
        }

        val controller = TrainingWordEpoxyController()
        epoxyRecyclerView!!.setController(controller)
        controller.setData(filteredWords, discoveredWords)
    }
}