package org.signin.theagents.service

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.aakira.napier.DebugAntilog
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.media.MediaStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.signin.theagents.AndroidApp
import java.text.SimpleDateFormat

actual fun getPlatformSettings(): Settings = SharedPreferencesSettings(
    AndroidApp.INSTANCE.getSharedPreferences("SmalkPreferences", Context.MODE_PRIVATE)
)

actual fun getLogger(defaultTag: String): DebugAntilog = DebugAntilog(defaultTag)

actual fun createDateFormat(pattern: String) = object : (Instant) -> String {
    private val formatter = SimpleDateFormat(pattern)
    override fun invoke(instant: Instant) = formatter.format(instant.toEpochMilliseconds())
}
actual suspend fun getPlatformRepositoryModule(): Module = createRealmRepositoriesModule {
    directory(getCacheDirectoryPath().resolve("realm").toString())
}

actual suspend fun getPlatformMediaStore(): MediaStore = OkioMediaStore(
    getCacheDirectoryPath().resolve("media")
)
private fun getCacheDirectoryPath(): Path =
    AndroidApp.INSTANCE.cacheDir.absolutePath.toPath().resolve("cache")
