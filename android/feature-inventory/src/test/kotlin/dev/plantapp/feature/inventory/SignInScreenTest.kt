package dev.plantapp.feature.inventory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric Compose tests for the stateless email-OTP sign-in screen (3c-ui). Driven directly
 *  with hoisted state + callback spies — no emulator, no ViewModel. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h2000dp")
class SignInScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `enter email and tap send invokes onRequestCode`() {
        var captured: String? = null
        composeRule.setContent {
            SignInScreen(
                codeSent = false,
                error = null,
                onRequestCode = { captured = it },
                onVerify = { _, _ -> },
            )
        }
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_EMAIL).performTextInput("a@b.test")
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_SEND_CODE_BUTTON).performClick()
        assertEquals("a@b.test", captured)
    }

    @Test
    fun `with codeSent, enter code and tap verify invokes onVerify`() {
        var capturedEmail: String? = null
        var capturedCode: String? = null
        composeRule.setContent {
            SignInScreen(
                codeSent = true,
                error = null,
                onRequestCode = {},
                onVerify = { email, code -> capturedEmail = email; capturedCode = code },
            )
        }
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_EMAIL).performTextInput("a@b.test")
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_CODE).performTextInput("123456")
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_VERIFY_BUTTON).performClick()
        assertEquals("a@b.test", capturedEmail)
        assertEquals("123456", capturedCode)
    }

    @Test
    fun `send button is disabled while the email is blank`() {
        composeRule.setContent {
            SignInScreen(codeSent = false, error = null, onRequestCode = {}, onVerify = { _, _ -> })
        }
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_SEND_CODE_BUTTON).assertIsNotEnabled()
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_EMAIL).performTextInput("a@b.test")
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_SEND_CODE_BUTTON).assertIsEnabled()
    }

    @Test
    fun `busy disables the send button even with an email entered`() {
        composeRule.setContent {
            SignInScreen(codeSent = false, error = null, busy = true, onRequestCode = {}, onVerify = { _, _ -> })
        }
        composeRule.onNodeWithTag(InventoryTestTags.FIELD_SIGNIN_EMAIL).performTextInput("a@b.test")
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_SEND_CODE_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun `error is shown`() {
        composeRule.setContent {
            SignInScreen(
                codeSent = true,
                error = "Invalid code",
                onRequestCode = {},
                onVerify = { _, _ -> },
            )
        }
        composeRule.onNodeWithTag(InventoryTestTags.SIGNIN_ERROR).assertIsDisplayed()
        composeRule.onNodeWithText("Invalid code").assertIsDisplayed()
    }
}
