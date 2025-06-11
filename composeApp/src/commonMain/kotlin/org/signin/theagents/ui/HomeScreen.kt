package org.signin.theagents.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
                        Napier.e("Home redirect and success login with :$loginState")
                    }
                }
            }.collect(
                collector = TODO()
            )
        }
    }
}