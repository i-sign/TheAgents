package org.signin.theagents.service

import android.content.Context
import androidx.room.RoomDatabase
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.aakira.napier.DebugAntilog
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.media.MediaStore
import net.folivo.trixnity.client.store.repository.room.TrixnityRoomDatabase
import net.folivo.trixnity.core.model.UserId
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import org.signin.theagents.AndroidApp
import java.text.SimpleDateFormat
import net.folivo.trixnity.client.store.repository.room.createRoomRepositoriesModule
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.media.okio.createOkioMediaStoreModule
import okio.Path.Companion.toOkioPath
import org.signin.theagents.utils.RootPath
import okio.FileSystem


actual fun getPlatformSettings(): Settings = SharedPreferencesSettings(
    AndroidApp.INSTANCE.getSharedPreferences("SmalkPreferences", Context.MODE_PRIVATE)
)

actual fun getLogger(defaultTag: String): DebugAntilog = DebugAntilog(defaultTag)

actual fun createDateFormat(pattern: String) = object : (Instant) -> String {
    private val formatter = SimpleDateFormat(pattern)
    override fun invoke(instant: Instant) = formatter.format(instant.toEpochMilliseconds())
}

actual fun getPlatformRepositoryModule(): Module = module{
    single<CreateRepositoriesModule> {
        val rootPath = get<RootPath>()
        val fileSystem = get<FileSystem>()
        val context = get<Context>()

        object : CreateRepositoriesModule {
            override suspend fun generateDatabaseKey(): ByteArray? = null
            override suspend fun create(userId: UserId, databaseKey: ByteArray?): Module {
                fileSystem.createDirectories(
                    rootPath.forAccountDatabase(userId),
                    mustCreate = false
                )
                return createRoomRepositoriesModule(db(userId))
            }

            override suspend fun load(userId: UserId, databaseKey: ByteArray?): Module {
                return createRoomRepositoriesModule(db(userId))
            }

            private fun db(userId: UserId): RoomDatabase.Builder<TrixnityRoomDatabase> =
                Room.databaseBuilder<TrixnityRoomDatabase>(
                    context,
                    rootPath.forAccountDatabase(userId).resolve("database").toString()
                ).apply {
                    setDriver(BundledSQLiteDriver())
                }
        }
    }
}

actual fun getPlatformCreateMediaStoreModule(): Module = module {
    single<CreateMediaStoreModule> {
        val rootPath = get<RootPath>()
        val filesystem = get<FileSystem>()
        CreateMediaStoreModule { userId ->
            withContext(Dispatchers.IO) {
                createOkioMediaStoreModule(
                    basePath = rootPath.forAccountMedia(userId),
                    fileSystem = filesystem
                )
            }
        }
    }
}



private fun getCacheDirectoryPath(): Path =
    AndroidApp.INSTANCE.cacheDir.absolutePath.toPath().resolve("cache")

actual fun platformPathsModule(): Module = module {
    single { okio.FileSystem.SYSTEM }
    single<RootPath> {
        val context = get<Context>()
        RootPath(context.filesDir.toOkioPath())
    }
}