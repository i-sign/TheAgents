package org.signin.theagents.service

import com.russhwolf.settings.Settings
import io.github.aakira.napier.DebugAntilog
import net.folivo.trixnity.client.media.MediaStore
import org.koin.core.module.Module

expect fun getPlatformSettings(): Settings
expect fun getLogger(defaultTag: String): DebugAntilog
expect suspend fun getPlatformRepositoryModule(): Module
expect suspend fun getPlatformMediaStore(): Module