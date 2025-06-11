package org.signin.theagents.ui

import TheAgents.composeApp.BuildConfig
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.signin.theagents.service.SessionManager

class LoginScreen(private val initialUrl: String = BuildConfig.MATRIX_URL) : Screen, KoinComponent {


    @Composable
    override fun Content() {
        val sessionManager by inject<SessionManager>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var url by remember { mutableStateOf(initialUrl) }
        var loginToken by remember { mutableStateOf("") }

        Box(modifier = Modifier.fillMaxSize()) {
            WebView(
                url = initialUrl,
                onLoginTokenReceived = { token ->
                    loginToken = token
                    scope.launch {
                        try {
                            //sessionManager.loginWithToken(BuildConfig.MATRIX_URL, token)
                            sessionManager.loginWithToken("https://chat-int.finnomena.com", token)
                            // Navigate to next screen after successful login
                            // navigator.push(NextScreen())
                            println("Login success")
                        } catch (e: Exception) {
                            println("Login failed: ${e.message}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
expect fun WebView(
    url: String,
    onLoginTokenReceived: (String) -> Unit
)