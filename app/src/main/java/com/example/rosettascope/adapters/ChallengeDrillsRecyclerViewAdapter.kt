package com.example.rosettascope.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.R
import com.example.rosettascope.models.Score

class ChallengeDrillsRecyclerViewAdapter(
    private val drillBank: List<Score>,
    private val playPronunAudio: (refText: String) -> Unit,
    private val recordPronunAudio: (refText: String, engWord: String, isRecording: Boolean) -> Unit,
    private val isAudioReady: (word: String) -> Boolean,
    private val getResult: (String) -> Boolean?,
    private val displayFeedbackDialog: () -> Unit
    ) : RecyclerView.Adapter<ChallengeDrillsRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_challenge_drill_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgViewChallengeDrillMarker.visibility = View.INVISIBLE
        holder.imgViewChallengeDrillMarker.alpha = 0f

        holder.tvChallengeDrillMarkerLabel.visibility = View.INVISIBLE
        holder.tvChallengeDrillMarkerLabel.text = ""

        holder.btnChallengeDrillFeedback.visibility = View.INVISIBLE
        holder.btnChallengeDrillFeedback.alpha = 0f
        holder.btnChallengeDrillFeedback.setOnClickListener(null)

        val word = drillBank[position].word
        var recording = false
        holder.tvChallengeDrillWord.text = word

        val audioReady = isAudioReady(word)

        holder.btnChallengeDrillPlay.isEnabled = audioReady
        holder.btnChallengeDrillPlay.alpha = if (audioReady) 1.0f else 0.5f
        holder.btnChallengeDrillRecord.isEnabled = audioReady
        holder.btnChallengeDrillRecord.alpha = if (audioReady) 1.0f else 0.5f

        holder.btnChallengeDrillPlay.setOnClickListener {
            if (audioReady) {
                playPronunAudio(word)
            }
        }

        holder.btnChallengeDrillRecord.setOnClickListener {
            recording = !recording
            recordPronunAudio(word, drillBank[position].engWord, recording)
            if (recording) {
                holder.btnChallengeDrillRecord.setBackgroundResource(R.drawable.bg_text3)
                holder.btnChallengeDrillRecord.setImageResource(R.drawable.recording)
                holder.btnChallengeDrillRecord.setPadding(16)
            }
            else {
                holder.btnChallengeDrillRecord.setBackgroundResource(R.drawable.bg_text2)
                holder.btnChallengeDrillRecord.setImageResource(R.drawable.microphone)
                holder.btnChallengeDrillRecord.setPadding(16)
            }

        }

        val result = getResult(word)
        holder.imgViewChallengeDrillMarker.visibility = View.VISIBLE
        holder.tvChallengeDrillMarkerLabel.visibility = View.VISIBLE
        when (result) {
            true -> {
                holder.imgViewChallengeDrillMarker.setImageResource(R.drawable.correct)
                holder.tvChallengeDrillMarkerLabel.text = "Correct!"
                holder.tvChallengeDrillMarkerLabel.setTextColor(android.graphics.Color.GREEN)
                holder.imgViewChallengeDrillMarker.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            }
            false -> {
                holder.imgViewChallengeDrillMarker.setImageResource(R.drawable.wrong)
                holder.tvChallengeDrillMarkerLabel.text = "Incorrect"
                holder.tvChallengeDrillMarkerLabel.setTextColor(android.graphics.Color.RED)
                holder.imgViewChallengeDrillMarker.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
                holder.btnChallengeDrillFeedback.visibility = View.VISIBLE
                holder.btnChallengeDrillFeedback.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
                holder.btnChallengeDrillFeedback.setOnClickListener {
                    displayFeedbackDialog()
                }
            }

            null -> {
                holder.imgViewChallengeDrillMarker.visibility = View.INVISIBLE
                holder.tvChallengeDrillMarkerLabel.visibility = View.INVISIBLE
                holder.btnChallengeDrillFeedback.visibility = View.INVISIBLE
            }
        }
    }

    override fun getItemCount(): Int = drillBank.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgViewChallengeDrillMarker: ImageView = itemView.findViewById(R.id.imageView_challenge_drill_marker)
        val tvChallengeDrillMarkerLabel: TextView = itemView.findViewById(R.id.textView_challenge_drill_marker_label)
        val tvChallengeDrillWord: TextView = itemView.findViewById(R.id.textView_challenge_drill_word)
        val btnChallengeDrillFeedback: ImageButton = itemView.findViewById(R.id.button_challenge_drill_feedback)
        val btnChallengeDrillPlay: ImageButton = itemView.findViewById(R.id.button_challenge_drill_play)
        val btnChallengeDrillRecord: ImageButton = itemView.findViewById(R.id.button_challenge_drill_record)
    }
}