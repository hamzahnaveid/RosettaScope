package com.example.rosettascope.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rosettascope.HomeActivity
import com.example.rosettascope.R
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.UserViewModel

private var flagDrawable : Painter? = null
private var strLanguage: String? = null
private var switchToLanguage: String? = "German"


@Preview(showBackground = true)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: UserViewModel = viewModel(LocalContext.current as ComponentActivity)) {
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
            ProfileCardContent(user)
        }
    }
}

@Composable
fun ProfileHeader(user: User) {
    val streak = user.currentStreak
    var streakDrawable : Painter? = null

    streakDrawable = when (user.currentStreak) {
        0 -> painterResource(R.drawable.no_streak)
        in 1..5 -> painterResource(R.drawable.low_streak)
        in 6..10 -> painterResource(R.drawable.medium_streak)
        else -> painterResource(R.drawable.high_streak)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = user.email,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = streakDrawable,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$streak",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
fun ProfileCardContent(user: User) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(user!!.targetLanguage) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF69A1A6)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user!!.email.firstOrNull()?.uppercase().toString(),
                color = Color.White,
                fontSize = 28.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ProfileHeader(user)

        Spacer(modifier = Modifier.height(20.dp))

        UserStats(user)


        Spacer(modifier = Modifier.height(24.dp))

        val openDialog = remember { mutableStateOf(false) }

        if (openDialog.value) {
            SwitchLanguageDialog(openDialog = openDialog)
        }

        Button(
            onClick = {
                openDialog.value = true
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF69A1A6)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Switch Target Language", color = Color.White)
        }
    }
}

@Composable
fun UserStats(user: User) {
    getFlag(user.targetLanguage)
    getStrLanguage(user.targetLanguage)

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
                    .fillMaxWidth()
            ) {
                Row (
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = flagDrawable as Painter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    StatItem("Target Language", strLanguage.toString())
                }
            }
        }

        Box {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Row (
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.discovered_tag),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    StatItem("Words Encountered", user.wordsEncountered.toString())
                }
            }
        }

        Box {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Row (
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.mastered_tag),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    StatItem("Words Mastered", user.wordsMastered.toString())
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun getFlag(targetLanguage: String) {
    when (targetLanguage) {
        "de-DE" -> flagDrawable = painterResource(R.drawable.germany)
        "fr-FR" -> flagDrawable = painterResource(R.drawable.france)
        "es-ES" -> flagDrawable = painterResource(R.drawable.spain)
        "vi-VN" -> flagDrawable = painterResource(R.drawable.vietnam)
        "zh-CN" -> flagDrawable = painterResource(R.drawable.china)
        "ar-SA" -> flagDrawable = painterResource(R.drawable.saudi_arabia)
        "hi-IN" -> flagDrawable = painterResource(R.drawable.india)
        "ko-KR" -> flagDrawable = painterResource(R.drawable.south_korea)
        "ja-JP" -> flagDrawable = painterResource(R.drawable.japan)
        "ru-RU" -> flagDrawable = painterResource(R.drawable.russia)
        "sv-SE" -> flagDrawable = painterResource(R.drawable.sweden)
        "fi-FI" -> flagDrawable = painterResource(R.drawable.finland)
        "pl-PL" -> flagDrawable = painterResource(R.drawable.poland)
        "it-IT" -> flagDrawable = painterResource(R.drawable.italy)
        "nl-NL" -> flagDrawable = painterResource(R.drawable.netherlands)
        else -> flagDrawable = painterResource(R.drawable.undiscovered)
    }
}

private fun getStrLanguage(targetLanguage: String) {
    when (targetLanguage) {
        "de-DE" -> strLanguage = "German"
        "fr-FR" -> strLanguage = "French"
        "es-ES" -> strLanguage = "Spanish"
        "vi-VN" -> strLanguage = "Vietnamese"
        "zh-CN" -> strLanguage = "Simplified Chinese"
        "ar-SA" -> strLanguage = "Arabic"
        "hi-IN" -> strLanguage = "Hindi"
        "ko-KR" -> strLanguage = "Korean"
        "ja-JP" -> strLanguage = "Japanese"
        "ru-RU" -> strLanguage = "Russian"
        "sv-SE" -> strLanguage = "Swedish"
        "fi-FI" -> strLanguage = "Finnish"
        "pl-PL" -> strLanguage = "Polish"
        "it-IT" -> strLanguage = "Italian"
        "nl-NL" -> strLanguage = "Dutch"
        else -> strLanguage = "?"
    }
}

@Composable
fun SwitchLanguageDialog(openDialog: MutableState<Boolean>) {
    Dialog(
        onDismissRequest = {
            openDialog.value = false
            switchToLanguage = "German"
        }
    ) {
        SwitchLanguageDialogUI(openDialog = openDialog)
    }
}

@Composable
fun SwitchLanguageDialogUI(modifier: Modifier = Modifier, openDialog: MutableState<Boolean>) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.switch_language),
                contentDescription = "Change Language Warning",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .height(70.dp)
                    .fillMaxWidth()
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Change Target Language",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "You are about to switch target languages. Progress (excluding streaks) from your current target language will NOT carry over, but it will be saved if you decide to continue.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 25.dp, end = 25.dp)
                        .fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                DisplayLangSpinner()

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        updateTargetLanguage(switchToLanguage!!, context)

                        // set to default selection in dropdown box for next time
                        switchToLanguage = "German"
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1CB1B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayLangSpinner() {
    val parentOptions = listOf(
        "German",
        "French",
        "Spanish",
        "Vietnamese",
        "Simplified Chinese",
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
                    switchToLanguage = selectedOption
                })
            }
        }
    }
}

private fun updateTargetLanguage(newTargetLanguage: String, context: Context) {
    val email = context.getSharedPreferences("USER", Context.MODE_PRIVATE)
        .getString("email", "").toString()

    val newTargetLanguageCode = when (newTargetLanguage) {
        "German" -> "de-DE"
        "French" -> "fr-FR"
        "Spanish" -> "es-ES"
        "Vietnamese" -> "vi-VN"
        "Simplified Chinese" -> "zh-CN"
        "Arabic" -> "ar-SA"
        "Hindi" -> "hi-IN"
        "Korean" -> "ko-KR"
        "Japanese" -> "ja-JP"
        "Russian" -> "ru-RU"
        "Swedish" -> "sv-SE"
        "Finnish" -> "fi-FI"
        "Polish" -> "pl-PL"
        "Italian" -> "it-IT"
        "Dutch" -> "nl-NL"
        else -> "de-DE"
    }

    val queue = Volley.newRequestQueue(context)
    val url = "https://gaston-distant-unamicably.ngrok-free.dev/update-language/$email?newTargetLanguage=$newTargetLanguageCode"

    val updateLanguageRequest = StringRequest(
        Request.Method.PUT, url,
        { response ->
            context.getSharedPreferences("USER", Context.MODE_PRIVATE)
                .edit().putString("target_language", newTargetLanguageCode).apply()

            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        },
        { error ->
            Toast.makeText(
                context,
                "Unable to change language. Please try again.",
                Toast.LENGTH_SHORT
            )
                .show()
            Log.e("VolleyRequest", error.toString())
        })
    queue.add(updateLanguageRequest)
}