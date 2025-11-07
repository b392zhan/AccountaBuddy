package com.example.accountabuddy.pages.Progress.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ProgressViewModelTest {

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
    private lateinit var query: Query

    @Mock
    private lateinit var querySnapshot: QuerySnapshot

    @Mock
    private lateinit var documentSnapshot1: DocumentSnapshot

    @Mock
    private lateinit var documentSnapshot2: DocumentSnapshot

    @Mock
    private lateinit var documentSnapshot3: DocumentSnapshot

    private lateinit var viewModel: ProgressViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(firestore.collection("activities")).thenReturn(activitiesCollection)
        whenever(auth.currentUser).thenReturn(currentUser)
        whenever(currentUser.uid).thenReturn("test-user-id")
        whenever(currentUser.email).thenReturn("test@example.com")
        whenever(activitiesCollection.whereEqualTo(eq("userId"), any())).thenReturn(query)
        whenever(activitiesCollection.whereEqualTo(eq("userEmail"), any())).thenReturn(query)

        viewModel = ProgressViewModel(firestore, auth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getUserStreak should return correct streak when user has consecutive days`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val yesterdayDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val twoDaysAgoDate = Date.from(twoDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant())

        val todayTimestamp = Timestamp(todayDate)
        val yesterdayTimestamp = Timestamp(yesterdayDate)
        val twoDaysAgoTimestamp = Timestamp(twoDaysAgoDate)

        whenever(documentSnapshot1.getTimestamp("timestamp")).thenReturn(todayTimestamp)
        whenever(documentSnapshot2.getTimestamp("timestamp")).thenReturn(yesterdayTimestamp)
        whenever(documentSnapshot3.getTimestamp("timestamp")).thenReturn(twoDaysAgoTimestamp)

        whenever(querySnapshot.documents).thenReturn(listOf(documentSnapshot1, documentSnapshot2, documentSnapshot3))

        var successListener: OnSuccessListener<QuerySnapshot>? = null
        var failureListener: OnFailureListener? = null

        val mockTask: Task<QuerySnapshot> = Mockito.mock(Task::class.java) as Task<QuerySnapshot>
        Mockito.`when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            successListener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
            mockTask
        }
        Mockito.`when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            failureListener = invocation.arguments[0] as OnFailureListener
            mockTask
        }

        whenever(query.get()).thenReturn(mockTask)

        var resultStreak = -1
        var error: Exception? = null

        viewModel.getUserStreak(
            onResult = { streak -> resultStreak = streak },
            onError = { e -> error = e }
        )

        successListener?.onSuccess(querySnapshot)

        assertEquals(3, resultStreak)
        assertNull(error)
    }

    @Test
    fun `getUserStreak should return 0 when user has no activities`() {
        whenever(querySnapshot.documents).thenReturn(emptyList())

        var successListener: OnSuccessListener<QuerySnapshot>? = null
        var failureListener: OnFailureListener? = null

        val mockTask: Task<QuerySnapshot> = Mockito.mock(Task::class.java) as Task<QuerySnapshot>
        Mockito.`when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            successListener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
            mockTask
        }
        Mockito.`when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            failureListener = invocation.arguments[0] as OnFailureListener
            mockTask
        }

        whenever(query.get()).thenReturn(mockTask)

        var resultStreak = -1
        var error: Exception? = null

        viewModel.getUserStreak(
            onResult = { streak -> resultStreak = streak },
            onError = { e -> error = e }
        )

        successListener?.onSuccess(querySnapshot)

        assertEquals(0, resultStreak)
        assertNull(error)
    }

    @Test
    fun `getUserStreak should return 0 when user has non-consecutive days`() {
        val today = LocalDate.now()
        val threeDaysAgo = today.minusDays(3)
        val fiveDaysAgo = today.minusDays(5)

        val todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val threeDaysAgoDate = Date.from(threeDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val fiveDaysAgoDate = Date.from(fiveDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant())

        val todayTimestamp = Timestamp(todayDate)
        val threeDaysAgoTimestamp = Timestamp(threeDaysAgoDate)
        val fiveDaysAgoTimestamp = Timestamp(fiveDaysAgoDate)

        whenever(documentSnapshot1.getTimestamp("timestamp")).thenReturn(todayTimestamp)
        whenever(documentSnapshot2.getTimestamp("timestamp")).thenReturn(threeDaysAgoTimestamp)
        whenever(documentSnapshot3.getTimestamp("timestamp")).thenReturn(fiveDaysAgoTimestamp)

        whenever(querySnapshot.documents).thenReturn(listOf(documentSnapshot1, documentSnapshot2, documentSnapshot3))

        var successListener: OnSuccessListener<QuerySnapshot>? = null
        var failureListener: OnFailureListener? = null

        val mockTask: Task<QuerySnapshot> = Mockito.mock(Task::class.java) as Task<QuerySnapshot>
        Mockito.`when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            successListener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
            mockTask
        }
        Mockito.`when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            failureListener = invocation.arguments[0] as OnFailureListener
            mockTask
        }

        whenever(query.get()).thenReturn(mockTask)

        var resultStreak = -1
        var error: Exception? = null

        viewModel.getUserStreak(
            onResult = { streak -> resultStreak = streak },
            onError = { e -> error = e }
        )

        successListener?.onSuccess(querySnapshot)

        assertEquals(1, resultStreak)
        assertNull(error)
    }

    @Test
    fun `getUserStreak should return error when user is not logged in`() {
        whenever(auth.currentUser).thenReturn(null)

        var resultStreak = -1
        var error: Exception? = null

        viewModel.getUserStreak(
            onResult = { streak -> resultStreak = streak },
            onError = { e -> error = e }
        )

        assertEquals(-1, resultStreak)
        assertNotNull(error)
        assertEquals("User not logged in", error?.message)
        verify(query, never()).get()
    }

    @Test
    fun `getUserStreak should handle Firestore failure`() {
        var successListener: OnSuccessListener<QuerySnapshot>? = null
        var failureListener: OnFailureListener? = null

        val mockTask: Task<QuerySnapshot> = Mockito.mock(Task::class.java) as Task<QuerySnapshot>
        Mockito.`when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            successListener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
            mockTask
        }
        Mockito.`when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            failureListener = invocation.arguments[0] as OnFailureListener
            mockTask
        }

        whenever(query.get()).thenReturn(mockTask)

        var resultStreak = -1
        var error: Exception? = null

        viewModel.getUserStreak(
            onResult = { streak -> resultStreak = streak },
            onError = { e -> error = e }
        )

        val firestoreError = Exception("Firestore error")
        failureListener?.onFailure(firestoreError)

        assertEquals(-1, resultStreak)
        assertNotNull(error)
        assertEquals("Firestore error", error?.message)
    }
} 