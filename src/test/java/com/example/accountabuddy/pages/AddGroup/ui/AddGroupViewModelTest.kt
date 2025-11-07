package com.example.accountabuddy.pages.AddGroup.ui

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
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AddGroupViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Mock
    private lateinit var auth: FirebaseAuth

    @Mock
    private lateinit var currentUser: FirebaseUser

    @Mock
    private lateinit var groupsCollection: CollectionReference

    @Mock
    private lateinit var documentReference: DocumentReference

    private lateinit var viewModel: AddGroupViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(firestore.collection("groups")).thenReturn(groupsCollection)
        whenever(auth.currentUser).thenReturn(currentUser)
        whenever(currentUser.email).thenReturn("test@example.com")
        whenever(groupsCollection.add(any())).thenReturn(Tasks.forResult(documentReference))

        viewModel = AddGroupViewModel(
            firestore = firestore,
            auth = auth,
            ioDispatcher = testDispatcher,
            mainDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createGroup with valid data should succeed`() = runTest {
        val groupName = "Test Group"
        val membersInput = "member1@example.com, member2@example.com"
        val expectedMembers = listOf("member1@example.com", "member2@example.com", "test@example.com")
        var success = false
        var message = ""

        val groupDataCaptor = argumentCaptor<HashMap<String, Any>>()

        viewModel.createGroup(groupName, membersInput) { isSuccess, resultMessage ->
            success = isSuccess
            message = resultMessage
        }
        testScheduler.advanceUntilIdle()

        verify(groupsCollection).add(groupDataCaptor.capture())
        val capturedData = groupDataCaptor.firstValue
        assertTrue(success)
        assertEquals("Group created successfully!", message)
        assertEquals(groupName, capturedData["name"])
        @Suppress("UNCHECKED_CAST")
        assertTrue((capturedData["members"] as List<String>).containsAll(expectedMembers))
    }

    @Test
    fun `createGroup should include current user in members list`() = runTest {
        val groupName = "Test Group"
        val membersInput = "member1@example.com, member2@example.com"
        var success = false
        var message = ""

        val groupDataCaptor = argumentCaptor<HashMap<String, Any>>()

        viewModel.createGroup(groupName, membersInput) { isSuccess, resultMessage ->
            success = isSuccess
            message = resultMessage
        }
        testScheduler.advanceUntilIdle()

        verify(groupsCollection).add(groupDataCaptor.capture())
        val capturedData = groupDataCaptor.firstValue
        @Suppress("UNCHECKED_CAST")
        val members = capturedData["members"] as List<String>
        assertTrue(members.contains("test@example.com"))
        assertTrue(success)
        assertEquals("Group created successfully!", message)
    }

    @Test
    fun `createGroup with empty group name should fail`() = runTest {
        val groupName = ""
        val membersInput = "member1@example.com"
        var success = false
        var message = ""

        viewModel.createGroup(groupName, membersInput) { isSuccess, resultMessage ->
            success = isSuccess
            message = resultMessage
        }
        testScheduler.advanceUntilIdle()

        verify(groupsCollection, never()).add(any())
        assertFalse(success)
        assertEquals("Group name cannot be empty", message)
    }

    @Test
    fun `createGroup with empty members should still include current user`() = runTest {
        val groupName = "Test Group"
        val membersInput = ""
        var success = false
        var message = ""

        val groupDataCaptor = argumentCaptor<HashMap<String, Any>>()

        viewModel.createGroup(groupName, membersInput) { isSuccess, resultMessage ->
            success = isSuccess
            message = resultMessage
        }
        testScheduler.advanceUntilIdle()

        verify(groupsCollection).add(groupDataCaptor.capture())
        val capturedData = groupDataCaptor.firstValue
        @Suppress("UNCHECKED_CAST")
        val members = capturedData["members"] as List<String>
        assertEquals(listOf("test@example.com"), members)
        assertTrue(success)
        assertEquals("Group created successfully!", message)
    }

    @Test
    fun `createGroup should handle Firebase failure`() = runTest {
        val groupName = "Test Group"
        val membersInput = "member1@example.com"
        var success = false
        var message = ""

        whenever(groupsCollection.add(any())).thenReturn(Tasks.forException(RuntimeException("Firebase Error")))

        viewModel.createGroup(groupName, membersInput) { isSuccess, resultMessage ->
            success = isSuccess
            message = resultMessage
        }
        testScheduler.advanceUntilIdle()

        assertFalse(success)
        assertEquals("Firebase Error", message)
    }

    @Test
    fun `createGroup should not add duplicate current user`() = runTest {
        val groupName = "Test Group"
        val membersInput = "test@example.com, member1@example.com"
        var success = false
        var message = ""

        val groupDataCaptor = argumentCaptor<HashMap<String, Any>>()

        viewModel.createGroup(groupName, membersInput) { isSuccess, resultMessage ->
            success = isSuccess
            message = resultMessage
        }
        testScheduler.advanceUntilIdle()

        verify(groupsCollection).add(groupDataCaptor.capture())
        val capturedData = groupDataCaptor.firstValue
        @Suppress("UNCHECKED_CAST")
        val members = capturedData["members"] as List<String>
        assertEquals(2, members.size)
        assertTrue(members.contains("test@example.com"))
        assertTrue(members.contains("member1@example.com"))
        assertTrue(success)
        assertEquals("Group created successfully!", message)
    }

    @Test
    fun `createGroup should handle whitespace in member emails`() = runTest {
        val groupName = "Test Group"
        val membersInput = "  member1@example.com  ,  member2@example.com  "
        var success = false
        var message = ""

        val groupDataCaptor = argumentCaptor<HashMap<String, Any>>()

        viewModel.createGroup(groupName, membersInput) { isSuccess, resultMessage ->
            success = isSuccess
            message = resultMessage
        }
        testScheduler.advanceUntilIdle()

        verify(groupsCollection).add(groupDataCaptor.capture())
        val capturedData = groupDataCaptor.firstValue
        @Suppress("UNCHECKED_CAST")
        val members = capturedData["members"] as List<String>
        assertEquals(3, members.size)
        assertTrue(members.contains("member1@example.com"))
        assertTrue(members.contains("member2@example.com"))
        assertTrue(members.contains("test@example.com"))
        assertTrue(success)
        assertEquals("Group created successfully!", message)
    }
} 