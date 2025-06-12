package org.signin.theagents.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.onEach
import net.folivo.trixnity.client.MatrixClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.signin.theagents.service.SessionManager

class HomeScreen : Screen, KoinComponent {
    @Composable
    override fun Content() {
        val sessionManager by inject<SessionManager>()
        val client = sessionManager.getClient()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            client.startSync()
            client.loginState.onEach { loginState ->
                when (loginState) {
                    MatrixClient.LoginState.LOGGED_OUT_SOFT -> {
                        // TODO only ask for password
                    }
                    MatrixClient.LoginState.LOGGED_OUT -> {
                        navigator.replaceAll(LoginScreen())
                    }
                    else -> {
                        Napier.d("Home screen - Login state: $loginState")
                    }
                }
            }.collect()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Matrix Chat!",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Logged in as: ${client.userId}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    // TODO: Implement logout functionality
                }
            ) {
                Text("Logout")
            }
        }
    }
}