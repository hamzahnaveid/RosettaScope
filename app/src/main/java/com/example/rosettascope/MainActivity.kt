package com.example.rosettascope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.rosettascope.nav.Navigation
import com.example.rosettascope.ui.theme.RosettaScopeTheme
import com.example.rosettascope.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            RosettaScopeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(modifier = Modifier
                        .padding(innerPadding),
                        authViewModel = authViewModel)
                }
            }
        }
    }
}