package com.example.rosettascope.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rosettascope.viewmodels.AuthState
import com.example.rosettascope.viewmodels.AuthViewModel

@Composable
fun SignupScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT)
                .show()
            else -> Unit
        }
    }

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
            authViewModel.signup(email, password)
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

                })
            }
        }
    }
}
