package com.example.accountabuddy.pages.Groups.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.accountabuddy.pages.Groups.Group
import com.example.accountabuddy.pages.Navigation.BottomNavigationBar

val BackgroundColor = Color(0xFFF5F5F5)
val PrimaryColor = Color(0xFF2B3A67)
val CardColor = Color.White

@Composable
fun GroupsPage(navController: NavController, viewModel: GroupsViewModel = viewModel()) {
    val groups = viewModel.groups

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = { BottomNavigationBar(navController = navController, "groups") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(innerPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = "Groups",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { navController.navigate("addGroup") },
                    modifier = Modifier.background(PrimaryColor, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Group",
                        tint = Color.White
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(groups) { group ->
                    GroupItem(
                        group = group,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun GroupItem(group: Group, navController: NavController, viewModel: GroupsViewModel = viewModel()) {
    val streak = viewModel.groupStreaks[group.name] ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(CardColor, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .clickable { navController.navigate("groupInfo/${group.name}") }
            .padding(16.dp)
    ) {
        Text(
            text = group.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Members: ${group.members.joinToString(", ")}", color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Streak: $streak day(s)", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
    }
}
