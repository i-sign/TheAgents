package org.signin.theagents.ui

import TheAgents.composeApp.BuildConfig
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.signin.theagents.service.SessionManager

class LoginScreen(private val initialUrl: String = BuildConfig.MATRIX_URL) : Screen, KoinComponent {

    private val sessionManager by inject<SessionManager>()

    @Composable
    override fun Content() {
        var url by remember { mutableStateOf(initialUrl) }

        Box(modifier = Modifier.fillMaxSize()) {
            // WebView will be implemented here
            WebView(url = initialUrl)
        }
    }
}

@Composable
expect fun WebView(url: String)