package org.signin.theagents.service

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import io.github.aakira.napier.DebugAntilog
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.media.MediaStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun getPlatformSettings(): Settings = NSUserDefaultsSettings(
    NSUserDefaults("SmalkSettings")
)

actual fun getLogger(defaultTag: String): DebugAntilog = DebugAntilog(defaultTag)

actual fun createDateFormat(pattern: String): (instant: Instant) -> String =
    object : (Instant) -> String {
        private val formatter = NSDateFormatter().apply {
            setDateFormat(pattern)
        }

        override fun invoke(instant: Instant) = formatter.stringFromDate(
            NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        )
    }

actual suspend fun getPlatformRepositoryModule(): Module = createRealmRepositoriesModule {
    directory(getCacheDirectoryPath().resolve("realm").toString())
}

actual suspend fun getPlatformMediaStore(): MediaStore = OkioMediaStore(
getCacheDirectoryPath().resolve("media")
)

private fun getCacheDirectoryPath(): Path {
    val cacheDir = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return "$cacheDir/cache".toPath()
}
