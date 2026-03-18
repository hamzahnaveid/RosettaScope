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

class ChallengeWordsRecyclerViewAdapter(
    private val translatedWords: List<String>,
    private val wordMap: MutableMap<String, Boolean>,
    private val targetLanguage: String
) : RecyclerView.Adapter<ChallengeWordsRecyclerViewAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_challenge_word_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = translatedWords[position]

        val queue = Volley.newRequestQueue(holder.itemView.context)
        val url = "https://subopaquely-unirradiative-bradley.ngrok-free.dev/translate-word/${word}/${targetLanguage}"

        val translateWordRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val translatedWord = response
                    .replace("\"", "")
                    .replace("\\", "")
                if (wordMap[translatedWord.lowercase()] == true) {
                    holder.imgViewChallengeWordStatus.setImageResource(R.drawable.discovered)
                }
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
        queue.add(translateWordRequest)

        holder.tvChallengeWord.text = word.replace("\"", "")
    }

    override fun getItemCount(): Int = translatedWords.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgViewChallengeWordStatus: ImageView = itemView.findViewById(R.id.imageView_challenge_word_status)
        val tvChallengeWord: TextView = itemView.findViewById(R.id.textView_challenge_word)

    }
}