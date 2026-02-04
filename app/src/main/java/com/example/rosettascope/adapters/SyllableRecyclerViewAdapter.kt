package com.example.rosettascope.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.R
import com.example.rosettascope.models.Syllable

class SyllableRecyclerViewAdapter(
    private val values: List<Syllable>
) : RecyclerView.Adapter<SyllableRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_syllable_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            when (item.accuracyScore) {
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

            holder.tvGrapheme.text = item.grapheme
            holder.tvAccuracyScore.text = item.accuracyScore.toString()
        }

        override fun getItemCount(): Int = values.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgView: ImageView = itemView.findViewById(R.id.imageView2)
           val tvGrapheme: TextView = itemView.findViewById(R.id.textview_grapheme)
            val tvAccuracyScore: TextView = itemView.findViewById(R.id.textview_grapheme_accuracyscore)

        }
}