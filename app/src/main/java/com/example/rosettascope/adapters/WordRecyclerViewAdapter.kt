package com.example.rosettascope.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.R
import com.example.rosettascope.WordActivity

class WordRecyclerViewAdapter(
    private val wordBank: List<String>,
    private val discoveredWords: Map<String, Double>,
) : RecyclerView.Adapter<WordRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_word_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wordBank[position]
        holder.tvWord.text = item


        if (!discoveredWords.contains(item)) {
            holder.imgView.setImageResource(R.drawable.undiscovered)
            holder.progressBar.visibility = View.GONE
            holder.tvMasteryLevel.visibility = View.GONE
            holder.imgViewArrow.visibility = View.GONE

        }
        else {
            holder.layout.setOnClickListener { view ->
                val intent = Intent(view.context, WordActivity::class.java)
                intent.putExtra("word", item)
                view.context.startActivity(intent)
            }

            holder.imgView.setImageResource(R.drawable.discovered)
            holder.progressBar.visibility = View.VISIBLE
            holder.tvMasteryLevel.visibility = View.VISIBLE
            holder.imgViewArrow.visibility = View.VISIBLE

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
        val layout: ConstraintLayout = itemView.findViewById(R.id.word_item_layout)
        val imgView: ImageView = itemView.findViewById(R.id.imageView_word_status)
        val tvWord: TextView = itemView.findViewById(R.id.textView_word)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvMasteryLevel: TextView = itemView.findViewById(R.id.textView_mastery_level)
        val imgViewArrow: ImageView = itemView.findViewById(R.id.imageView_arrow_to_word)

    }

//    fun setTextViewToTranslation(word: String, textView: TextView, context: Context) {
//        val queue = Volley.newRequestQueue(context)
//        val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/translate-to-target/${word}/${targetLanguage}"
//
//        val translateWordRequest = StringRequest(
//            Request.Method.GET, url,
//            { response ->
//                textView.text = response
//            },
//            { error ->
//                Toast.makeText(
//                    context,
//                    "Error connecting to server",
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//                Log.e("VolleyRequest", error.toString())
//            })
//        queue.add(translateWordRequest)
//
//    }
}