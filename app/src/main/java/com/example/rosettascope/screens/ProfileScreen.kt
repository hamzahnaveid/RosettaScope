package com.example.rosettascope.screens

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rosettascope.R
import com.example.rosettascope.models.User
import com.example.rosettascope.viewmodels.UserViewModel

private var flagDrawable : Painter? = null
private var strLanguage: String? = null


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
    val streak = 0 //todo get streak from server
    val streakDrawable = when (streak) {
        in 1..3 -> R.drawable.low_streak
        in 4..7 -> R.drawable.medium_streak
        in 8..30 -> R.drawable.high_streak
        else -> R.drawable.no_streak
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
                painter = painterResource(streakDrawable),
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
        
        Button(
            onClick = {
                // TODO:   
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
fun getFlag(targetLanguage: String) {
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

fun getStrLanguage(targetLanguage: String) {
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