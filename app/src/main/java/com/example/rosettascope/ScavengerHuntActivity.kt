package com.example.rosettascope

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ScavengerHuntActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scavenger_hunt)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonHome = findViewById<Button>(R.id.button_option_home)
        val buttonOutside = findViewById<Button>(R.id.button_option_outside)
        val buttonOther = findViewById<Button>(R.id.button_option_other)

        buttonHome.setOnClickListener { navCameraChallenge(buttonHome) }
        buttonOutside.setOnClickListener { navCameraChallenge(buttonOutside) }
        buttonOther.setOnClickListener { navCameraChallenge(buttonOther) }
    }

    fun navCameraChallenge(button: Button) {
        val intent = Intent(this, CameraActivity::class.java)
        val bundle = Bundle()
        when (button.id) {
            R.id.button_option_home -> bundle.putString("location", "home")
            R.id.button_option_outside -> bundle.putString("location", "outside")
            R.id.button_option_other -> bundle.putString("location", "other")
        }
        intent.putExtras(bundle)
        startActivity(intent)
    }
}