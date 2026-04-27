package com.example.rosettascope.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.R

class ResultsRecyclerViewAdapter(
    private val resultsMap: Map<String, Double>,
    private val userMap: Map<String, Double>,
    private val targetLanguage: String
) : RecyclerView.Adapter<ResultsRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_result_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultsRecyclerViewAdapter.ViewHolder, position: Int) {
        val word = resultsMap.keys.elementAt(position)
        val confidenceScore = resultsMap.values.elementAt(position)

        holder.tvWord.text = word.replace("/$targetLanguage", "")

        when (confidenceScore.times(100).toInt()) {
            in 0..24 -> {
                holder.progressBar.setProgress((confidenceScore/0.25).times(100).toInt(), true)
                holder.tvMastery.text = "Beginner"
            }

            in 25..76 -> {
                holder.progressBar.setProgress((confidenceScore/0.77).times(100).toInt(), true)
                holder.tvMastery.text = "Intermediate"
            }

            in 77..94 -> {
                holder.progressBar.setProgress((confidenceScore/0.95).times(100).toInt(), true)
                holder.tvMastery.text = "Advanced"
            }

            in 95..100 -> {
                holder.ivMastery.setImageResource(R.drawable.mastered)
                holder.progressBar.setProgress(100, true)
                holder.tvMastery.text = "Mastered"
            }
        }


        if (userMap.get(word)!! > resultsMap.get(word)!!) {
            holder.ivMastery.setImageResource(R.drawable.down_arrow)
        }
        else if (userMap.get(word)!! < resultsMap.get(word)!!){
            holder.ivMastery.setImageResource(R.drawable.up_arrow)
        }
        else {
            holder.ivMastery.setImageResource(R.drawable.middle)
        }
    }

    override fun getItemCount(): Int = resultsMap.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWord: TextView = itemView.findViewById(R.id.label_pb_result_word)
        val progressBar: ProgressBar = itemView.findViewById(R.id.pb_result_mastery)
        val tvMastery: TextView = itemView.findViewById(R.id.label_pb_result_mastery)
        val ivMastery: ImageView = itemView.findViewById(R.id.imageView_pb_result_mastery)
    }

}
