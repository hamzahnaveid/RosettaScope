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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import org.json.JSONObject

    var targetLanguage = "English"
    var proficiency  = "Beginner"
@Composable
fun SignupScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

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
            text = "Sign Up",
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

        Spacer(modifier = Modifier.height(8.dp))

        DisplayLangSpinner()

        Spacer(modifier = Modifier.height(8.dp))

        DisplayProficiencySpinner()

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
                when (targetLanguage) {
                    "English" -> targetLanguage = "en-US"
                    "Mandarin Chinese" -> targetLanguage = "zh-CN"
                    "German" -> targetLanguage = "de-DE"
                    "French" -> targetLanguage = "fr-FR"
                    "Spanish" -> targetLanguage = "es-ES"
                    "Korean" -> targetLanguage = "ko-KR"
                    "Japanese" -> targetLanguage = "ja-JP"
                    "Russian" -> targetLanguage = "ru-RU"
                }

                val queue = Volley.newRequestQueue(context);
                val url = "https://gaston-distant-unamicably.ngrok-free.dev/user-save"

                val userJsonBody = JSONObject()
                userJsonBody.put("email", email)
                userJsonBody.put("password", password)
                userJsonBody.put("proficiency", proficiency)
                userJsonBody.put("targetLanguage", targetLanguage)
                userJsonBody.put("wordsEncountered", 0)
                userJsonBody.put("wordsMastered", 0)


                val signUpRequest = JsonObjectRequest(Request.Method.POST, url, userJsonBody,
                    { response ->
                        navController.navigate("home")

                        context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                            .edit().putString("email", email).apply()

                        context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                            .edit().putString("target_language", targetLanguage).apply()

                        Toast.makeText(
                            context,
                            "Account successfully created",
                            Toast.LENGTH_SHORT)
                            .show()
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
//            authViewModel.signup(email, password)
        }) {
            Text(text = "Create account")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("login")
        }) {
            Text(text = "Already have an account? Login")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayLangSpinner() {
    val parentOptions = listOf("English","Mandarin Chinese","German","French","Spanish","Korean","Japanese","Russian")
    var expandedState by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(parentOptions[0]) }

    ExposedDropdownMenuBox(expanded = expandedState,
        onExpandedChange = { expandedState = !expandedState }) {

        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState)
            })

        ExposedDropdownMenu(expanded = expandedState,
            onDismissRequest = { expandedState = false }) {

            parentOptions.forEach {
                item ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayProficiencySpinner() {
    val parentOptions = listOf("Beginner","Intermediate","Advanced")
    var expandedState by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(parentOptions[0]) }

    ExposedDropdownMenuBox(expanded = expandedState,
        onExpandedChange = { expandedState = !expandedState }) {

        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState)
            })

        ExposedDropdownMenu(expanded = expandedState,
            onDismissRequest = { expandedState = false }) {

            parentOptions.forEach {
                    item ->
                DropdownMenuItem(text = {
                    Text(text = item)
                }, onClick = {
                    selectedOption = item
                    expandedState = false
                    proficiency = selectedOption
                })
            }
        }
    }
}
