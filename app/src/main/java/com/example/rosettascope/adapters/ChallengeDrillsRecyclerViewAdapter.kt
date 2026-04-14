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
    private val isAudioReady: (word: String) -> Boolean
    ) : RecyclerView.Adapter<ChallengeDrillsRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_challenge_drill_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = drillBank[position].word
        var recording = false
        holder.tvChallengeDrillWord.text = word

        val audioReady = isAudioReady(word)

        holder.btnChallengeDrillPlay.isEnabled = audioReady
        holder.btnChallengeDrillPlay.alpha = if (audioReady) 1.0f else 0.5f

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
    }

    override fun getItemCount(): Int = drillBank.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgViewChallengeDrillMarker: ImageView = itemView.findViewById(R.id.imageView_challenge_drill_marker)
        val tvChallengeDrillMarkerLabel: TextView = itemView.findViewById(R.id.textView_challenge_drill_marker_label)
        val tvChallengeDrillWord: TextView = itemView.findViewById(R.id.textView_challenge_drill_word)
        val btnChallengeDrillPlay: ImageButton = itemView.findViewById(R.id.button_challenge_drill_play)
        val btnChallengeDrillRecord: ImageButton = itemView.findViewById(R.id.button_challenge_drill_record)
    }
}