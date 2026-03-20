package com.example.rosettascope.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.R
import com.example.rosettascope.helpers.KTAnswerData
import com.example.rosettascope.helpers.KTChallengeState
import com.example.rosettascope.models.Score

class ChallengeQuestionsRecyclerViewAdapter(
    private val questionBank: List<Score>,
    private val counter: TextView,
    private val progressBar: ProgressBar,
    private val onNextClicked: (Int) -> Unit,
    private val updateConfidenceScore: (KTAnswerData) -> Unit,
    private val completeChallenge: () -> Unit
) : RecyclerView.Adapter<ChallengeQuestionsRecyclerViewAdapter.ViewHolder>() {
    private val stateMap = mutableMapOf<Int, KTChallengeState>()

    private var answerCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_challenge_question_item, parent, false)
        progressBar.max = itemCount
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = questionBank[position].word
        val state = stateMap.getOrPut(position) { KTChallengeState() }

        holder.tvChallengeQuestionWord.text = word
        holder.etChallengeQuestionInput.setText(state.input)
        holder.btnChallengeQuestionCheck.isEnabled = !state.checked
        holder.btnChallengeQuestionCheck.alpha = if (state.checked) 0.5f else 1f
        holder.btnChallengeQuestionNext.isEnabled = position < itemCount - 1
        holder.btnChallengeQuestionNext.alpha = if (position == itemCount - 1) 0.5f else 1f

        if (state.checked) {
            holder.imgViewChallengeQuestionMarker.visibility = View.VISIBLE
            holder.tvChallengeQuestionMarkerLabel.visibility = View.VISIBLE
            holder.tvChallengeQuestionAnswer.visibility = View.VISIBLE

            holder.tvChallengeQuestionAnswer.text = state.answer

            if (state.correct) {
                holder.imgViewChallengeQuestionMarker.setImageResource(R.drawable.correct)
                holder.tvChallengeQuestionMarkerLabel.text = "Correct!"
                holder.tvChallengeQuestionMarkerLabel.setTextColor(android.graphics.Color.GREEN)
            } else {
                holder.imgViewChallengeQuestionMarker.setImageResource(R.drawable.wrong)
                holder.tvChallengeQuestionMarkerLabel.text = "Incorrect"
                holder.tvChallengeQuestionMarkerLabel.setTextColor(android.graphics.Color.RED)
            }
        }
        else {
            holder.imgViewChallengeQuestionMarker.visibility = View.INVISIBLE
            holder.tvChallengeQuestionMarkerLabel.visibility = View.INVISIBLE
            holder.tvChallengeQuestionAnswer.visibility = View.INVISIBLE
        }

        holder.etChallengeQuestionInput.addTextChangedListener {
            state.input = it.toString()
        }

        holder.btnChallengeQuestionCheck.setOnClickListener {
            val pos = holder.adapterPosition
            val currentState = stateMap.getOrPut(pos) { KTChallengeState() }
            currentState.input = holder.etChallengeQuestionInput.text.toString()

            val queue = Volley.newRequestQueue(holder.itemView.context)
            val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/translate-word/${word}/${questionBank[position].language}"

            val checkAnswerRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    val answer = response
                        .replace("\"", "")
                        .replace(".", "")

                    holder.imgViewChallengeQuestionMarker.visibility = View.VISIBLE
                    holder.tvChallengeQuestionMarkerLabel.visibility = View.VISIBLE
                    holder.tvChallengeQuestionAnswer.visibility = View.VISIBLE
                    holder.tvChallengeQuestionAnswer.text = answer

                    if (holder.etChallengeQuestionInput.text.toString().lowercase() == answer.lowercase()) {
                        holder.imgViewChallengeQuestionMarker.setImageResource(R.drawable.correct)
                        holder.tvChallengeQuestionMarkerLabel.text = "Correct!"
                        holder.tvChallengeQuestionMarkerLabel.setTextColor(android.graphics.Color.GREEN)
                    }
                    else {
                        holder.imgViewChallengeQuestionMarker.setImageResource(R.drawable.wrong)
                        holder.tvChallengeQuestionMarkerLabel.text = "Incorrect"
                        holder.tvChallengeQuestionMarkerLabel.setTextColor(android.graphics.Color.RED)
                    }
                    currentState.answer = answer
                    currentState.checked = true
                    currentState.correct = currentState.input.lowercase() == answer.lowercase()
                    notifyItemChanged(pos)
                    updateConfidenceScore(KTAnswerData(questionBank[pos].engWord, currentState.correct))
                    Log.d("ChallengeRecyclerView", response)
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
            queue.add(checkAnswerRequest)
            holder.btnChallengeQuestionCheck.isEnabled = false
            holder.btnChallengeQuestionCheck.alpha = 0.5f
            counter.text = (++answerCount).toString() + "/" + itemCount.toString()
            progressBar.progress++
            if (answerCount == itemCount) {
                completeChallenge()
            }
        }
        holder.btnChallengeQuestionNext.setOnClickListener {
            onNextClicked(position)
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