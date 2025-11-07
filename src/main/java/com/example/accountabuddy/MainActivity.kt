package com.example.accountabuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.accountabuddy.pages.AddGroup.ui.AddGroupPage
import com.example.accountabuddy.pages.GroupInfo.ui.GroupInfoPage
import com.example.accountabuddy.ui.theme.AccountabuddyTheme
import com.example.accountabuddy.pages.Groups.ui.GroupsPage
import com.example.accountabuddy.pages.LogActivity.ui.LogActivityPage
import com.example.accountabuddy.pages.Progress.ui.ProgressPage
import com.example.accountabuddy.pages.SignUp.SignUpPage
import com.example.accountabuddy.pages.LogIn.LoginPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountabuddyTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("groups") { GroupsPage(navController) }
        composable("logActivity") { LogActivityPage(navController) }
        composable("progress") { ProgressPage(navController) }
        composable("signup") { SignUpPage(navController) }
        composable("login") { LoginPage(navController) }
        composable("groupInfo/{groupName}") { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Default Group"
            GroupInfoPage(navController, groupName) }
        composable("addGroup") { AddGroupPage(navController) }
    }
}
