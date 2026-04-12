package com.example.rosettascope.epoxy

import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.rosettascope.R
import com.example.rosettascope.helpers.KotlinHelperModel

class TrainingWordEpoxyModel(
    val word: String,
    val discoveredWords: Map<String, Double>
) : KotlinHelperModel(R.layout.recycler_view_word_item2) {

    val layout by bind<ConstraintLayout>(R.id.ms_word_item_layout)
    val checkBox by bind<CheckBox>(R.id.checkBox_word)
    val tvWord by bind<TextView>(R.id.textView_ms_word)
    val progressBar by bind<ProgressBar>(R.id.progressBar_ms)
    val tvMasteryLevel by bind<TextView>(R.id.textView_mastery_level_ms)

    override fun bind() {
        tvWord.text = word

        layout.setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
        }

        when (discoveredWords.get(word)?.times(100)?.toInt()) {
            in 0..24 -> {
                progressBar.setProgress((discoveredWords.get(word)!!/0.25).times(100).toInt(), true)
                tvMasteryLevel.text = "Beginner"
            }

            in 25..76 -> {
                progressBar.setProgress((discoveredWords.get(word)!!/0.77).times(100).toInt(), true)
                tvMasteryLevel.text = "Intermediate"
            }

            in 77..94 -> {
                progressBar.setProgress((discoveredWords.get(word)!!/0.95).times(100).toInt(), true)
                tvMasteryLevel.text = "Advanced"
            }

            else -> {
                progressBar.setProgress(100, true)
                tvMasteryLevel.text = "Mastered"
            }
        }
    }
}