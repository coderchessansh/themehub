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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 23, 42))
        }
        val bar = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 8)
        }
        val url = EditText(this).apply {
            hint = "https://example.com"
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setBackgroundColor(Color.rgb(35, 45, 65))
            setPadding(14, 10, 14, 10)
        }
        val go = Button(this).apply { text = "GO" }
        bar.addView(url, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(go, LinearLayout.LayoutParams(-2, -2))
        root.addView(bar)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.settings.setSupportZoom(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, loadedUrl: String) {
                injectThemeUi()
            }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
        }
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        go.setOnClickListener { loadUrl(url.text.toString()) }
        loadUrl("https://www.google.com")
    }

    private fun loadUrl(raw: String) {
        var value = raw.trim()
        if (value.isEmpty()) return
        if (!value.startsWith("http://") && !value.startsWith("https://")) value = "https://$value"
        webView.loadUrl(value)
    }

    private fun injectThemeUi() {
        val safe = JSONObject.quote(themeJson)
        val js = """
            (() => {
              try {
                const t = JSON.parse($safe);
                const old = document.getElementById('__themehub_root');
                if (old) old.remove();
                const fonts = ['Arial','Verdana','Georgia','Courier New','Inter','Roboto','Poppins','Montserrat','Open Sans','Nunito','Lato','Raleway','Oswald','Playfair Display','Merriweather','Ubuntu','Quicksand','Rubik','Bebas Neue','Space Grotesk','DM Sans','Fira Sans'];
                const root = document.createElement('div');
                root.id = '__themehub_root';
                root.style.cssText = 'position:fixed;right:14px;bottom:18px;z-index:2147483647;font-family:Arial,sans-serif;';
                const button = document.createElement('button');
                button.textContent = '🎨';
                button.title = 'ThemeHub';
                button.style.cssText = 'width:46px;height:46px;border:0;border-radius:50%;background:'+(t.accent||'#6366f1')+';color:#fff;font-size:22px;box-shadow:0 4px 14px #0008;cursor:pointer;';
                const panel = document.createElement('div');
                panel.style.cssText = 'display:none;position:absolute;right:0;bottom:56px;width:220px;padding:12px;border-radius:14px;background:'+(t.bg||'#0f172a')+';color:'+(t.text||'#fff')+';box-shadow:0 8px 30px #0009;border:1px solid '+(t.accent||'#6366f1')+'88;';
                const title = document.createElement('div');
                title.textContent = 'ThemeHub';
                title.style.cssText = 'font-weight:700;margin-bottom:9px;font-size:15px;';
                const select = document.createElement('select');
                select.style.cssText = 'width:100%;padding:8px;border-radius:8px;margin-bottom:8px;background:'+(t.bg||'#0f172a')+';color:'+(t.text||'#fff')+';border:1px solid '+(t.accent||'#6366f1')+';';
                fonts.forEach(f => { const o=document.createElement('option'); o.value=f; o.textContent=f; select.appendChild(o); });
                if (t.font) select.value=t.font;
                const apply = document.createElement('button');
                apply.textContent = 'Apply theme';
                apply.style.cssText = 'width:100%;padding:9px;border:0;border-radius:8px;background:'+(t.accent||'#6366f1')+';color:#fff;font-weight:600;cursor:pointer;';
                const reset = document.createElement('button');
                reset.textContent = 'Reset';
                reset.style.cssText = 'width:100%;padding:8px;margin-top:7px;border:1px solid '+(t.accent||'#6366f1')+';border-radius:8px;background:transparent;color:'+(t.text||'#fff')+';cursor:pointer;';
                panel.append(title, select, apply, reset);
                root.append(button, panel);
                document.documentElement.appendChild(root);

                const applyTheme = (fontName) => {
                  const oldStyle=document.getElementById('__themehub_style'); if(oldStyle) oldStyle.remove();
                  const oldFont=document.getElementById('__themehub_font'); if(oldFont) oldFont.remove();
                  const link=document.createElement('link'); link.id='__themehub_font'; link.rel='stylesheet';
                  link.href='https://fonts.googleapis.com/css2?family='+encodeURIComponent(fontName).replace(/%20/g,'+')+':wght@400;500;600;700&display=swap';
                  document.head.appendChild(link);
                  const s=document.createElement('style'); s.id='__themehub_style';
                  const safeFont=fontName.replace(/[^a-zA-Z0-9 ,'-]/g,'');
                  s.textContent='html,body{background:'+(t.bg||'#fff')+' !important;color:'+(t.text||'#111')+' !important} body,body *{font-family:"'+safeFont+'",sans-serif !important;color:'+(t.text||'#111')+' !important;border-color:'+(t.accent||'#6366f1')+'55 !important} a{color:'+(t.accent||'#6366f1')+' !important} button,[role=button]{border-color:'+(t.accent||'#6366f1')+' !important}';
                  document.documentElement.appendChild(s);
                };
                button.onclick=()=>{panel.style.display=panel.style.display==='none'?'block':'none';};
                apply.onclick=()=>{applyTheme(select.value);};
                reset.onclick=()=>{const s=document.getElementById('__themehub_style');if(s)s.remove();const l=document.getElementById('__themehub_font');if(l)l.remove();};
                applyTheme(t.font || 'Inter');
              } catch(e) {}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }
}
