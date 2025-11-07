package com.example.accountabuddy.pages.Progress.ui

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.ZoneId

class ProgressViewModel(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _activitiesMap = mutableStateMapOf<LocalDate, List<WorkoutActivity>>()
    val activitiesMap: Map<LocalDate, List<WorkoutActivity>> = _activitiesMap

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("activities")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val activitiesList = snapshot.documents.mapNotNull { doc ->
                            val activityName = doc.getString("activityName") ?: return@mapNotNull null
                            val duration = doc.getLong("duration")?.toInt() ?: return@mapNotNull null
                            val timestamp = doc.getTimestamp("timestamp")?.toDate()
                            if (timestamp != null) {
                                val date = timestamp.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                WorkoutActivity(
                                    id = doc.id,
                                    title = activityName,
                                    duration = duration,
                                    date = date
                                )
                            } else {
                                null
                            }
                        }
                        // put the activities together based on timestamp
                        val grouped = activitiesList.groupBy { it.date }
                        _activitiesMap.clear()
                        _activitiesMap.putAll(grouped)
                    }
                }
        }
    }

    fun getUserStreak(onResult: (Int) -> Unit, onError: (Exception) -> Unit) {
        //get teh current user
        val currentUser = auth.currentUser ?: run {
            onError(Exception("User not logged in"))
            return
        }

        // the activities for the current user
        firestore.collection("activities")
            .whereEqualTo("userEmail", currentUser.email)
            .get()
            .addOnSuccessListener { snapshot ->
                val datesSet = snapshot.documents
                    .mapNotNull { it.getTimestamp("timestamp")?.toDate() }
                    .map { it.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }
                    .toSet()

                // calculate streak for the user
                var streak = 0
                // need the current time to calculate the streak
                var day = LocalDate.now()
                while (datesSet.contains(day)) {
                    streak++
                    day = day.minusDays(1)
                }
                onResult(streak)
            }
            .addOnFailureListener { e -> onError(e) }
    }
}