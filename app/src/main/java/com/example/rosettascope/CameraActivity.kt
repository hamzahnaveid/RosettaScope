// This file is made up of code that has been adapted from Google's MediaPipe Object Detection example code
// https://github.com/google-ai-edge/mediapipe-samples/blob/main/examples/object_detection/android/app/src/main/java/com/google/mediapipe/examples/objectdetection/MainActivity.kt
package com.example.rosettascope

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.rosettascope.databinding.ActivityHomeBinding


class CameraActivity : AppCompatActivity() {
    private lateinit var activityHomeBinding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityHomeBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(activityHomeBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityHomeBinding.navigation.setupWithNavController(navController)
    }
}