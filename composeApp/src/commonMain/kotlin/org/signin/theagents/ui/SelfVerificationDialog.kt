package org.signin.theagents.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.verification.SelfVerificationMethod


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfVerificationDialog(
    methods: Set<SelfVerificationMethod>,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(onDismissRequest) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    text = "Verify this device"
                )
                val (selectedMethod, setSelectedMethod) = remember { mutableStateOf<SelfVerificationMethod?>(null) }
                if (selectedMethod == null) {
                    methods.forEach { method ->
                        when (method) {
                            is SelfVerificationMethod.AesHmacSha2RecoveryKey -> ListItem(
                                headlineContent = { Text("Verify with recovery key") },
                                leadingContent = { Icon(Icons.Outlined.Key, "key") },
                                modifier = Modifier.clickable { setSelectedMethod(method) }
                            )

                            is SelfVerificationMethod.AesHmacSha2RecoveryKeyWithPbkdf2Passphrase -> ListItem(
                                headlineContent = { Text("Verify with recovery passphrase") },
                                leadingContent = { Icon(Icons.Outlined.Password, "passphrase") },
                                modifier = Modifier.clickable { setSelectedMethod(selectedMethod) }
                            )

                            is SelfVerificationMethod.CrossSignedDeviceVerification -> ListItem(
                                headlineContent = { Text("Verify with another device") },
                                leadingContent = { Icon(Icons.Outlined.Devices, "another device") },
                                modifier = Modifier.clickable { setSelectedMethod(selectedMethod) }
                            )
                        }
                    }
                } else {
                    var failure by remember { mutableStateOf(false) }
                    var inputVisibility by remember { mutableStateOf(false) }
                    var progress by remember { mutableStateOf(false) }
                    var action by remember { mutableStateOf<suspend () -> Unit>({}) }
                    when (selectedMethod) {
                        is SelfVerificationMethod.AesHmacSha2RecoveryKey -> {
                            var key by remember { mutableStateOf("") }
                            action = {
                                progress = true
                                selectedMethod.verify(key)
                                    .onSuccess { onDismissRequest() }
                                    .onFailure {
                                        progress = false
                                        failure = true
                                    }
                            }
                            OutlinedTextField(
                                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                                value = key,
                                onValueChange = { key = it },
                                label = { Text("Key") },
                                isError = failure,
                                visualTransformation = if (inputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password
                                ),
                                trailingIcon = {
                                    IconButton(
                                        modifier = Modifier.size(24.dp),
                                        onClick = { inputVisibility = !inputVisibility }
                                    ) {
                                        Icon(
                                            if (inputVisibility) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = if (inputVisibility) "hide" else "show"
                                        )
                                    }
                                }
                            )
                        }

                        is SelfVerificationMethod.AesHmacSha2RecoveryKeyWithPbkdf2Passphrase -> {
                            var passphrase by remember { mutableStateOf("") }
                            action = {
                                progress = true
                                selectedMethod.verify(passphrase)
                                    .onSuccess { onDismissRequest() }
                                    .onFailure {
                                        failure = true
                                        progress = false
                                    }
                            }
                            OutlinedTextField(
                                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                                value = passphrase,
                                onValueChange = { passphrase = it },
                                label = { Text("Passphrase") },
                                isError = failure,
                                visualTransformation = if (inputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password
                                ),
                                trailingIcon = {
                                    IconButton(
                                        modifier = Modifier.size(24.dp),
                                        onClick = { inputVisibility = !inputVisibility }
                                    ) {
                                        Icon(
                                            if (inputVisibility) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = if (inputVisibility) "hide" else "show"
                                        )
                                    }
                                }
                            )
                        }

                        is SelfVerificationMethod.CrossSignedDeviceVerification -> {
                            //TODO
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                                text = "Not implemented"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onDismissRequest() }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch { action() }
                            },
                            enabled = !progress
                        ) {
                            if (progress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 1.dp
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            }
                            Text("Verify")
                        }
                    }
                }
            }
        }
    }
}