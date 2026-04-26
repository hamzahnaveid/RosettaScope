package com.example.rosettascope.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rosettascope.CameraActivity
import com.example.rosettascope.ChallengeActivity
import com.example.rosettascope.R
import com.example.rosettascope.ReviseActivity
import com.example.rosettascope.WordBankActivity
import com.example.rosettascope.viewmodels.UserViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: UserViewModel = viewModel(LocalContext.current as ComponentActivity)) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val email = context.getSharedPreferences("USER", Context.MODE_PRIVATE)
        .getString("email", "").toString()

        if (viewModel.user == null) {
            viewModel.loadUser(email)
        }
        viewModel.loadUser(email)
    }

    Image(painter = painterResource(id = R.drawable.bg_home),
        contentDescription = "Background",
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxSize()
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        UserGreeting()
        StreakDisplay(viewModel)
        ActivityButtons(viewModel)
    }
}

@Composable
fun UserGreeting() {
    val targetLanguage = LocalContext.current.getSharedPreferences("USER", Context.MODE_PRIVATE)
        .getString("target_language", "").toString()

    var flagDrawable : Painter? = null

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Image(
            modifier = Modifier
                .width(55.dp)
                .height(55.dp),
            painter = flagDrawable as Painter,
            contentDescription = "Target language flag")

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(40.dp)
            .align(alignment = Alignment.CenterVertically)
        ) {
            Text(text = "Hello!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StreakDisplay(viewModel: UserViewModel) {
    if (viewModel.user == null) {
        return
    }

    var streakDrawable : Painter? = null


    streakDrawable = when (viewModel.user?.currentStreak) {
        0 -> painterResource(R.drawable.no_streak)
        in 1..5 -> painterResource(R.drawable.low_streak)
        in 6..10 -> painterResource(R.drawable.medium_streak)
        else -> painterResource(R.drawable.high_streak)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Image(
            modifier = Modifier
                .size(55.dp)
                .padding(top = 16.dp),
            painter = streakDrawable,
            contentDescription = "Streak badge")

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 34.dp)
        ) {
            Text(text = viewModel.user?.currentStreak.toString(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@SuppressLint("RememberInComposition")
@Composable
fun ActivityButtons(viewModel: UserViewModel) {
    val context = LocalContext.current

    DiscoverButton(context)
    ReviseButton(context, viewModel)
    ChallengeButton(context, viewModel)
    WordBankButton(context)
}

@Composable
fun DiscoverButton(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Image(
            modifier = Modifier
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {
                        val intent = Intent(context, CameraActivity::class.java)
                        context.startActivity(intent)
                    }
                ),
            painter = painterResource(id = R.drawable.discover_binoculars),
            contentDescription = "Discover",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ChallengeButton(context: Context, viewModel: UserViewModel) {
    var img = painterResource(id = R.drawable.challenge_dartboard)
    viewModel.user?.wordsEncountered?.let {
        if (it < 3) {
            img = painterResource(id = R.drawable.challenge_locked)
        }
    }

    val openAlertDialog = remember { mutableStateOf(false) }

    if (openAlertDialog.value) {
        LockedActivityAlertDialog(
            onDismissRequest = {
                openAlertDialog.value = false
            })
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Image(
            modifier = Modifier
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {
                        if (viewModel.user!!.wordsEncountered < 3) {
                            openAlertDialog.value = true
                        }
                        else {
                            val intent = Intent(context, ChallengeActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                ),
            painter = img,
            contentDescription = "Challenge",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun WordBankButton(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Image(
            modifier = Modifier
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {
                        val intent = Intent(context, WordBankActivity::class.java)
                        context.startActivity(intent)
                    }
                ),
            painter = painterResource(id = R.drawable.wordbank_book),
            contentDescription = "Word Bank",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ReviseButton(context: Context, viewModel: UserViewModel) {
    var img = painterResource(id = R.drawable.revise_glasses)
    viewModel.user?.wordsEncountered?.let {
        if (it < 3) {
            img = painterResource(id = R.drawable.revise_locked)
        }
    }

    val openAlertDialog = remember { mutableStateOf(false) }

    if (openAlertDialog.value) {
        LockedActivityAlertDialog(
            onDismissRequest = {
                openAlertDialog.value = false
            })
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Image(
            modifier = Modifier
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {
                        if (viewModel.user!!.wordsEncountered < 3) {
                            openAlertDialog.value = true
                        }
                        else {
                            val intent = Intent(context, ReviseActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                ),
            painter = img,
            contentDescription = "Revise",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun LockedActivityAlertDialog(
    onDismissRequest: () -> Unit
)
{
    AlertDialog(
        icon = {
            Image(painterResource(R.drawable.lock   ), contentDescription = "Locked")
        },
        title = {
            Text(text = "Activity Locked")
        },
        text = {
            Text(text = "This activity is locked until you have discovered at least 3 words. Get out there and explore!")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("OK",
                    color = Color(0xFF69A1A6)
                )
            }
        }
    )
}