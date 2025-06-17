package org.signin.theagents

import TheAgents.composeApp.BuildConfig
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.signin.theagents.service.SessionManager
import org.signin.theagents.service.getLogger
import org.signin.theagents.service.getPlatformSettings
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.runBlocking
import org.signin.theagents.theme.AppTheme
import org.signin.theagents.ui.HomeScreen
import org.signin.theagents.ui.LoginScreen

internal val LocalAppScope =
    compositionLocalOf<CoroutineScope> { error("LocalAppScope is not provided") }
internal val LocalPhoneMode = compositionLocalOf { mutableStateOf(false) }

@Composable
@Preview
internal fun App(systemAppearance: (isLight: Boolean) -> Unit = {}) {

    initApp()
    Napier.d("Start application")
    val sessionManager: SessionManager by remember { KoinPlatform.getKoin().inject() }
    DisposableEffect(Unit) {
        onDispose {
            Napier.d("Stop application")
            runBlocking { sessionManager.stop() }
        }
    }
    CompositionLocalProvider(
        LocalAppScope provides rememberCoroutineScope()
    ) {
        var isPhone by LocalPhoneMode.current
        val density = LocalDensity.current

        Box(Modifier.fillMaxSize().onGloballyPositioned {
            with(density) {
                isPhone = it.size.width.toDp() <= 700.dp
            }
        }) {
            AppTheme(systemAppearance) {
                var launchScreen by remember { mutableStateOf<Screen?>(null) }
                LaunchedEffect(Unit) {
                    val hasPreviousSession = sessionManager.tryRestoreSession()
                    launchScreen = if (hasPreviousSession) {
                        HomeScreen()
                    } else {
                        LoginScreen()
                    }

                }
                launchScreen?.let { Navigator(it) }
            }
        }
    }
}

private var isInit = false
private fun initApp() {
    if (isInit) {
        Napier.e("Second initialization!")
        return
    }
    isInit = true

    if (BuildConfig.DEBUG) {
        Napier.base(getLogger("TheAgents"))
    }

    val appModule = module {
        single { getPlatformSettings() }
        single { SessionManager(get()) }
        factory { get<SessionManager>().getClient() }
    }
    startKoin {
        modules(appModule)
    }
}