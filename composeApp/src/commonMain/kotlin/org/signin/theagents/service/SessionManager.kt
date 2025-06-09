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
import net.folivo.trixnity.client.serverDiscovery
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType

class SessionManager(settings: Settings) {
    private var client: MatrixClient? = null
    private var deviceId by settings.nullableString("DEVICE_ID")

    suspend fun tryRestoreSession(): Boolean {
        Napier.d("Try restore session [$deviceId]")
        val restored = MatrixClient.fromStore(
            mediaStore = getPlatformMediaStore(),
            repositoriesModule = getPlatformRepositoryModule(),
            configuration = clientConfig
        ).getOrNull()

        if (restored != null) {
            client = restored
            return true
        } else {
            return false
        }
    }

    suspend fun login(server: String, username: String, password: String) {
        Napier.d("Open session [$deviceId]")
        val url = server.serverDiscovery().getOrNull() ?: Url(server)
        client = MatrixClient.login(
            baseUrl = url,
            mediaStore = getPlatformMediaStore(),
            repositoriesModule = getPlatformRepositoryModule(),
            identifier = IdentifierType.User(username),
            password = password,
            deviceId = deviceId,
            configuration = clientConfig
        ).getOrThrow()
        deviceId = client?.deviceId
    }

    suspend fun logout() {
        Napier.d("End session")
        client?.apply {
            logout()
            clearCache()
            clearMediaCache()
            stop()
        }
        client = null
    }

    fun stop() {
        Napier.d("Stop session")
        client?.stop()
        client = null
    }

    fun getClient() = client ?: error("Session client is NULL!")

    private val clientConfig: MatrixClientConfiguration.() -> Unit = {
        httpClientFactory = {
            HttpClient {
                it()
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