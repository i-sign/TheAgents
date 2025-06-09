package org.signin.theagents.service

import androidx.compose.runtime.Composable
import com.russhwolf.settings.Settings
import io.github.aakira.napier.DebugAntilog
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.media.MediaStore
import org.koin.core.module.Module

expect fun getPlatformSettings(): Settings
expect fun getLogger(defaultTag: String): DebugAntilog
expect fun createDateFormat(pattern: String): (instant: Instant) -> String
expect suspend fun getPlatformRepositoryModule(): Module
expect suspend fun getPlatformMediaStore(): MediaStore

