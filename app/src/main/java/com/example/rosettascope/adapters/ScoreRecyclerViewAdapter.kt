package com.example.rosettascope.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.R
import com.example.rosettascope.models.Score

class ScoreRecyclerViewAdapter(
    private val values: List<Score>
) : RecyclerView.Adapter<ScoreRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_score_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]

            val queue = Volley.newRequestQueue(holder.itemView.context)
            val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/translate-word/${item.word}/${item.language}"

            val translateWordRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    response.toLowerCase()
                    holder.tvOriginalText.text = response
                },
                { error ->
                    Toast.makeText(
                        holder.itemView.context,
                        "Error connecting to server",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.e("VolleyRequest", error.toString())
                })
            queue.add(translateWordRequest)

            when (item.score) {
                in 0..50 -> {
                    holder.imgView.setImageResource(R.drawable.wrong)

                }
                in 51..89 -> {
                    holder.imgView.setImageResource(R.drawable.partial)
                }
                else -> {
                    holder.imgView.setImageResource(R.drawable.correct)
                }

            }

            holder.tvGrapheme.text = item.word
            holder.tvAccuracyScore.text = item.score.toString()
        }

        override fun getItemCount(): Int = values.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgView: ImageView = itemView.findViewById(R.id.imageView2)
            val tvGrapheme: TextView = itemView.findViewById(R.id.textview_grapheme)
            val tvAccuracyScore: TextView = itemView.findViewById(R.id.textview_grapheme_accuracyscore)
            val tvOriginalText: TextView = itemView.findViewById(R.id.textview_original_text)

        }
}