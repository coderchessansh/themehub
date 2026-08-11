package com.coderchessansh.themehub

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        // ThemeHub only loads its bundled local HTML, so broad file/content
        // access is unnecessary and disabled to reduce the WebView attack surface.
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }
}