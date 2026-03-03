package com.example.rosettascope.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rosettascope.R

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    Image(painter = painterResource(id = R.drawable.login_bg),
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.transparent_logo),
            contentDescription = null,
            modifier = Modifier
                .width(320.dp)
                .height(240.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            "Rosetta Scope",
            fontSize = 32.sp,
            fontWeight = FontWeight(700),
            color = Color.White
        )

        Text(
            "Intelligent AR-Powered Language Learning",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
            color = Color.White
        )

        Spacer(modifier = Modifier.weight(1f))


        Button(
            onClick = {
                navController.navigate("login")
            },
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.rosetta_yellow)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = "Sign In With Email",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White
                )
            )
        }

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