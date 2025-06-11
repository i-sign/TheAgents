package org.signin.theagents.ui

import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

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

