package com.example.rosettascope.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.HomeActivity
import com.example.rosettascope.R

@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    Image(painter = painterResource(id = R.drawable.signup_bg),
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )

    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.transparent_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 54.dp, start = 16.dp)
                .height(100.dp)
                .align(Alignment.Start)
                .offset(x = (-20).dp)
        )

        Text(text = "Login",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight(500),
                color = Color.White
            ),
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.Start)
        )

        Text("Progress in your language learning journey through your surroundings",
            style = TextStyle(
                fontSize = 20.sp,
                color = Color.White
            ),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Email Address")
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = colorResource(R.color.rosetta_yellow),
                focusedLabelColor = colorResource(R.color.rosetta_yellow),
                focusedTextColor = Color.White,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = colorResource(R.color.rosetta_yellow),
                focusedLabelColor = colorResource(R.color.rosetta_yellow),
                focusedTextColor = Color.White,
                unfocusedContainerColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    context,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                val queue = Volley.newRequestQueue(context)
                val url = "https://gaston-distant-unamicably.ngrok-free.dev/login?email=$email&password=$password"

                val getUserRequest = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        if (response.getString("response").equals("success")) {
                            val intent = Intent(context, HomeActivity::class.java)
                            context.startActivity(intent)

                            context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                                .edit().putString("email", email).apply()

                            val targetLanguage = response.getString("target_language")
                            context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                                .edit().putString("target_language", targetLanguage).apply()

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
        },
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.rosetta_yellow)
            ),
            modifier = Modifier
                .height(50.dp))
        {
            Text(text = "Login",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.padding(top=12.dp, bottom = 80.dp)
        ) {
            Text(
                "Don't have an account? ",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.White
                )
            )

            Text(
                "Sign Up",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight(800),
                    color = Color.White
                ),
                modifier = Modifier.clickable {
                    navController.navigate("signup")
                }
            )
        }
    }
}