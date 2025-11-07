package com.example.accountabuddy.pages.SignUp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.accountabuddy.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Don't have an account? Register now").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("AccountaBuddy").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun signUpPage_hasAllRequiredElements() {
        composeTestRule.onNodeWithText("AccountaBuddy").assertExists()
        composeTestRule.onNodeWithTag("email_input").assertExists()
        composeTestRule.onNodeWithTag("password_input").assertExists()
        composeTestRule.onNodeWithTag("confirm_password_input").assertExists()
        composeTestRule.onNodeWithTag("signup_button").assertExists()
        composeTestRule.onNodeWithTag("login_link").assertExists()
    }

    @Test
    fun signUpPage_showsErrorWhenFieldsAreEmpty() {
        composeTestRule.onNodeWithTag("signup_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Please fill in all fields.").assertExists()
    }

    @Test
    fun signUpPage_showsErrorWhenPasswordsDontMatch() {
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")
        composeTestRule.onNodeWithTag("confirm_password_input").performTextInput("password456")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("signup_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Passwords do not match.").assertExists()
    }

    @Test
    fun signUpPage_acceptsInput() {
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")
        composeTestRule.onNodeWithTag("confirm_password_input").performTextInput("password123")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("test@example.com").assertExists()
    }

    @Test
    fun signUpPage_navigatesToLogin() {
        composeTestRule.onNodeWithTag("login_link").performClick()
        composeTestRule.waitForIdle()
    }
} 