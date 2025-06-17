package org.signin.theagents.service

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
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
    AndroidApp.INSTANCE.getSharedPreferences("TheAgentsPreferences", Context.MODE_PRIVATE)
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

@Composable
actual fun WebView(
    url: String,
    onLoginTokenReceived: (String) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        println("log on shouldOverrideUrlLoading with :" + request?.url?.toString())
                        if (request?.url?.toString()?.contains("/?loginToken=") == true) {
                            val loginToken = request.url.getQueryParameter("loginToken").orEmpty()
                            println("log loginToken : $loginToken")
                            onLoginTokenReceived(loginToken)
                        }

                        return super.shouldOverrideUrlLoading(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        println("log on onPageFinished")
                        if (url?.contains("chat") == true)
                            evaluateJavascript(
                                "document.getElementsByClassName('primary-button')[0].click();",
                                null
                            )
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
            }
        },
        modifier = Modifier,
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}
