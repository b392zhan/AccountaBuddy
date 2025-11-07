package com.example.accountabuddy.pages.GroupInfo.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

class GroupInfoViewModel(
    private val firestore: FirebaseFirestore = Firebase.firestore
) : ViewModel() {
    var members by mutableStateOf(listOf<String>())
    var activities by mutableStateOf<List<Map<String, Any?>>>(emptyList())
    var streak by mutableStateOf<Int?>(null)

    fun loadGroupMembers(groupName: String) {
        viewModelScope.launch {
            try {
                val querySnapshot = firestore.collection("groups")
                    .whereEqualTo("name", groupName)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents.first()
                    val membersList = doc.get("members") as? List<String> ?: emptyList()
                    members = membersList
                } else {
                    members = emptyList()
                }
            } catch (e: Exception) {
                members = emptyList()
            }
        }
    }

    fun removeMember(groupName: String, memberEmail: String) {
        viewModelScope.launch {
            try {
                val querySnapshot = firestore.collection("groups")
                    .whereEqualTo("name", groupName)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val removeFrom = querySnapshot.documents.first().reference
                    removeFrom.update("members", FieldValue.arrayRemove(memberEmail))
                        .await()
                    members = members.filterNot { it == memberEmail }
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun loadGroupActivities(groupName: String) {
        viewModelScope.launch {
            try {
                val groupSnapshot = firestore.collection("groups")
                    .whereEqualTo("name", groupName)
                    .get()
                    .await()

                if (!groupSnapshot.isEmpty) {
                    val doc = groupSnapshot.documents.first()
                    val membersList = doc.get("members") as? List<String> ?: emptyList()

                    if (membersList.isEmpty()) {
                        activities = emptyList()
                        return@launch
                    }

                    val activitiesSnapshot = firestore.collection("activities")
                        .get()
                        .await()

                    val membersLowercase = membersList.map { it.lowercase().trim() }

                    val filteredActivities = activitiesSnapshot.documents.mapNotNull { document ->
                        val data = document.data ?: return@mapNotNull null
                        val userEmail = (data["userEmail"] as? String)?.lowercase()?.trim()

                        if (userEmail != null && membersLowercase.contains(userEmail)) {
                            data
                        } else {
                            null
                        }
                    }

                    val sortedActivities = filteredActivities.sortedByDescending { activityData ->
                        (activityData["timestamp"] as? com.google.firebase.Timestamp)?.toDate()
                    }

                    activities = sortedActivities
                } else {
                    activities = emptyList()
                }
            } catch (e: Exception) {
                activities = emptyList()
            }
        }
    }

    fun loadGroupStreak(groupName: String) {
        viewModelScope.launch {
            try {
                val groupSnap = firestore.collection("groups")
                    .whereEqualTo("name", groupName)
                    .get()
                    .await()

                if (groupSnap.isEmpty) {
                    streak = 0
                    return@launch
                }

                val membersList = groupSnap.documents.first().get("members") as? List<String> ?: emptyList()
                if (membersList.isEmpty()) {
                    streak = 0
                    return@launch
                }

                val snap = firestore.collection("activities")
                    .whereIn("userEmail", membersList)
                    .get()
                    .await()

                val dateToMembers = mutableMapOf<LocalDate, MutableSet<String>>()
                snap.documents.forEach { doc ->
                    val time = doc.getTimestamp("timestamp")?.toDate() ?: return@forEach
                    val email = doc.getString("userEmail") ?: return@forEach
                    val date = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    dateToMembers.getOrPut(date) { mutableSetOf() }.add(email)
                }

                var count = 0
                var day = LocalDate.now()
                while (dateToMembers[day]?.containsAll(membersList) == true) {
                    count++
                    day = day.minusDays(1)
                }
                streak = count
            } catch (e: Exception) {
                streak = 0
            }
        }
    }
}
