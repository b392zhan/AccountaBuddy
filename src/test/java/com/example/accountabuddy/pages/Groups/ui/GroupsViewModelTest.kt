package com.example.accountabuddy.pages.Groups.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.accountabuddy.pages.Groups.Group
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.*
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
import org.junit.runners.JUnit4
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GroupsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var groupsCollection: CollectionReference
    private lateinit var activitiesCollection: CollectionReference
    private lateinit var groupsQuery: Query
    private lateinit var activitiesQuery: Query
    private lateinit var viewModel: GroupsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        firestore = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        currentUser = mockk(relaxed = true)
        groupsCollection = mockk(relaxed = true)
        activitiesCollection = mockk(relaxed = true)
        groupsQuery = mockk(relaxed = true)
        activitiesQuery = mockk(relaxed = true)

        every { firestore.collection("groups") } returns groupsCollection
        every { firestore.collection("activities") } returns activitiesCollection
        every { auth.currentUser } returns currentUser
        every { currentUser.email } returns "test@example.com"
        every { groupsCollection.whereArrayContains("members", any()) } returns groupsQuery

        viewModel = GroupsViewModel(firestore, auth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchGroupsForCurrentUser should update groups list when successful`() {
        val members = listOf("test@example.com", "other@example.com")
        val groupName = "Test Group"
        val groupDocument = mockk<DocumentSnapshot>()
        
        every { groupDocument.getString("name") } returns groupName
        every { groupDocument.get("members") } returns members
        
        val groupsQuerySnapshot = mockk<QuerySnapshot>()
        every { groupsQuerySnapshot.documents } returns listOf(groupDocument)
        
        val listenerRegistration = mockk<ListenerRegistration>()
        every { groupsQuery.addSnapshotListener(any()) } returns listenerRegistration
        
        val activitiesQuerySnapshot = mockk<QuerySnapshot>()
        val activitiesTask = mockk<Task<QuerySnapshot>>()
        
        every { activitiesTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(activitiesQuerySnapshot)
            activitiesTask
        }
        every { activitiesTask.addOnFailureListener(any()) } returns activitiesTask
        
        every { activitiesCollection.whereIn("userEmail", any<List<String>>()) } returns activitiesQuery
        every { activitiesQuery.get() } returns activitiesTask
        
        val today = LocalDate.now()
        val todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val todayTimestamp = Timestamp(todayDate)
        
        val activityDocument1 = mockk<DocumentSnapshot>()
        val activityDocument2 = mockk<DocumentSnapshot>()
        
        every { activityDocument1.getTimestamp("timestamp") } returns todayTimestamp
        every { activityDocument1.getString("userEmail") } returns "test@example.com"
        every { activityDocument2.getTimestamp("timestamp") } returns todayTimestamp
        every { activityDocument2.getString("userEmail") } returns "other@example.com"
        
        every { activitiesQuerySnapshot.documents } returns listOf(activityDocument1, activityDocument2)
        
        var capturedListener: EventListener<QuerySnapshot>? = null
        every { groupsQuery.addSnapshotListener(any()) } answers {
            capturedListener = firstArg()
            listenerRegistration
        }
        
        viewModel = GroupsViewModel(firestore, auth)
        
        capturedListener?.onEvent(groupsQuerySnapshot, null)
        
        assertEquals(1, viewModel.groups.size)
        assertEquals(groupName, viewModel.groups[0].name)
        assertEquals(members, viewModel.groups[0].members)
        assertEquals(1, viewModel.groupStreaks[groupName])
    }

    @Test
    fun `fetchGroupStreak should calculate correct streak for consecutive days`() {
        val group = Group("Test Group", listOf("test@example.com"))
        val todayDoc = mockDocument(
            mapOf(
                "userEmail" to "test@example.com",
                "timestamp" to Timestamp(Date())
            )
        )
        val yesterdayDoc = mockDocument(
            mapOf(
                "userEmail" to "test@example.com",
                "timestamp" to Timestamp(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
            )
        )

        val activitiesQuerySnapshot = mockk<QuerySnapshot>()
        every { activitiesQuerySnapshot.documents } returns listOf(todayDoc, yesterdayDoc)

        val activitiesTask = mockk<Task<QuerySnapshot>>()
        every { activitiesTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(activitiesQuerySnapshot)
            activitiesTask
        }
        every { activitiesTask.addOnFailureListener(any()) } returns activitiesTask

        every { activitiesCollection.whereIn("userEmail", any<List<String>>()) } returns activitiesQuery
        every { activitiesQuery.get() } returns activitiesTask

        viewModel.groups.add(group)
        viewModel.fetchGroupStreak(group)

        verify { activitiesCollection.whereIn("userEmail", eq(listOf("test@example.com"))) }
        verify { activitiesQuery.get() }
        assertEquals(2, viewModel.groupStreaks[group.name])
    }

    @Test
    fun `fetchGroupStreak should calculate 0 streak when no activities exist`() {
        val group = Group("Test Group", listOf("test@example.com"))
        val activitiesQuerySnapshot = mockk<QuerySnapshot>()
        every { activitiesQuerySnapshot.documents } returns emptyList()

        val activitiesTask = mockk<Task<QuerySnapshot>>()
        every { activitiesTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(activitiesQuerySnapshot)
            activitiesTask
        }
        every { activitiesTask.addOnFailureListener(any()) } returns activitiesTask

        every { activitiesCollection.whereIn("userEmail", any<List<String>>()) } returns activitiesQuery
        every { activitiesQuery.get() } returns activitiesTask

        viewModel.groups.add(group)
        viewModel.fetchGroupStreak(group)

        verify { activitiesCollection.whereIn("userEmail", eq(listOf("test@example.com"))) }
        verify { activitiesQuery.get() }
        assertEquals(0, viewModel.groupStreaks[group.name])
    }

    @Test
    fun `fetchGroupStreak should handle Firestore failure`() {
        val group = Group("Test Group", listOf("test@example.com"))
        val activitiesTask = mockk<Task<QuerySnapshot>>()
        every { activitiesTask.addOnSuccessListener(any()) } returns activitiesTask
        every { activitiesTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(Exception("Firestore error"))
            activitiesTask
        }

        every { activitiesCollection.whereIn("userEmail", any<List<String>>()) } returns activitiesQuery
        every { activitiesQuery.get() } returns activitiesTask

        viewModel.groups.add(group)
        viewModel.fetchGroupStreak(group)

        verify { activitiesCollection.whereIn("userEmail", eq(listOf("test@example.com"))) }
        verify { activitiesQuery.get() }
        assertEquals(0, viewModel.groupStreaks[group.name])
    }

    @Test
    fun `fetchGroupsForCurrentUser should not update groups when user is not logged in`() {
        every { auth.currentUser } returns null

        viewModel = GroupsViewModel(firestore, auth)

        assertTrue(viewModel.groups.isEmpty())
    }

    private fun mockDocument(data: Map<String, Any>): DocumentSnapshot {
        return mockk {
            every { getString(any()) } answers { data[firstArg()] as? String }
            every { getTimestamp(any()) } answers { data[firstArg()] as? Timestamp }
        }
    }
} 