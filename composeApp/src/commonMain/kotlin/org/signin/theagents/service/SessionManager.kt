package org.signin.theagents.service

import com.russhwolf.settings.Settings
import com.russhwolf.settings.nullableString
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.Url
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.MatrixClientConfiguration
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.loginWith
import net.folivo.trixnity.client.loginWithToken
import net.folivo.trixnity.client.media.createInMemoryMediaStoreModule
import net.folivo.trixnity.client.media.createMediaModule
import net.folivo.trixnity.client.store.repository.createInMemoryRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.media.CreateMedia
import net.folivo.trixnity.core.model.UserId
import org.signin.theagents.CreateRepositoriesModule
import org.signin.theagents.platformCreateMediaStoreModuleModule
import org.signin.theagents.platformCreateRepositoriesModuleModule

class SessionManager(settings: Settings) {
    private var client: MatrixClient? = null
    private var deviceId by settings.nullableString("DEVICE_ID")

    suspend fun loginWithToken(serverUrl: String, loginToken: String) {
        try {
            val chatServerUrl = Url(serverUrl)


            /*
               baseUrl: Url,
    identifier: IdentifierType? = null,
    token: String,
    deviceId: String? = null,
    initialDeviceDisplayName: String? = null,
    repositoriesModule: Module,
    mediaStoreModule: Module,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    configuration: MatrixClientConfiguration.() -> Unit = {},
             */

            client = MatrixClient.loginWithToken(
                identifier = null,
                baseUrl = chatServerUrl,
                repositoriesModule = createInMemoryRepositoriesModule(),
                mediaStoreModule = createInMemoryMediaStoreModule(),
                token = loginToken,
                configuration = clientConfig
            ).getOrThrow()

            println("Successful to login" + client?.displayName?.value ?: "N/A")

            Napier.e("Successful to login" + client?.loginState?.value ?: "N/A")
        } catch (e: Exception) {
            Napier.e("Failed to login", e)
            throw e
        }
    }

    fun getClient() = client ?: error("Session client is NULL!")

    suspend fun tryRestoreSession(): Boolean {
        Napier.d("Try restore session [$deviceId]")
        val restored = MatrixClient.fromStore(
            repositoriesModule = createInMemoryRepositoriesModule(),
            mediaStoreModule = createInMemoryMediaStoreModule(),
            configuration = clientConfig
        ).getOrNull()


        if (restored != null) {
            client = restored
            return true
        } else {
            return false
        }
    }

    suspend fun stop() {
        Napier.d("Stop session")
        client?.stopSync()
        client = null
    }

    private val clientConfig: MatrixClientConfiguration.() -> Unit = {
        httpClientConfig = {
            HttpClient {
                install(Logging) {
                    level = LogLevel.ALL
                    logger = object : Logger {
                        override fun log(message: String) {
                            Napier.d(tag = "HTTP Client", message = message)
                        }
                    }
                }
            }
        }
    }
}