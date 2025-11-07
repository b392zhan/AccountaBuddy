package com.example.accountabuddy.pages.AddGroup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.accountabuddy.pages.Navigation.BottomNavigationBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupPage(navController: NavController, viewModel: AddGroupViewModel = viewModel()) {
    var groupName by remember { mutableStateOf("") }
    var membersInput by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController, "createGroup") }
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
                    text = "Groups",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B3A67)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Create New Group",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B3A67)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter the name of the new group",
                    fontSize = 16.sp,
                    color = Color(0xFF2B3A67)
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF0F4FF),
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color(0xFF2B3A67)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Add Group Members",
                        fontSize = 16.sp,
                        color = Color(0xFF2B3A67)
                    )
                    Text(
                        text = "Optional",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                OutlinedTextField(
                    value = membersInput,
                    onValueChange = { membersInput = it },
                    label = { Text("Enter name or email", color = Color(0xFF757575)) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF0F4FF),
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color(0xFF2B3A67)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Separate multiple emails with commas",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (groupName.isBlank()) {
                                errorMessage = "Group name cannot be empty!"
                                coroutineScope.launch {
                                    delay(5000)
                                    errorMessage = ""
                                }
                            } else {
                                viewModel.createGroup(groupName, membersInput) { success, message ->
                                    if (success) {
                                        groupName = ""
                                        membersInput = ""
                                        showConfirmation = true
                                        coroutineScope.launch {
                                            delay(5000)
                                            showConfirmation = false
                                        }
                                        errorMessage = ""
                                    } else {
                                        errorMessage = message
                                        coroutineScope.launch {
                                            delay(5000)
                                            errorMessage = ""
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(text = "Create", color = Color.White, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (showConfirmation) {
                    Text(
                        text = "Group Created Successfully!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}