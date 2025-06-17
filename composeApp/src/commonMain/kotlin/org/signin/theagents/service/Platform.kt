package org.signin.theagents.service

import com.russhwolf.settings.Settings
import io.github.aakira.napier.DebugAntilog

expect fun getPlatformSettings(): Settings
expect fun getLogger(defaultTag: String): DebugAntilog
