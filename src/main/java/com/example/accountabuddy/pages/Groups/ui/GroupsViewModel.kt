package com.example.accountabuddy.pages.Groups.ui

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.example.accountabuddy.pages.Groups.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.ZoneId

class GroupsViewModel(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    val groups = mutableStateListOf<Group>()
    val groupStreaks = mutableStateMapOf<String, Int>()

    init {
        fetchGroupsForCurrentUser()
    }

    private fun fetchGroupsForCurrentUser() {
        // currnet users email
        val currentUserEmail = auth.currentUser?.email ?: return

        firestore.collection("groups")
            .whereArrayContains("members", currentUserEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.documents?.let { docs ->
                    groups.clear()
                    docs.forEach { doc ->
                        val name = doc.getString("name") ?: return@forEach
                        val members = doc.get("members") as? List<String> ?: emptyList()
                        val group = Group(name = name, members = members)
                        groups.add(group)
                        fetchGroupStreak(group)
                    }
                }
            }
    }

    fun fetchGroupStreak(group: Group) {
        // get activities for all group members
        firestore.collection("activities")
            .whereIn("userEmail", group.members)
            .get()
            .addOnSuccessListener { snapshot ->
                val dateToMembers = mutableMapOf<LocalDate, MutableSet<String>>()
                snapshot.documents.forEach { doc ->
                    val time = doc.getTimestamp("timestamp")?.toDate() ?: return@forEach
                    val email = doc.getString("userEmail") ?: return@forEach
                    val date = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                    dateToMembers.getOrPut(date) { mutableSetOf() }.add(email)
                }

                // calculate streak while all members have an activity logged
                var streak = 0
                // current time to calcualte streak
                var day = LocalDate.now()
                while (dateToMembers[day]?.containsAll(group.members) == true) {
                    streak++
                    day = day.minusDays(1)
                }
                groupStreaks[group.name] = streak
            }
            .addOnFailureListener {
                groupStreaks[group.name] = 0
            }
    }
}
