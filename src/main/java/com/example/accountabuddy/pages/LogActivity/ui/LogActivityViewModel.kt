package com.example.accountabuddy.pages.LogActivity.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LogActivityViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // create activity record in firestore for the user
    fun logActivity(activityName: String, durationMinutes: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure(Exception("User is not logged in."))
            return
        }

        val activityData = hashMapOf(
            "userId" to currentUser.uid,
            "userEmail" to currentUser.email,
            "activityName" to activityName,
            "duration" to durationMinutes,
            "timestamp" to FieldValue.serverTimestamp()
        )

        //actually store the data
        firestore.collection("activities")
            .add(activityData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}