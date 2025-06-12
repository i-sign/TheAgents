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
import net.folivo.trixnity.client.store.RootStore
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.loginWithToken
import net.folivo.trixnity.client.serverDiscovery
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.clientserverapi.model.authentication.LoginType
import net.folivo.trixnity.core.model.UserId
import org.koin.core.module.Module

class SessionManager(settings: Settings) {
    private var client: MatrixClient? = null
    private var deviceId by settings.nullableString("DEVICE_ID")
    private var accessToken by settings.nullableString("ACCESS_TOKEN")
    private var userId by settings.nullableString("USER_ID")


    suspend fun loginWithToken(serverUrl: String, loginToken: String) {
        try {
            val chatServerUrl = Url(serverUrl)
            //val serverInfo = serverDiscovery(serverUrl)


val a = getPlatformRepositoryModule()
            val b = getPlatformCreateMediaStoreModule()

         val client =   MatrixClient.loginWithToken(
                baseUrl = chatServerUrl,
                token = loginToken,
                repositoriesModule = a,
                mediaStoreModule = b,
            ).getOrNull()


            Napier.e( "Successful to login"+client?.loginState.toString())
        } catch (e: Exception) {
            Napier.e("Failed to login", e)
            throw e
        }
    }

    suspend fun initFromStore(
        userId: UserId,
    ): Result<MatrixClient?> = kotlin.runCatching {
        Napier.e { "initFromStore (userId=$userId)" }
        MatrixClient.fromStore(
            repositoriesModule = getPlatformRepositoryModule(),
            mediaStoreModule = getPlatformCreateMediaStoreModule(),
        ).getOrThrow()
    }


}