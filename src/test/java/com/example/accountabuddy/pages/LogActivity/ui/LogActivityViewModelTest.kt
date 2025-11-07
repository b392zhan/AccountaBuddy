package com.example.accountabuddy.pages.LogActivity.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.junit.Assert.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LogActivityViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Mock
    private lateinit var auth: FirebaseAuth

    @Mock
    private lateinit var currentUser: FirebaseUser

    @Mock
    private lateinit var activitiesCollection: CollectionReference

    @Mock
    private lateinit var documentReference: DocumentReference

    private lateinit var viewModel: LogActivityViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(firestore.collection("activities")).thenReturn(activitiesCollection)
        whenever(auth.currentUser).thenReturn(currentUser)
        whenever(currentUser.uid).thenReturn("test-user-id")
        whenever(currentUser.email).thenReturn("test@example.com")

        viewModel = LogActivityViewModel(firestore, auth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `logActivity should successfully log activity when user is logged in`() = runTest {
        val activityName = "Test Activity"
        val durationMinutes = 30
        var successCallbackCalled = false
        var failureCallbackCalled = false

        var successListener: OnSuccessListener<DocumentReference>? = null
        var failureListener: OnFailureListener? = null

        val mockTask: Task<DocumentReference> = Mockito.mock(Task::class.java) as Task<DocumentReference>
        Mockito.`when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            successListener = invocation.arguments[0] as OnSuccessListener<DocumentReference>
            mockTask
        }
        Mockito.`when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            failureListener = invocation.arguments[0] as OnFailureListener
            mockTask
        }

        whenever(activitiesCollection.add(any())).thenReturn(mockTask)

        viewModel.logActivity(
            activityName = activityName,
            durationMinutes = durationMinutes,
            onSuccess = { successCallbackCalled = true },
            onFailure = { failureCallbackCalled = true }
        )

        verify(activitiesCollection).add(argThat { data ->
            @Suppress("UNCHECKED_CAST")
            val map = data as Map<String, Any?>
            map["userId"] == "test-user-id" &&
            map["userEmail"] == "test@example.com" &&
            map["activityName"] == activityName &&
            map["duration"] == durationMinutes &&
            map["timestamp"] != null
        })

        successListener?.onSuccess(documentReference)

        assertTrue(successCallbackCalled)
        assertFalse(failureCallbackCalled)
    }

    @Test
    fun `logActivity should fail when user is not logged in`() = runTest {
        val activityName = "Test Activity"
        val durationMinutes = 30
        var successCallbackCalled = false
        var failureCallbackCalled = false
        var failureException: Exception? = null

        whenever(auth.currentUser).thenReturn(null)

        viewModel.logActivity(
            activityName = activityName,
            durationMinutes = durationMinutes,
            onSuccess = { successCallbackCalled = true },
            onFailure = { exception -> 
                failureCallbackCalled = true
                failureException = exception
            }
        )

        assertFalse(successCallbackCalled)
        assertTrue(failureCallbackCalled)
        assertEquals("User is not logged in.", failureException?.message)
        verify(activitiesCollection, never()).add(any())
    }

    @Test
    fun `logActivity should handle Firestore failure`() = runTest {
        val activityName = "Test Activity"
        val durationMinutes = 30
        var successCallbackCalled = false
        var failureCallbackCalled = false
        var failureException: Exception? = null

        var successListener: OnSuccessListener<DocumentReference>? = null
        var failureListener: OnFailureListener? = null

        val mockTask: Task<DocumentReference> = Mockito.mock(Task::class.java) as Task<DocumentReference>
        Mockito.`when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            successListener = invocation.arguments[0] as OnSuccessListener<DocumentReference>
            mockTask
        }
        Mockito.`when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            failureListener = invocation.arguments[0] as OnFailureListener
            mockTask
        }

        whenever(activitiesCollection.add(any())).thenReturn(mockTask)

        viewModel.logActivity(
            activityName = activityName,
            durationMinutes = durationMinutes,
            onSuccess = { successCallbackCalled = true },
            onFailure = { e -> 
                failureCallbackCalled = true
                failureException = e
            }
        )

        val error = Exception("Firestore error")
        failureListener?.onFailure(error)

        assertFalse(successCallbackCalled)
        assertTrue(failureCallbackCalled)
        assertEquals("Firestore error", failureException?.message)
        verify(activitiesCollection).add(any())
    }
}