package org.signin.theagents.service

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import io.github.aakira.napier.DebugAntilog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.media.okio.createOkioMediaStoreModule
import net.folivo.trixnity.client.store.repository.room.TrixnityRoomDatabase
import net.folivo.trixnity.client.store.repository.room.createRoomRepositoriesModule
import net.folivo.trixnity.core.model.UserId
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import org.signin.theagents.utils.RootPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
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


actual fun getPlatformRepositoryModule(): Module = module {
    single<CreateRepositoriesModule> {
        val rootPath = get<RootPath>()
        val fileSystem = get<FileSystem>()

        object : CreateRepositoriesModule {
            override suspend fun generateDatabaseKey(): ByteArray? = null
            override suspend fun create(userId: UserId, databaseKey: ByteArray?): Module {
                fileSystem.createDirectories(rootPath.forAccountDatabase(userId), mustCreate = false)
                return createRoomRepositoriesModule(db(userId))
            }

            override suspend fun load(userId: UserId, databaseKey: ByteArray?): Module {
                return createRoomRepositoriesModule(db(userId))
            }

            private fun db(userId: UserId): RoomDatabase.Builder<TrixnityRoomDatabase> {

                return Room.databaseBuilder<TrixnityRoomDatabase>(
                    rootPath.forAccountDatabase(userId).resolve("database").toString()
                ).apply {
                    setDriver(BundledSQLiteDriver())
                }
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


private fun getCacheDirectoryPath(): Path {
    val cacheDir = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return "$cacheDir/cache".toPath()
}

actual fun platformPathsModule(): Module = module {
    single { FileSystem.SYSTEM }
    single<RootPath> {
        RootPath(
            (NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            )[0] as String)
                .toPath()
        )
    }
}