package com.coderchessansh.themehub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private val pickerRequest = 1001
    private val exportRequest = 1002
    private var pendingExportJson: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.addJavascriptInterface(ThemeHubBridge(), "Android")
        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }

    private inner class ThemeHubBridge {
        @JavascriptInterface
        fun openThemeFilePicker() {
            runOnUiThread {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }
                startActivityForResult(intent, pickerRequest)
            }
        }

        @JavascriptInterface
        fun exportThemeFile(themeJson: String, fileName: String) {
            pendingExportJson = themeJson
            runOnUiThread {
                val safeName = fileName.ifBlank { "theme" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, if (safeName.endsWith(".json")) safeName else "$safeName.json")
                }
                startActivityForResult(intent, exportRequest)
            }
        }

        @JavascriptInterface
        fun saveTheme(themeJson: String) {
            getSharedPreferences("themehub", MODE_PRIVATE).edit().putString("theme", themeJson).apply()
        }

        @JavascriptInterface
        fun openBrowser() {
            startActivity(Intent(this@MainActivity, BrowserActivity::class.java))
        }
    }

    @Deprecated("Use Activity Result APIs when modernizing this project")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        val uri: Uri = data.data!!

        if (requestCode == pickerRequest) {
            try {
                val input = contentResolver.openInputStream(uri) ?: return
                val json = BufferedReader(InputStreamReader(input)).use { it.readText() }
                val escaped = org.json.JSONObject.quote(json)
                webView.evaluateJavascript("window.importThemeFromAndroid($escaped)", null)
            } catch (_: Exception) {
                webView.evaluateJavascript("alert('Could not read that theme file.')", null)
            }
        } else if (requestCode == exportRequest) {
            try {
                val json = pendingExportJson ?: return
                contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                webView.evaluateJavascript("alert('Theme exported!')", null)
            } catch (_: Exception) {
                webView.evaluateJavascript("alert('Could not export that theme file.')", null)
            } finally {
                pendingExportJson = null
            }
        }
    }
}