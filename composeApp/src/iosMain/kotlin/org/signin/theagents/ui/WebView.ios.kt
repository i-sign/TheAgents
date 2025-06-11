package org.signin.theagents.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

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

