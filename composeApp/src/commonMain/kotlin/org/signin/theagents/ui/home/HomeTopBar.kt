package org.signin.theagents.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.verification
import net.folivo.trixnity.client.verification.VerificationService
import net.folivo.trixnity.clientserverapi.client.SyncState
import org.signin.theagents.LocalAppScope
import org.signin.theagents.service.SessionManager
import org.signin.theagents.ui.Avatar
import org.signin.theagents.ui.SelfVerificationDialog
import org.signin.theagents.ui.auth.LoginScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(sessionManager: SessionManager, syncState: SyncState) {
    val client = sessionManager.getClient()
    val navigator = LocalNavigator.currentOrThrow
    val appScope = LocalAppScope.current
    CenterAlignedTopAppBar(
        title = {
            val stateName = when (syncState) {
                SyncState.INITIAL_SYNC -> "Connecting..."
                SyncState.STARTED, SyncState.RUNNING, SyncState.TIMEOUT -> "The Agents"
                SyncState.ERROR -> "No connection..."
                SyncState.STOPPING -> "Stopping"
                SyncState.STOPPED -> "Stopped"
            }
            Text(
                stateName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            var showLogoutDialog by remember { mutableStateOf(false) }
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    text = { Text("Do you want to logout?") },
                    confirmButton = {
                        Button(onClick = {
                            showLogoutDialog = false
                            appScope.launch {
                                delay(50) //TODO fix racing on state text
                                sessionManager.logout()
                            }
                            navigator.replaceAll(LoginScreen())
                        }) {
                            Text("Logout")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showLogoutDialog = false
                        }) {
                            Text("Cancel")
                        }
                    },
                )
            }
            IconButton(onClick = { showLogoutDialog = true }) {
                ProfileAvatar(client = client)
            }
        },
        actions = {
            val verificationState by client.verification
                .getSelfVerificationMethods()
                .filter { it !is VerificationService.SelfVerificationMethods.PreconditionsNotMet }
                .collectAsState(null)
            val verification = verificationState
            if (verification is VerificationService.SelfVerificationMethods.CrossSigningEnabled) {
                var showVerificationDialog by remember { mutableStateOf(false) }
                IconButton(modifier = Modifier.alpha(0f),
                    onClick = { showVerificationDialog = false }
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Outlined.Key,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = null
                    )
                }
                if (showVerificationDialog) {
                    SelfVerificationDialog(
                        verification.methods,
                        onDismissRequest = { showVerificationDialog = false }
                    )
                }
            }
/*
            var isDark by LocalThemeIsDark.current
            IconButton(onClick = { isDark = !isDark }) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = null
                )
            }*/
        }
    )
}

@Composable
private fun ProfileAvatar(
    modifier: Modifier = Modifier,
    client: MatrixClient,
    textSize: TextUnit = 18.sp
) {
    val urlAndName by combine(client.avatarUrl, client.displayName) { u, n -> u to n }
        .collectAsState(null to null)
    val (url, name) = urlAndName
    Avatar(
        modifier = modifier,
        client = client,
        id = client.userId.full,
        url = url,
        name = name.orEmpty(),
        textSize = textSize
    )
}