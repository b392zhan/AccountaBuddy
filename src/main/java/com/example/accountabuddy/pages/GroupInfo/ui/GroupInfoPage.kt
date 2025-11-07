package com.example.accountabuddy.pages.GroupInfo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.accountabuddy.pages.Navigation.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoPage(navController: NavController, groupName: String, groupInfoViewModel: GroupInfoViewModel = viewModel()
) {
    LaunchedEffect(groupName) {
        groupInfoViewModel.loadGroupMembers(groupName)
        groupInfoViewModel.loadGroupActivities(groupName)
        groupInfoViewModel.loadGroupStreak(groupName)
    }
    val members = groupInfoViewModel.members
    val activities = groupInfoViewModel.activities
    var selectedMember by remember { mutableStateOf<String?>(null) }

    if (selectedMember != null) {
        AlertDialog(
            onDismissRequest = { selectedMember = null },
            title = { Text("Remove Member") },
            text = { Text("Are you sure you want to remove $selectedMember?") },
            confirmButton = {
                TextButton(onClick = {
                    groupInfoViewModel.removeMember(groupName, selectedMember!!)
                    selectedMember = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMember = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController, "groupInfo") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF2B3A67)
                    )
                }
                Text(
                    text = groupName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B3A67)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Streak: ${groupInfoViewModel.streak ?: "..."} day(s)",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2B3A67),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            SectionBox(title = "Members") {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    members.forEach { memberEmail ->
                        MemberItem(
                            name = memberEmail,
                            borderColor = Color(0xFF4285F4),
                            onClick = { selectedMember = memberEmail }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionBox(title = "Activity") {
                if (activities.isEmpty()) {
                    Text(
                        text = "No activity data yet",
                        color = Color(0xFF2B3A67),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                } else {
                    LazyColumn {
                        items(activities) { activityMap ->
                            val activityName = activityMap["activityName"] as? String ?: "Unknown"
                            val duration = (activityMap["duration"] as? Long)?.toInt() ?: 0
                            val userEmail = activityMap["userEmail"] as? String ?: "Unknown"
                            val timestamp = activityMap["timestamp"]

                            ActivityItem(
                                activityName = activityName,
                                duration = duration,
                                userEmail = userEmail,
                                timestamp = timestamp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SectionBox(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B3A67)
        )
        content()
    }
}

@Composable
fun MemberItem(name: String, borderColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5))
                .border(3.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = name,
                tint = Color(0xFF757575)
            )
        }
        Text(text = name, color = Color(0xFF2B3A67), fontSize = 14.sp)
    }
}

@Composable
fun ActivityItem(
    activityName: String,
    duration: Int,
    userEmail: String,
    timestamp: Any?
) {
    val formattedTime = when (timestamp) {
        is com.google.firebase.Timestamp -> {
            val date = timestamp.toDate()
            android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", date)
        }
        is String -> timestamp
        else -> "Unknown time"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF0F4FF), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Activity: $activityName",
            color = Color(0xFF2B3A67),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Duration: $duration minutes",
            color = Color(0xFF757575)
        )
        Text(
            text = "Logged by: $userEmail",
            color = Color(0xFF757575)
        )
        Text(
            text = "Time: $formattedTime",
            color = Color(0xFF757575)
        )
    }
}