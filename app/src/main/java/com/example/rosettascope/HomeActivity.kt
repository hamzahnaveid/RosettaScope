package com.example.rosettascope

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rosettascope.screens.HomeScreen
import com.example.rosettascope.screens.ProfileScreen
import com.example.rosettascope.screens.SettingsScreen
import com.example.rosettascope.ui.theme.RosettaScopeTheme

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RosettaScopeTheme {
                val navController = rememberNavController()
                Scaffold(bottomBar = {
                    BottomNavBar(navController)
                    }, content = { innerPadding ->
                    NavHostContainer(modifier = Modifier
                        .padding(innerPadding), navController = navController)
                }
                )
            }
        }
    }
}

@Composable
fun NavHostContainer(modifier: Modifier = Modifier, navController: NavHostController) {

    NavHost(navController = navController, startDestination = "Home", builder = {
        composable("Home") {
            HomeScreen(modifier)
        }
        composable("Profile") {
            ProfileScreen(modifier)
        }
        composable("Settings") {
            SettingsScreen(modifier)
        }
    })

}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val bottomNavItemsList = prepareBottomNav()
    var selectedItem by remember {
        mutableStateOf("Home")
    }

    NavigationBar(tonalElevation = 3.dp, containerColor = Color.White) {
        bottomNavItemsList.forEach { bottomNavItem ->
            NavigationBarItem(
                selected = (selectedItem == bottomNavItem.label),
                onClick = {
                    selectedItem = bottomNavItem.label
                    navController.navigate(bottomNavItem.label)
                },
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = bottomNavItem.icon,
                        contentDescription = bottomNavItem.label)
                },
                label = {
                    Text(text = bottomNavItem.label)
                },
                alwaysShowLabel = true,
                enabled = true
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: Painter)

@Composable
fun prepareBottomNav(): List<BottomNavItem> {
    val bottomNavItemsList = arrayListOf<BottomNavItem>()

    bottomNavItemsList.add(
        BottomNavItem(
            label = "Home",
            icon = painterResource(id = R.drawable.home)
        )
    )
    bottomNavItemsList.add(
        BottomNavItem(
            label = "Profile",
            icon = painterResource(id = R.drawable.user)
        )
    )
    bottomNavItemsList.add(
        BottomNavItem(
            label = "Settings",
            icon = painterResource(id = R.drawable.settings)
        )
    )

    return bottomNavItemsList
}