package com.example.accountabuddy.pages.LogActivity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.accountabuddy.pages.Navigation.BottomNavigationBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogActivityPage(navController: NavController, viewModel: LogActivityViewModel = viewModel()) {
    var activityName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    // vars to show confirmation pop ups or error pop ups
    var showConfirmation by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController, "logActivity") }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log Activity",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    label = { Text("Activity Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (activityName.isBlank() || duration.isBlank()) {
                            showError = "Please fill in all fields."
                            coroutineScope.launch {
                                delay(3000)
                                showError = null
                            }
                        } else {
                            val durationValue = duration.toIntOrNull()
                            if (durationValue == null) {
                                showError = "Duration must be a valid number."
                                coroutineScope.launch {
                                    delay(3000)
                                    showError = null
                                }
                            } else {
                                viewModel.logActivity(
                                    activityName = activityName,
                                    durationMinutes = durationValue,
                                    onSuccess = {
                                        // it worked, clear the fields
                                        activityName = ""
                                        duration = ""
                                        showConfirmation = true
                                        coroutineScope.launch {
                                            delay(2000)
                                            showConfirmation = false
                                        }
                                    },
                                    onFailure = { e ->
                                        // error
                                        showError = "Error logging activity: ${e.message}"
                                        coroutineScope.launch {
                                            delay(3000)
                                            showError = null
                                        }
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Log Activity",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showConfirmation) {
                    Text(
                        text = "Activity Logged!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
                showError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
        }
    }
}
