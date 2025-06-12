package org.signin.theagents

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.MatrixClient.LoginInfo
import net.folivo.trixnity.clientserverapi.client.MatrixAuthProvider
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClientImpl
import net.folivo.trixnity.clientserverapi.client.classicInMemory
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.clientserverapi.model.authentication.LoginType
import net.folivo.trixnity.core.model.UserId


@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface MatrixClients : StateFlow<Map<UserId, MatrixClient>> {
    suspend fun login(
        baseUrl: Url,
        identifier: IdentifierType,
        password: String,
        initialDeviceDisplayName: String?,
    ): Result<MatrixClient>

    suspend fun login(
        baseUrl: Url,
        token: String,
        initialDeviceDisplayName: String?,
    ): Result<MatrixClient>

    suspend fun loginWith(
        baseUrl: Url,
        loginInfo: LoginInfo,
    ): Result<MatrixClient>

    data class InitFromStoreResult(
        val success: Set<UserId>,
        val failures: Map<UserId, MatrixClientInitializationException>,
    )

    suspend fun initFromStore(): InitFromStoreResult

    suspend fun logout(userId: UserId): Result<Unit>

    suspend fun remove(userId: UserId): Result<Unit>
}

data class AccountAlreadyExistsException(val userId: UserId) :
    IllegalStateException("account $userId already exists locally")
