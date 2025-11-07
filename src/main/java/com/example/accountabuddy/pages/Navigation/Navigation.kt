package com.example.accountabuddy.pages.Navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 10.dp, end = 8.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationButton(
            label = "Groups",
            isSelected = currentRoute == "groups"
        ) {
            if (currentRoute != "groups") navController.navigate("groups")
        }
        NavigationButton(
            label = "Add Item",
            isSelected = currentRoute == "logActivity"
        ) {
            if (currentRoute != "logActivity") navController.navigate("logActivity")
        }
        NavigationButton(
            label = "Progress",
            isSelected = currentRoute == "progress"
        ) {
            if (currentRoute != "progress") navController.navigate("progress")
        }
        NavigationButton(
            label = "Log Out",
            isSelected = false
        ) {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
}

@Composable
fun NavigationButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 4.dp,
            end = 20.dp,
            bottom = 4.dp
        ),
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(64.dp)
            .widthIn(min = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }
}

