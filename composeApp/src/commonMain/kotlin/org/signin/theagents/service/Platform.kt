package org.signin.theagents.service

import androidx.compose.runtime.Composable
import com.russhwolf.settings.Settings
import io.github.aakira.napier.DebugAntilog
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.media.MediaStore
import net.folivo.trixnity.core.model.UserId
import org.koin.core.module.Module


interface CreateRepositoriesModule {
    suspend fun generateDatabaseKey(): ByteArray?
    suspend fun create(userId: UserId, databaseKey: ByteArray?): Module
    suspend fun load(userId: UserId, databaseKey: ByteArray?): Module
}

fun interface CreateMediaStoreModule {
    suspend operator fun invoke(userId: UserId): Module
}

expect fun getPlatformSettings(): Settings
expect fun getLogger(defaultTag: String): DebugAntilog
expect fun createDateFormat(pattern: String): (instant: Instant) -> String
expect fun getPlatformRepositoryModule(): Module
expect fun getPlatformCreateMediaStoreModule(): Module
expect fun platformPathsModule(): Module

