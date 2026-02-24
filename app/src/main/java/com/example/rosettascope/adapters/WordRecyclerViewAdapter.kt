package com.example.rosettascope.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.R

class WordRecyclerViewAdapter(
    private val wordBank: List<String>,
    private val discoveredWords: Map<String, Double>,
    private val targetLanguage: String
) : RecyclerView.Adapter<WordRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_word_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wordBank[position]

        if (!discoveredWords.contains(item)) {
            holder.imgView.setImageResource(R.drawable.undiscovered)
            holder.tvWord.text = "???"
            holder.tvEngTranslation.text = item
            holder.progressBar.visibility = View.GONE
            holder.tvMasteryLevel.visibility = View.GONE
        }
        else {
            holder.imgView.setImageResource(R.drawable.discovered)
            setTextViewToTranslation(item, holder.tvWord, holder.itemView.context)
            holder.tvEngTranslation.text = item
            holder.progressBar.visibility = View.VISIBLE
            holder.tvMasteryLevel.visibility = View.VISIBLE

            when (discoveredWords.get(item)?.times(100)?.toInt()) {
                in 0..24 -> {
                    holder.progressBar.setProgress((discoveredWords.get(item)!!/0.25).times(100).toInt(), true)
                    holder.tvMasteryLevel.text = "Beginner"
                }

                in 25..76 -> {
                    holder.progressBar.setProgress((discoveredWords.get(item)!!/0.77).times(100).toInt(), true)
                    holder.tvMasteryLevel.text = "Intermediate"
                }

                in 77..94 -> {
                    holder.progressBar.setProgress((discoveredWords.get(item)!!/0.95).times(100).toInt(), true)
                    holder.tvMasteryLevel.text = "Advanced"
                }

                in 95..100 -> {
                    holder.imgView.setImageResource(R.drawable.mastered)
                    holder.progressBar.setProgress(100, true)
                    holder.tvMasteryLevel.text = "Mastered"
                }
            }
        }
    }

    override fun getItemCount(): Int = wordBank.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgView: ImageView = itemView.findViewById(R.id.imageView_word_status)
        val tvWord: TextView = itemView.findViewById(R.id.textView_word)
        val tvEngTranslation: TextView = itemView.findViewById(R.id.textView_translation)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvMasteryLevel: TextView = itemView.findViewById(R.id.textView_mastery_level)
    }

    fun setTextViewToTranslation(word: String, textView: TextView, context: Context) {
        val queue = Volley.newRequestQueue(context)
        val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/translate-to-target/${word}/${targetLanguage}"

        val translateWordRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                textView.text = response
            },
            { error ->
                Toast.makeText(
                    context,
                    "Error connecting to server",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("VolleyRequest", error.toString())
            })
        queue.add(translateWordRequest)

    }
}