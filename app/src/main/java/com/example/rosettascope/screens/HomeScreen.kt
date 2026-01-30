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
import com.example.rosettascope.viewmodels.AuthViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

//    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
//    val email = context.getSharedPreferences("USER", Application.MODE_PRIVATE).getString("email", "")

//    LaunchedEffect(email) {
//        val queue = Volley.newRequestQueue(context);
//        val url = "https://gaston-distant-unamicably.ngrok-free.dev/user/$email"
//
//        val getUserRequest = JsonObjectRequest(Request.Method.GET, url, null,
//            { response ->
//                navController.navigate("home")
//                AlertDialog.Builder(context)
//                    .setTitle("User Details")
//                    .setMessage(response.toString())
//                    .setPositiveButton("OK", null)
//                    .show()
//            },
//            { error ->
//                Toast.makeText(
//                    context,
//                    "Error connecting to server",
//                    Toast.LENGTH_SHORT)
//                    .show()
//                Log.e("VolleyRequest", error.toString())
//            })
//        queue.add(getUserRequest)
//    }

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
                authViewModel.signout()
            }
        ) {
            Text(text = "Sign Out")
        }
    }
}