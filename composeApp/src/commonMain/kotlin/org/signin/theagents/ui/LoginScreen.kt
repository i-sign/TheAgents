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
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.loginWithToken
import net.folivo.trixnity.client.media.createInMemoryMediaStoreModule
import net.folivo.trixnity.client.store.repository.createInMemoryRepositoriesModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.signin.theagents.service.SessionManager
import org.signin.theagents.service.getPlatformCreateMediaStoreModule
import org.signin.theagents.service.getPlatformRepositoryModule

class LoginScreen(private val initialUrl: String = BuildConfig.MATRIX_URL) : Screen, KoinComponent {
    @Composable
    override fun Content() {
        val sessionManager by inject<SessionManager>()
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var url by remember { mutableStateOf(initialUrl) }
        var loginToken by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            WebView(
                url = initialUrl,
                onLoginTokenReceived = { token ->
                    loginToken = token
                    scope.launch {
                        try {

                                val chatServerUrl = Url("https://chat-int.finnomena.com")

                                var client = MatrixClient.loginWithToken(
                                    identifier = null,
                                    baseUrl = chatServerUrl,
                                    repositoriesModule = createInMemoryRepositoriesModule(),
                                    mediaStoreModule = createInMemoryMediaStoreModule(),
                                    token = loginToken
                                ).getOrThrow()

                                println("Successful to login" + client.displayName.value ?: "N/A")


                        } catch (e: Exception) {
                            errorMessage = "Login failed: ${e.message}"
                            println(errorMessage)
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