package com.example.accountabuddy.pages.LogIn

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.accountabuddy.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("AccountaBuddy").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun loginPage_hasAllRequiredElements() {
        composeTestRule.onNodeWithText("AccountaBuddy").assertExists()
        composeTestRule.onNodeWithTag("email_input").assertExists()
        composeTestRule.onNodeWithTag("password_input").assertExists()
        composeTestRule.onNodeWithTag("login_button").assertExists()
        composeTestRule.onNodeWithTag("register_link").assertExists()
    }

    @Test
    fun loginPage_showsErrorWhenFieldsAreEmpty() {
        composeTestRule.onNodeWithTag("login_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Please fill in all fields.").assertExists()
    }

    @Test
    fun loginPage_acceptsEmailAndPasswordInput() {
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("test@example.com").assertExists()
    }

    @Test
    fun loginPage_navigatesToSignup() {
        composeTestRule.onNodeWithTag("register_link").performClick()
        composeTestRule.waitForIdle()
    }
} 