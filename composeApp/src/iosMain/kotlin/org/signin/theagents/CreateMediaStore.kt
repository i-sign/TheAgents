package org.signin.theagents

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.media.okio.createOkioMediaStoreModule
import okio.FileSystem
import org.koin.core.module.Module
import org.koin.dsl.module
import org.signin.theagents.utils.RootPath

actual fun platformCreateMediaStoreModuleModule(): Module = module {
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