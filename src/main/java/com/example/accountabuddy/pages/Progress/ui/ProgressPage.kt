package com.example.accountabuddy.pages.Progress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.accountabuddy.pages.Navigation.BottomNavigationBar
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountabuddy.pages.LogActivity.ui.LogActivityViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

// we're gonna use duration in minutes
data class WorkoutActivity(
    val id: String,
    val title: String,
    val duration: Int,
    val date: LocalDate
)

@Composable
fun ProgressPage(navController: NavController, viewModel: ProgressViewModel = viewModel()) {
    val today = LocalDate.now()
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }
    var streak by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getUserStreak(
            onResult = { streak = it },
            onError = { errorMessage = it.message }
        )
    }

    // the activities
    val activitiesMap = viewModel.activitiesMap

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController, "progress") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            Text(
                text = when {
                    errorMessage != null -> "Error: ${errorMessage!!}"
                    streak == null -> "Streak: Loading..."
                    else -> "Streak: $streak day(s)"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            // calender here
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // header of calender, with month nav
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentYearMonth = currentYearMonth.minusMonths(1)
                    }) {
                        Text("<", fontSize = 20.sp, color = Color.Black)
                    }

                    Text(
                        text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        fontSize = 24.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = {
                        currentYearMonth = currentYearMonth.plusMonths(1)
                    }) {
                        Text(">", fontSize = 20.sp, color = Color.Black)
                    }
                }

                // sunday-sat header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (day in listOf("S", "M", "T", "W", "T", "F", "S")) {
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // grid of calendar days
                val daysInMonth = getDaysInMonth(currentYearMonth)
                val rows = daysInMonth.chunked(7)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    rows.forEach { weekDays ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekDays.forEach { day ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CalendarDayItem(
                                        day = day,
                                        isSelected = selectedDate == day.date,
                                        hasActivity = activitiesMap.containsKey(day.date),
                                        onDayClick = { selectedDate = day.date }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // section to show activities for each day
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "Activities for ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    // get the activities for the current selected day
                    val activitiesForDate = activitiesMap[selectedDate] ?: emptyList()
                    if (activitiesForDate.isNotEmpty()) {
                        ActivityList(
                            activities = activitiesForDate,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        EmptyActivityView(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    day: CalendarDay,
    isSelected: Boolean,
    hasActivity: Boolean,
    onDayClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable(onClick = onDayClick)
            .then(
                when {
                    isSelected -> Modifier.background(Color(0xFF2196F3), CircleShape)
                    day.isToday -> Modifier.border(2.dp, Color(0xFF2196F3), CircleShape)
                    else -> Modifier
                }
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.White
                    !day.isCurrentMonth -> Color.Gray
                    else -> Color.Black
                },
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            if (hasActivity) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) Color.White else Color(0xFF4CAF50),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun ActivityList(activities: List<WorkoutActivity>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        items(activities) { activity ->
            ActivityCard(activity = activity)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ActivityCard(activity: WorkoutActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "${activity.duration} min",
                    fontSize = 16.sp,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun EmptyActivityView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No activities logged for this day",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap 'Add Item' to log activity",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// get the days in the displayed month
private fun getDaysInMonth(yearMonth: YearMonth): List<CalendarDay> {
    val today = LocalDate.now()
    val firstOfMonth = yearMonth.atDay(1)
    val lastOfMonth = yearMonth.atEndOfMonth()
    val firstDate = firstOfMonth.minusDays(firstOfMonth.dayOfWeek.value.toLong() % 7)
    val lastDate = lastOfMonth.plusDays(6 - lastOfMonth.dayOfWeek.value.toLong() % 7)
    val days = mutableListOf<CalendarDay>()
    var currentDate = firstDate

    while (!currentDate.isAfter(lastDate)) {
        days.add(
            CalendarDay(
                date = currentDate,
                isCurrentMonth = currentDate.month == yearMonth.month,
                isToday = currentDate.isEqual(today)
            )
        )
        currentDate = currentDate.plusDays(1)
    }
    return days
}