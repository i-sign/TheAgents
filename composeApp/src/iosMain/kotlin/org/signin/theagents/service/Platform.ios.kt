package org.signin.theagents.service

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
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
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

actual fun getPlatformSettings(): Settings = NSUserDefaultsSettings(
    NSUserDefaults("TheAgentsSettings")
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


@Composable
actual fun WebView(
    url: String,
    onLoginTokenReceived: (String) -> Unit
) {
    val webView = remember { WKWebView() }
    UIKitView(
        factory = {
            webView.apply {
                WKWebViewConfiguration().apply {
                    allowsInlineMediaPlayback = true
                    allowsAirPlayForMediaPlayback = true
                    allowsPictureInPictureMediaPlayback = true
                }
                navigationDelegate = WKNavigationDelegate(onLoginTokenReceived)

                loadRequest(NSURLRequest(NSURL(string = url)))
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

class WKNavigationDelegate(
    private val onLoginTokenReceived: (String) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val url = decidePolicyForNavigationAction.request.URL?.absoluteString ?: ""
        if (url.contains("/?loginToken=")) {
            val loginToken = url.split("loginToken=").getOrNull(1) ?: ""
            onLoginTokenReceived(loginToken)
        }
        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
    }

    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        if (webView.URL?.host().toString().contains("chat")) {
            //webView.evaluateJavaScript("document.getElementsByClassName('primary-button')[0].click();")
            webView.evaluateJavaScript(
                "document.getElementsByClassName('primary-button')[0].click();",
            ) { result, error ->
                // Handle result or error if needed
            }
        }
    }
}
