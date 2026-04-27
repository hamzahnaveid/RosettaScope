package com.example.rosettascope.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.MainActivity
import com.example.rosettascope.R
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.UserViewModel
import java.util.Calendar

@Preview(showBackground = true)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: UserViewModel = viewModel(LocalContext.current as ComponentActivity)) {
    val user = viewModel.user

    if (user == null) {
        Text("Loading...")
        return
    }

    Image(painter = painterResource(id = R.drawable.bg_home),
        contentDescription = "Background",
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxSize()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        ElevatedCard(
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            SettingsCardContent(user)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCardContent(user: User) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                        .edit().putString("email", "").apply()
                    context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                        .edit().putString("target_language", "").apply()
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.signout),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Sign out",
                        fontSize = 18.sp,
                        color = Color(0xFF69A1A6)
                    )
                }
            }
        }

        val openTimePicker = remember { mutableStateOf(false) }

        if (openTimePicker.value) {
            SetTimePickerContent(
                onDismiss = {
                    openTimePicker.value = false
                },
                onConfirm = { timeState ->
                    openTimePicker.value = false

                    val hour = timeState.hour
                    val minute = timeState.minute

                    Log.d("TimePicker", "Selected: $hour:$minute")
                }
            )
        }

        Box {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                onClick = {
                    openTimePicker.value = true
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.clock),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Schedule notifications",
                        fontSize = 18.sp,
                        color = Color(0xFF69A1A6)
                    )
                }

            }
        }

        val openAlertDialog = remember { mutableStateOf(false) }

        if (openAlertDialog.value) {
            DeleteAccountAlertDialog(
                onDismissRequest = {
                    openAlertDialog.value = false
                },
                onConfirmation = {
                    openAlertDialog.value = false
                    deleteUser(user.email, context)
                })
        }

        Box {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                onClick = {
                    openAlertDialog.value = true
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Delete account",
                        fontSize = 18.sp,
                        color = Color(0xFFD25859)
                    )
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTimePickerContent(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirmation = { onConfirm(timePickerState) }
    ) {
        TimePicker(
            state = timePickerState
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() } ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmation() } ) {
                Text("OK")
            }
        },
        text = { content() }

    )
}

@Composable
fun DeleteAccountAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit)
{
    AlertDialog(
        icon = {
            Image(painterResource(R.drawable.warning), contentDescription = "Warning")
        },
        title = {
            Text(text = "Confirm Account Deletion")
        },
        text = {
            Text(text = "You are about to delete your Rosetta Scope account and all of your language learning progress. This action cannot be undone.")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Delete Account",
                    color = Color(0xFFD25859)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel",
                    color = Color(0xFF69A1A6)
                )
            }
        }
    )
}

private fun deleteUser(email: String, context: Context) {
    val queue = Volley.newRequestQueue(context)
    val url = "https://gaston-distant-unamicably.ngrok-free.dev/user-delete/$email"

    val getUserRequest = JsonObjectRequest(
        Request.Method.DELETE, url, null,
        { response ->
            if (response.getString("response").equals("success")) {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                    .edit().putString("email", "").apply()
                context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                    .edit().putString("target_language", "").apply()

                Toast.makeText(
                    context,
                    "Account successfully deleted",
                    Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                Toast.makeText(
                    context,
                    "An error has occurred. Please try again.",
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