package com.example.rosettascope.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rosettascope.CameraActivity
import com.example.rosettascope.ChallengeActivity
import com.example.rosettascope.R
import com.example.rosettascope.ReviseActivity
import com.example.rosettascope.WordBankActivity

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
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
        StreakDisplay()
        ActivityButtons()
    }
}

@Composable
fun UserGreeting() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(40.dp)
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
fun StreakDisplay() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Image(
            modifier = Modifier.size(55.dp),
            //TODO get streak from server
            painter = painterResource(id = R.drawable.no_streak),
            contentDescription = "Streak badge")

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 34.dp)
        ) {
            Text(text = "0",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@SuppressLint("RememberInComposition")
@Composable
fun ActivityButtons() {
    val context = LocalContext.current

    DiscoverButton(context)
    ChallengeButton(context)
    WordBankButton(context)
    ReviseButton(context)
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
fun ChallengeButton(context: Context) {
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
                        val intent = Intent(context, ChallengeActivity::class.java)
                        context.startActivity(intent)
                    }
                ),
            painter = painterResource(id = R.drawable.challenge_dartboard),
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
fun ReviseButton(context: Context) {
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
                        val intent = Intent(context, ReviseActivity::class.java)
                        context.startActivity(intent)
                    }
                ),
            painter = painterResource(id = R.drawable.revise_glasses),
            contentDescription = "Revise",
            contentScale = ContentScale.Crop
        )
    }
}