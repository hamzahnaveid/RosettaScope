package com.example.rosettascope.epoxy

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.rosettascope.R
import com.example.rosettascope.WordActivity
import com.example.rosettascope.helpers.KotlinHelperModel

class WordEpoxyModel(
    val word: String,
    val discoveredWords: Map<String, Double>
) : KotlinHelperModel(R.layout.recycler_view_word_item) {

    val layout by bind<ConstraintLayout>(R.id.word_item_layout)
    val imgView by bind<ImageView>(R.id.imageView_word_status)
    val tvWord by bind<TextView>(R.id.textView_word)
    val progressBar by bind<ProgressBar>(R.id.progressBar)
    val tvMasteryLevel by bind<TextView>(R.id.textView_mastery_level)
    val imgViewArrow by bind<ImageView>(R.id.imageView_arrow_to_word)



    override fun bind() {
        tvWord.text = word

        if (!discoveredWords.contains(word)) {
            imgView.setImageResource(R.drawable.undiscovered)
            progressBar.visibility = View.GONE
            tvMasteryLevel.visibility = View.GONE
            imgViewArrow.visibility = View.GONE

        }
        else {
            layout.setOnClickListener { view ->
                val intent = Intent(view.context, WordActivity::class.java)
                intent.putExtra("word", word)
                view.context.startActivity(intent)
            }

            imgView.setImageResource(R.drawable.discovered)
            progressBar.visibility = View.VISIBLE
            tvMasteryLevel.visibility = View.VISIBLE
            imgViewArrow.visibility = View.VISIBLE

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

                in 95..100 -> {
                    imgView.setImageResource(R.drawable.mastered)
                    progressBar.setProgress(100, true)
                    tvMasteryLevel.text = "Mastered"
                }
            }
        }

    }

}