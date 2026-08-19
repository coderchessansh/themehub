package com.coderchessansh.themehub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var pendingExportJson: String? = null

    private val filePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val json = contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
            } ?: throw IllegalStateException("Could not open file")
            val escaped = org.json.JSONObject.quote(json)
            webView.evaluateJavascript("window.importThemeFromAndroid($escaped)", null)
        } catch (_: Exception) {
            Toast.makeText(this, "Could not import that theme file.", Toast.LENGTH_SHORT).show()
        }
    }

    private val fileSaver = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri == null || json == null) return@registerForActivityResult
        try {
            contentResolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(json.toByteArray(Charsets.UTF_8))
                output.flush()
            } ?: throw IllegalStateException("Could not open output")
            Toast.makeText(this, "Theme exported successfully!", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not export that theme file.", Toast.LENGTH_SHORT).show()
        }
    }

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
            runOnUiThread { filePicker.launch(arrayOf("application/json", "text/json", "text/plain")) }
        }

        @JavascriptInterface
        fun exportThemeFile(themeJson: String, fileName: String) {
            runOnUiThread {
                pendingExportJson = themeJson
                val clean = fileName.trim()
                    .ifBlank { "theme" }
                    .replace(Regex("[^A-Za-z0-9._-]"), "_")
                    .removeSuffix(".json")
                fileSaver.launch("$clean.json")
            }
        }

        @JavascriptInterface
        fun saveTheme(themeJson: String) {
            getSharedPreferences("themehub", MODE_PRIVATE).edit()
                .putString("theme", themeJson)
                .apply()
        }

        @JavascriptInterface
        fun openBrowser() {
            startActivity(Intent(this@MainActivity, BrowserActivity::class.java))
        }
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface("Android")
        webView.destroy()
        super.onDestroy()
    }
}