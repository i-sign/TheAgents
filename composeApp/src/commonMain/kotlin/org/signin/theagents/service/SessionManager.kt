package org.signin.theagents.service

import com.russhwolf.settings.Settings
import com.russhwolf.settings.nullableString
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.loginWithToken
import net.folivo.trixnity.core.model.UserId

class SessionManager(settings: Settings) {
    private var client: MatrixClient? = null
    private var deviceId by settings.nullableString("DEVICE_ID")
    private var accessToken by settings.nullableString("ACCESS_TOKEN")
    private var userId by settings.nullableString("USER_ID")


    suspend fun loginWithToken(serverUrl: String, loginToken: String) {
        try {
            val chatServerUrl = Url(serverUrl)
            //val serverInfo = serverDiscovery(serverUrl)
           var client =MatrixClient.loginWithToken(
                baseUrl = chatServerUrl,
                repositoriesModule = getPlatformRepositoryModule(),
                mediaStoreModule = getPlatformCreateMediaStoreModule(),
                token = loginToken
            ).getOrThrow()

            Napier.e("Successful to login" + client.loginState ?: "N/A")
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
            mediaStoreModule = getPlatformCreateMediaStoreModule()
        ).getOrThrow()
    }
}