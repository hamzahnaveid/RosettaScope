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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.example.rosettascope.models.User
import com.google.gson.Gson
import org.json.JSONObject

var targetLanguage = "German"

@Composable
fun SignupScreen(modifier: Modifier = Modifier, navController: NavController) {

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

        Text(text = "Sign Up",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight(500),
                color = Color.White
            ),
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.Start)
        )

        Text("Start your journey in language learning with Rosetta Scope",
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

        Spacer(modifier = Modifier.height(8.dp))

        DisplayLangSpinner()

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
                when (targetLanguage) {
                    "German" -> targetLanguage = "de-DE"
                    "French" -> targetLanguage = "fr-FR"
                    "Spanish" -> targetLanguage = "es-ES"
                    "Vietnamese" -> targetLanguage = "vi-VN"
                    "Mandarin Chinese" -> targetLanguage = "zh-CN"
                    "Arabic" -> targetLanguage = "ar-SA"
                    "Hindi" -> targetLanguage = "hi-IN"
                    "Korean" -> targetLanguage = "ko-KR"
                    "Japanese" -> targetLanguage = "ja-JP"
                    "Russian" -> targetLanguage = "ru-RU"
                    "Swedish" -> targetLanguage = "sv-SE"
                    "Finnish" -> targetLanguage = "fi-FI"
                    "Polish" -> targetLanguage = "pl-PL"
                    "Italian" -> targetLanguage = "it-IT"
                    "Dutch" -> targetLanguage = "nl-NL"
                }

                val gson = Gson()
                val queue = Volley.newRequestQueue(context)
                val url = "https://gaston-distant-unamicably.ngrok-free.dev/signup"

                val user = User(email, password,  targetLanguage, 0, 0, ArrayList(), mutableMapOf())
                val userJsonBody = gson.toJson(user)

                val signUpRequest = JsonObjectRequest(Request.Method.POST, url, JSONObject(userJsonBody),
                    { response ->
                        if (response.getString("response").equals("success")) {
                            val intent = Intent(context, HomeActivity::class.java)
                            context.startActivity(intent)

                            context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                                .edit().putString("email", email).apply()

                            context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                                .edit().putString("target_language", targetLanguage).apply()

                            Toast.makeText(
                                context,
                                "Account successfully created",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                        else {
                            Toast.makeText(
                                context,
                                "Email already in use",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    { error ->
                        Toast.makeText(
                            context,
                            "Error connecting to server",
                            Toast.LENGTH_SHORT)
                            .show()
                        Log.e("VolleyRequest", error.toString())
                    })
                queue.add(signUpRequest)
            }
        },
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.rosetta_yellow)
            ),
            modifier = Modifier
                .height(50.dp))
        {
            Text(text = "Sign Up",
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
                "Already have an account? ",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.White
                )
            )

            Text(
                "Login",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight(800),
                    color = Color.White
                ),
                modifier = Modifier.clickable {
                    navController.navigate("login")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayLangSpinner() {
    val parentOptions = listOf(
        "German",
        "French",
        "Spanish",
        "Vietnamese",
        "Mandarin Chinese",
        "Arabic",
        "Hindi",
        "Korean",
        "Japanese",
        "Russian",
        "Swedish",
        "Finnish",
        "Polish",
        "Italian",
        "Dutch",
    )
    var expandedState by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(parentOptions[0]) }

    ExposedDropdownMenuBox(
        expanded = expandedState,
        onExpandedChange = { expandedState = !expandedState }) {

        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = colorResource(R.color.rosetta_yellow),
                focusedLabelColor = colorResource(R.color.rosetta_yellow),
                focusedTextColor = Color.White,
                unfocusedContainerColor = Color.Transparent
            )
        )

        ExposedDropdownMenu(
            expanded = expandedState,
            onDismissRequest = { expandedState = false }) {

            parentOptions.forEach { item ->
                DropdownMenuItem(text = {
                    Text(text = item)
                }, onClick = {
                    selectedOption = item
                    expandedState = false
                    targetLanguage = selectedOption
                })
            }
        }
    }
}

