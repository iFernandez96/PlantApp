package dev.plantapp.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.plantapp.designsystem.GlassCard

/** Stateless email-OTP sign-in (D-05), Garden Hearth skin. The user enters an email and asks
 *  for a one-time code ([onRequestCode]); once [codeSent], a code field + sign-in button
 *  ([onVerify]) appear. The screen owns only its email/code text; [codeSent]/[error]/[busy]
 *  are hoisted from the ViewModel ([busy] disables the buttons while a request is in flight). */
@Composable
fun SignInScreen(
    codeSent: Boolean,
    error: String?,
    busy: Boolean = false,
    onRequestCode: (email: String) -> Unit,
    onVerify: (email: String, code: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text("Welcome", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "We'll email you a one-time code — no password to remember.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.FIELD_SIGNIN_EMAIL),
                )
                Button(
                    onClick = { onRequestCode(email.trim()) },
                    enabled = email.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.SIGNIN_SEND_CODE_BUTTON),
                ) {
                    Text(if (busy && !codeSent) "Sending…" else "Send me a code")
                }

                if (codeSent) {
                    Text(
                        text = "We emailed a code to ${email.trim()}. It can take a minute to arrive.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("6-digit code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.FIELD_SIGNIN_CODE),
                    )
                    Button(
                        onClick = { onVerify(email.trim(), code.trim()) },
                        enabled = code.isNotBlank() && !busy,
                        modifier = Modifier.fillMaxWidth().testTag(InventoryTestTags.SIGNIN_VERIFY_BUTTON),
                    ) {
                        Text("Sign in")
                    }
                }

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(InventoryTestTags.SIGNIN_ERROR),
                    )
                }
            }
        }
    }
}
