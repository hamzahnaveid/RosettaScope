package com.example.rosettascope.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.R

class ChallengeTrainingRecyclerViewAdapter (
    private val translationMap: Map<String, String>,
    private val feedbackMap: Map<String, String>,
    private val isDataReady: () -> Boolean,
    private val displayExerciseListDialog: () -> Unit
) : RecyclerView.Adapter<ChallengeTrainingRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_challenge_training_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = translationMap.keys.elementAt(position)
        val translation = translationMap[word] ?: "Loading translation..."
        val feedback = feedbackMap[word] ?: "Loading initial feedback..."

        holder.tvTranslation.text = translation
        holder.tvTrainingWord.text = word
        holder.tvFeedback.text = feedback

        val dataReady = isDataReady()

        holder.btnTrainingStart.isEnabled = dataReady
        holder.btnTrainingStart.alpha = if (dataReady) 1.0f else 0.5f
        holder.btnTrainingStart.text = if (dataReady) "Start Training" else "Please wait..."

        holder.btnTrainingStart.setOnClickListener {
            displayExerciseListDialog()
        }
    }

    override fun getItemCount(): Int {
        return translationMap.keys.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTranslation: TextView = itemView.findViewById(R.id.textView_challenge_training_word_translation)
        val tvTrainingWord: TextView = itemView.findViewById(R.id.textView_challenge_training_word)
        val tvFeedback: TextView = itemView.findViewById(R.id.textView_challenge_training_feedback)
        val btnTrainingStart: Button = itemView.findViewById(R.id.button_challenge_training_start)
    }


}