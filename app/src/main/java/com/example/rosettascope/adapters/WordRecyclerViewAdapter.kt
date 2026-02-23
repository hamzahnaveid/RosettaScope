package com.example.rosettascope.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.R

class WordRecyclerViewAdapter(
    private val wordBank: List<String>,
    private val discoveredWords: Map<String, Double>
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
            holder.tvWord.text = item
            holder.tvEngTranslation.text = item
            holder.progressBar.visibility = View.VISIBLE
            holder.tvMasteryLevel.visibility = View.VISIBLE

            when (discoveredWords.get(item)?.times(100)?.toInt()) {
                in 0..24 -> {
                    holder.progressBar.progress = (discoveredWords.get(item)!!/0.25).toInt()
                    holder.tvMasteryLevel.text = "Beginner"
                }

                in 25..76 -> {
                    holder.progressBar.progress = (discoveredWords.get(item)!!/0.77).toInt()
                    holder.tvMasteryLevel.text = "Intermediate"
                }

                in 77..94 -> {
                    holder.progressBar.progress = (discoveredWords.get(item)!!/0.95).toInt()
                    holder.tvMasteryLevel.text = "Advanced"
                }

                in 95..100 -> {
                    holder.imgView.setImageResource(R.drawable.mastered)
                    holder.progressBar.progress = 100
                    holder.tvMasteryLevel.text = "Mastered"
                }
            }
        }
//        val queue = Volley.newRequestQueue(holder.itemView.context)
//        val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/translate-word/${item}"
//
//        val translateWordRequest = StringRequest(
//            Request.Method.GET, url,
//            { response ->
//                holder.tvEngTranslation.text = response
//            },
//            { error ->
//                Toast.makeText(
//                    holder.itemView.context,
//                    "Error connecting to server",
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//                Log.e("VolleyRequest", error.toString())
//            })
//        queue.add(translateWordRequest)
//
//        when (item.score) {
//            in 0..50 -> {
//                holder.imgView.setImageResource(R.drawable.wrong)
//
//            }
//            in 51..89 -> {
//                holder.imgView.setImageResource(R.drawable.partial)
//            }
//            else -> {
//                holder.imgView.setImageResource(R.drawable.correct)
//            }
//
//        }
//
//        holder.tvGrapheme.text = item.word
//        holder.tvAccuracyScore.text = item.score.toString()
    }

    override fun getItemCount(): Int = wordBank.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgView: ImageView = itemView.findViewById(R.id.imageView_word_status)
        val tvWord: TextView = itemView.findViewById(R.id.textView_word)
        val tvEngTranslation: TextView = itemView.findViewById(R.id.textView_translation)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvMasteryLevel: TextView = itemView.findViewById(R.id.textView_mastery_level)
    }

}