package com.example.rosettascope.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.models.User
import com.google.gson.Gson

class UserViewModel(application: Application) : AndroidViewModel(application) {
    var user by mutableStateOf<User?>(null)

    fun loadUser(email: String) {
        val gson = Gson()
        val queue = Volley.newRequestQueue(getApplication<Application>().applicationContext)
        val url = "https://gaston-distant-unamicably.ngrok-free.dev/user/$email"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val json = response.toString()
                user = gson.fromJson(json, User::class.java)
                Log.d("JavaDB", "User retrieved")
            },
            {
                    error ->
                Toast.makeText(
                application,
                "Error connecting to server",
                Toast.LENGTH_SHORT
                ).show()
                Log.e("VolleyRequest", error.toString())
            }
        )

        queue.add(request)
    }
}