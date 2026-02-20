package com.example.rosettascope.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rosettascope.CameraActivity

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {

    val context = LocalContext.current

    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Home",
            fontSize = 32.sp
        )

        Button(
            onClick = {
                val intent = Intent(context, CameraActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text(text = "Camera")
        }

        Button(
            onClick = {
                //TODO
            }
        ) {
            Text(text = "Sign Out")
        }
    }
}