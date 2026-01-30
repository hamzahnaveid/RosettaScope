package com.example.rosettascope.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.viewmodels.AuthViewModel

@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

//    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

//    LaunchedEffect(authState.value) {
//        when (authState.value) {
//            is AuthState.Authenticated -> navController.navigate("home")
//            is AuthState.Error -> Toast.makeText(
//                context,
//                (authState.value as AuthState.Error).message,
//                Toast.LENGTH_SHORT)
//                .show()
//            else -> Unit
//        }
//    }

    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    context,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                val queue = Volley.newRequestQueue(context);
                val url = "https://gaston-distant-unamicably.ngrok-free.dev/login?email=$email&password=$password"

                val getUserRequest = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        if (response.getString("response").equals("success")) {
                            navController.navigate("home")

                            context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                                .edit().putString("email", email).apply()

                            Toast.makeText(
                                context,
                                "Logged in successfully",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                        else {
                            Toast.makeText(
                                context,
                                "Invalid credentials",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    { error ->
                        Toast.makeText(
                            context,
                            "Error connecting to server",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.e("VolleyRequest", error.toString())
                    })
                queue.add(getUserRequest)

            }
//            authViewModel.login(email, password)
        }) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("signup")
        }) {
            Text(text = "Create an account")
        }
    }
}