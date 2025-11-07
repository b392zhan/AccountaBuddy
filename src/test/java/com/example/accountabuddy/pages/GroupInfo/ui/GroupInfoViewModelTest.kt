package com.example.accountabuddy.pages.GroupInfo.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner.Silent
import org.mockito.kotlin.*
import org.junit.Assert.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Silent::class)
class GroupInfoViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Mock
    private lateinit var groupsCollection: CollectionReference

    @Mock
    private lateinit var activitiesCollection: CollectionReference

    @Mock
    private lateinit var querySnapshot: QuerySnapshot

    @Mock
    private lateinit var documentSnapshot: DocumentSnapshot

    @Mock
    private lateinit var documentReference: DocumentReference

    private lateinit var viewModel: GroupInfoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(firestore.collection("groups")).thenReturn(groupsCollection)
        whenever(firestore.collection("activities")).thenReturn(activitiesCollection)
        whenever(groupsCollection.whereEqualTo(eq("name"), any())).thenReturn(groupsCollection)
        whenever(groupsCollection.get()).thenReturn(Tasks.forResult(querySnapshot))
        whenever(querySnapshot.isEmpty).thenReturn(false)
        whenever(querySnapshot.documents).thenReturn(listOf(documentSnapshot))
        whenever(documentSnapshot.reference).thenReturn(documentReference)
        whenever(documentSnapshot.get("members")).thenReturn(listOf("member1@example.com", "member2@example.com"))

        viewModel = GroupInfoViewModel(firestore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadGroupMembers should update members list when group exists`() = runTest {
        val groupName = "Test Group"
        val expectedMembers = listOf("member1@example.com", "member2@example.com")

        viewModel.loadGroupMembers(groupName)
        testScheduler.advanceUntilIdle()

        assertEquals(expectedMembers, viewModel.members)
    }

    @Test
    fun `loadGroupMembers should set empty list when group does not exist`() = runTest {
        val groupName = "NonExistent Group"
        whenever(querySnapshot.isEmpty).thenReturn(true)

        viewModel.loadGroupMembers(groupName)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.members.isEmpty())
    }

    @Test
    fun `removeMember should update members list and call Firestore`() = runTest {
        val groupName = "Test Group"
        val memberEmail = "member1@example.com"
        val initialMembers = listOf("member1@example.com", "member2@example.com")
        val expectedMembers = listOf("member2@example.com")

        whenever(documentSnapshot.get("members")).thenReturn(initialMembers)
        whenever(documentReference.update(eq("members"), any())).thenReturn(Tasks.forResult(null))

        viewModel.loadGroupMembers(groupName)
        testScheduler.advanceUntilIdle()

        viewModel.removeMember(groupName, memberEmail)
        testScheduler.advanceUntilIdle()

        verify(documentReference).update(eq("members"), any())
        assertEquals(expectedMembers, viewModel.members)
    }

    @Test
    fun `loadGroupActivities should update activities list with sorted activities`() = runTest {
        val groupName = "Test Group"
        val currentTime = Date()
        val yesterday = Date(currentTime.time - 24 * 60 * 60 * 1000)

        val activity1 = mapOf(
            "userEmail" to "member1@example.com",
            "timestamp" to Timestamp(currentTime),
            "activity" to "Activity 1"
        )
        val activity2 = mapOf(
            "userEmail" to "member2@example.com",
            "timestamp" to Timestamp(yesterday),
            "activity" to "Activity 2"
        )

        val activitySnapshot1 = mock<DocumentSnapshot> { 
            whenever(it.data).thenReturn(activity1)
        }
        val activitySnapshot2 = mock<DocumentSnapshot> {
            whenever(it.data).thenReturn(activity2)
        }

        val activitiesQuerySnapshot = mock<QuerySnapshot> {
            whenever(it.documents).thenReturn(listOf(activitySnapshot1, activitySnapshot2))
            whenever(it.isEmpty).thenReturn(false)
        }

        whenever(activitiesCollection.get()).thenReturn(Tasks.forResult(activitiesQuerySnapshot))

        viewModel.loadGroupActivities(groupName)
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.activities.size)
        assertEquals(activity1, viewModel.activities[0])
        assertEquals(activity2, viewModel.activities[1])
    }

    @Test
    fun `loadGroupStreak should calculate correct streak when all members have activities`() = runTest {
        val groupName = "Test Group"
        val currentDate = LocalDate.now()
        val yesterday = currentDate.minusDays(1)

        val activity1 = mapOf(
            "userEmail" to "member1@example.com",
            "timestamp" to Timestamp(Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "activity" to "Activity 1"
        )
        val activity2 = mapOf(
            "userEmail" to "member2@example.com",
            "timestamp" to Timestamp(Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "activity" to "Activity 2"
        )
        val activity3 = mapOf(
            "userEmail" to "member1@example.com",
            "timestamp" to Timestamp(Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "activity" to "Activity 3"
        )
        val activity4 = mapOf(
            "userEmail" to "member2@example.com",
            "timestamp" to Timestamp(Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "activity" to "Activity 4"
        )

        val activitySnapshot1 = mock<DocumentSnapshot> { 
            whenever(it.getString("userEmail")).thenReturn(activity1["userEmail"] as String)
            whenever(it.getTimestamp("timestamp")).thenReturn(activity1["timestamp"] as Timestamp)
        }
        val activitySnapshot2 = mock<DocumentSnapshot> {
            whenever(it.getString("userEmail")).thenReturn(activity2["userEmail"] as String)
            whenever(it.getTimestamp("timestamp")).thenReturn(activity2["timestamp"] as Timestamp)
        }
        val activitySnapshot3 = mock<DocumentSnapshot> {
            whenever(it.getString("userEmail")).thenReturn(activity3["userEmail"] as String)
            whenever(it.getTimestamp("timestamp")).thenReturn(activity3["timestamp"] as Timestamp)
        }
        val activitySnapshot4 = mock<DocumentSnapshot> {
            whenever(it.getString("userEmail")).thenReturn(activity4["userEmail"] as String)
            whenever(it.getTimestamp("timestamp")).thenReturn(activity4["timestamp"] as Timestamp)
        }

        val activitiesQuerySnapshot = mock<QuerySnapshot> {
            whenever(it.documents).thenReturn(listOf(
                activitySnapshot1,
                activitySnapshot2,
                activitySnapshot3,
                activitySnapshot4
            ))
            whenever(it.isEmpty).thenReturn(false)
        }

        whenever(activitiesCollection.whereIn(eq("userEmail"), any())).thenReturn(activitiesCollection)
        whenever(activitiesCollection.get()).thenReturn(Tasks.forResult(activitiesQuerySnapshot))

        viewModel.loadGroupStreak(groupName)
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.streak)
    }

    @Test
    fun `loadGroupStreak should set streak to 0 when no activities exist`() = runTest {
        val groupName = "Test Group"
        whenever(querySnapshot.isEmpty).thenReturn(true)

        viewModel.loadGroupStreak(groupName)
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.streak)
    }

    @Test
    fun `loadGroupStreak should set streak to 0 when group does not exist`() = runTest {
        val groupName = "NonExistent Group"
        whenever(querySnapshot.isEmpty).thenReturn(true)

        viewModel.loadGroupStreak(groupName)
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.streak)
    }
} 