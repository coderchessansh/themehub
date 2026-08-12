package com.coderchessansh.themehub

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.content.Context
import org.json.JSONObject

class BrowserActivity : Activity() {
    private lateinit var webView: WebView
    private var themeJson: String = "{}"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeJson = getSharedPreferences("themehub", Context.MODE_PRIVATE).getString("theme", "{}") ?: "{}"

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(15,23,42)) }
        val bar = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(8,8,8,8) }
        val url = EditText(this).apply { hint = "https://example.com"; singleLine = true; setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY); setBackgroundColor(Color.rgb(35,45,65)); setPadding(14,10,14,10) }
        val go = Button(this).apply { text = "GO" }
        val theme = Button(this).apply { text = "🎨"; contentDescription = "Apply Theme" }
        bar.addView(url, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(go, LinearLayout.LayoutParams(-2, -2))
        bar.addView(theme, LinearLayout.LayoutParams(-2, -2))
        root.addView(bar)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.settings.setSupportZoom(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, loadedUrl: String) {
                injectTheme()
            }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
        }
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        go.setOnClickListener { loadUrl(url.text.toString()) }
        theme.setOnClickListener { injectTheme(); Toast.makeText(this, "Theme applied", Toast.LENGTH_SHORT).show() }
        loadUrl("https://www.google.com")
    }

    private fun loadUrl(raw: String) {
        var value = raw.trim()
        if (value.isEmpty()) return
        if (!value.startsWith("http://") && !value.startsWith("https://")) value = "https://$value"
        webView.loadUrl(value)
    }

    private fun injectTheme() {
        val safe = JSONObject.quote(themeJson)
        val js = """
            (() => {
              try {
                const t = JSON.parse($safe);
                const old = document.getElementById('__themehub_style');
                if (old) old.remove();
                const s = document.createElement('style'); s.id='__themehub_style';
                s.textContent = `html,body{background:${'$'}{t.bg} !important;color:${'$'}{t.text} !important} body *{color:${'$'}{t.text} !important;border-color:${'$'}{t.accent}55 !important} a{color:${'$'}{t.accent} !important} button,[role=button],input,textarea,select{background-color:${'$'}{t.accent} !important;color:#fff !important;border-color:${'$'}{t.accent} !important} header,nav,aside,main,section,article,div{}`;
                document.documentElement.appendChild(s);
              } catch(e) {}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }
}