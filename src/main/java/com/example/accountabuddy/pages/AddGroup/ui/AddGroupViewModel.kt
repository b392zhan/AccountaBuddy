package com.example.accountabuddy.pages.AddGroup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddGroupViewModel(
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private var auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    // For testing purposes
    fun setDependencies(firestore: FirebaseFirestore, auth: FirebaseAuth) {
        this.firestore = firestore
        this.auth = auth
    }

    // creates a group, for the group members, multiple emails have to be seprerated by commas
    fun createGroup(groupName: String, membersInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            try {
                if (groupName.isBlank()) {
                    withContext(mainDispatcher) {
                        onResult(false, "Group name cannot be empty")
                    }
                    return@launch
                }

                //get the members from the comma seperated string, turn into a list
                val membersList = membersInput
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toMutableList()

                // add current user to the members list for the group
                auth.currentUser?.email?.let { currentUserEmail ->
                    if (!membersList.contains(currentUserEmail)) {
                        membersList.add(currentUserEmail)
                    }
                }

                // actual data to put into firestore
                val groupData = hashMapOf(
                    "name" to groupName,
                    "members" to membersList
                )

                //store data
                try {
                    firestore.collection("groups")
                        .add(groupData)
                        .await()
                    withContext(mainDispatcher) {
                        onResult(true, "Group created successfully!")
                    }
                } catch (e: Exception) {
                    withContext(mainDispatcher) {
                        onResult(false, e.message ?: "Firebase Error")
                    }
                }
            } catch (e: Exception) {
                withContext(mainDispatcher) {
                    onResult(false, e.message ?: "Error creating group.")
                }
            }
        }
    }
}
