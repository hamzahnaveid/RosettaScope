package com.example.rosettascope.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rosettascope.R
import com.example.rosettascope.models.Score

class ChallengeQuestionsRecyclerViewAdapter(
    private val questionBank: List<Score>,
    private val counter: TextView,
    private val progressBar: ProgressBar
) : RecyclerView.Adapter<ChallengeQuestionsRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_challenge_question_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        progressBar.max = itemCount
        val word = questionBank[position].word
        holder.tvChallengeQuestionWord.text = word

        holder.btnChallengeQuestionCheck.setOnClickListener {
            counter.text = (position + 1).toString() + "/" + itemCount.toString()
            holder.btnChallengeQuestionCheck.isEnabled = false
            holder.btnChallengeQuestionCheck.alpha = 0.5f
            progressBar.progress++
        }
        holder.btnChallengeQuestionNext.setOnClickListener {

        }
    }

    override fun getItemCount(): Int = questionBank.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgViewChallengeQuestionMarker: ImageView = itemView.findViewById(R.id.imageView_challenge_question_marker)
        val tvChallengeQuestionMarkerLabel: TextView = itemView.findViewById(R.id.textView_challenge_question_marker_label)
        val tvChallengeQuestionWord: TextView = itemView.findViewById(R.id.textView_challenge_question_word)
        val etChallengeQuestionInput: EditText = itemView.findViewById(R.id.editText_challenge_question_input)
        val tvChallengeQuestionAnswer: TextView = itemView.findViewById(R.id.textView_challenge_question_answer)
        val btnChallengeQuestionCheck: Button = itemView.findViewById(R.id.button_challenge_question_check)
        val btnChallengeQuestionNext: Button = itemView.findViewById(R.id.button_challenge_question_next)
    }
}