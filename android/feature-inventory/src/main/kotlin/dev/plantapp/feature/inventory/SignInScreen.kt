package dev.plantapp.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** Stateless email-OTP sign-in (D-05). The user enters an email and taps "Send code"
 *  ([onRequestCode]); once [codeSent], a code field + "Verify" button ([onVerify]) appear. The
 *  screen owns its email/code text; [codeSent]/[error] are hoisted from the ViewModel. */
@Composable
fun SignInScreen(
    codeSent: Boolean,
    error: String?,
    onRequestCode: (email: String) -> Unit,
    onVerify: (email: String, code: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Sign in", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.FIELD_SIGNIN_EMAIL),
        )
        Button(
            onClick = { if (email.isNotBlank()) onRequestCode(email.trim()) },
            modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.SIGNIN_SEND_CODE_BUTTON),
        ) {
            Text("Send code")
        }

        if (codeSent) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.FIELD_SIGNIN_CODE),
            )
            Button(
                onClick = { onVerify(email.trim(), code.trim()) },
                modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.SIGNIN_VERIFY_BUTTON),
            ) {
                Text("Verify")
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag(InventoryTestTags.SIGNIN_ERROR),
            )
        }
    }
}
