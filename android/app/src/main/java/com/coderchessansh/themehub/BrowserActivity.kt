package com.coderchessansh.themehub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject

class BrowserActivity : Activity() {
    private data class BrowserTab(
        val webView: WebView,
        var title: String = "New tab",
        var url: String = "https://www.google.com"
    )

    private lateinit var tabStrip: LinearLayout
    private lateinit var content: FrameLayout
    private lateinit var urlBar: EditText
    private val tabs = mutableListOf<BrowserTab>()
    private var selectedTab = 0
    private var themeJson: String = "{}"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeJson = getSharedPreferences("themehub", Context.MODE_PRIVATE)
            .getString("theme", "{}") ?: "{}"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 23, 42))
        }

        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(6, 5, 6, 5)
            setBackgroundColor(Color.rgb(10, 15, 28))
        }
        val tabScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(tabBar)
        }
        tabStrip = tabBar
        root.addView(tabScroll, LinearLayout.LayoutParams(-1, 52))

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(6, 6, 6, 6)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }
        val back = Button(this).apply { text = "‹"; textSize = 20f }
        val forward = Button(this).apply { text = "›"; textSize = 20f }
        val reload = Button(this).apply { text = "↻"; textSize = 18f }
        urlBar = EditText(this).apply {
            hint = "Search or enter URL"
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setBackgroundColor(Color.rgb(35, 45, 65))
            setPadding(14, 7, 14, 7)
        }
        val go = Button(this).apply { text = "GO" }
        controls.addView(back, LinearLayout.LayoutParams(45, -2))
        controls.addView(forward, LinearLayout.LayoutParams(45, -2))
        controls.addView(reload, LinearLayout.LayoutParams(45, -2))
        controls.addView(urlBar, LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(go, LinearLayout.LayoutParams(-2, -2))
        root.addView(controls)

        content = FrameLayout(this)
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        val loadTypedUrl = View.OnClickListener { loadUrl(urlBar.text.toString()) }
        go.setOnClickListener(loadTypedUrl)
        urlBar.setOnEditorActionListener { _, _, _ -> loadUrl(urlBar.text.toString()); true }
        back.setOnClickListener { currentWebView()?.goBack() }
        forward.setOnClickListener { currentWebView()?.goForward() }
        reload.setOnClickListener { currentWebView()?.reload() }
        createTab("https://www.google.com")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createTab(initialUrl: String = "https://www.google.com") {
        val web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.setSupportZoom(true)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, loadedUrl: String) {
                    val index = tabs.indexOfFirst { it.webView === view }
                    if (index >= 0) {
                        tabs[index].url = loadedUrl
                        tabs[index].title = view.title?.takeIf { it.isNotBlank() } ?: hostName(loadedUrl)
                        if (index == selectedTab) updateBrowserUi()
                        renderTabs()
                    }
                    injectThemeUi(view)
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
            }
        }

        val tab = BrowserTab(web)
        tabs.add(tab)
        selectedTab = tabs.lastIndex
        content.addView(web, FrameLayout.LayoutParams(-1, -1))
        web.loadUrl(initialUrl)
        showSelectedTab()
    }

    private fun closeTab(index: Int) {
        if (tabs.size == 1) {
            tabs[0].webView.loadUrl("https://www.google.com")
            tabs[0].title = "New tab"
            tabs[0].url = "https://www.google.com"
            selectedTab = 0
            showSelectedTab()
            renderTabs()
            return
        }
        val removed = tabs.removeAt(index)
        content.removeView(removed.webView)
        removed.webView.destroy()
        if (selectedTab >= tabs.size) selectedTab = tabs.lastIndex
        else if (index < selectedTab) selectedTab--
        showSelectedTab()
        renderTabs()
    }

    private fun selectTab(index: Int) {
        if (index !in tabs.indices) return
        selectedTab = index
        showSelectedTab()
        renderTabs()
    }

    private fun showSelectedTab() {
        tabs.forEachIndexed { index, tab ->
            tab.webView.visibility = if (index == selectedTab) View.VISIBLE else View.GONE
        }
        updateBrowserUi()
    }

    private fun renderTabs() {
        tabStrip.removeAllViews()
        tabs.forEachIndexed { index, tab ->
            val chip = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12, 0, 4, 0)
                setBackgroundColor(if (index == selectedTab) Color.rgb(35, 45, 65) else Color.rgb(20, 27, 43))
                setOnClickListener { selectTab(index) }
            }
            val title = TextView(this).apply {
                text = tab.title.take(20)
                setTextColor(Color.WHITE)
                textSize = 13f
                maxLines = 1
            }
            val close = TextView(this).apply {
                text = "  ×"
                setTextColor(Color.LTGRAY)
                textSize = 17f
                setPadding(4, 0, 6, 0)
                setOnClickListener { closeTab(index) }
            }
            chip.addView(title, LinearLayout.LayoutParams(0, 46, 1f))
            chip.addView(close, LinearLayout.LayoutParams(-2, 46))
            val params = LinearLayout.LayoutParams(180, 46)
            params.setMargins(3, 0, 3, 0)
            tabStrip.addView(chip, params)
        }

        val add = TextView(this).apply {
            text = "+"
            setTextColor(Color.WHITE)
            textSize = 25f
            gravity = Gravity.CENTER
            setOnClickListener { createTab() }
        }
        val addParams = LinearLayout.LayoutParams(50, 46)
        addParams.setMargins(3, 0, 3, 0)
        tabStrip.addView(add, addParams)
    }

    private fun currentWebView(): WebView? = tabs.getOrNull(selectedTab)?.webView

    private fun updateBrowserUi() {
        val tab = tabs.getOrNull(selectedTab) ?: return
        urlBar.setText(tab.url)
        urlBar.setSelection(urlBar.text.length)
    }

    private fun loadUrl(raw: String) {
        var value = raw.trim()
        if (value.isEmpty()) return
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = if (value.contains(".") && !value.contains(" ")) "https://$value"
            else "https://www.google.com/search?q=" + android.net.Uri.encode(value)
        }
        val tab = tabs.getOrNull(selectedTab) ?: return
        tab.url = value
        tab.webView.loadUrl(value)
    }

    private fun hostName(url: String): String = try {
        android.net.Uri.parse(url).host ?: "New tab"
    } catch (_: Exception) { "New tab" }

    private fun injectThemeUi(view: WebView) {
        val safe = JSONObject.quote(themeJson)
        val js = """
            (() => {
              try {
                const t = JSON.parse($safe);
                const old = document.getElementById('__themehub_root'); if (old) old.remove();
                const fonts = ['Arial','Verdana','Tahoma','Georgia','Times New Roman','Courier New','Impact','Inter','Roboto','Poppins','Montserrat','Open Sans','Lato','Nunito','Ubuntu','Raleway','Oswald','Playfair Display','Merriweather','Roboto Slab','Fira Code'];
                const googleFonts = new Set(['Inter','Roboto','Poppins','Montserrat','Open Sans','Lato','Nunito','Ubuntu','Raleway','Oswald','Playfair Display','Merriweather','Roboto Slab','Fira Code']);
                const root = document.createElement('div');
                root.id='__themehub_root';
                root.style.cssText='position:fixed;right:14px;bottom:18px;z-index:2147483647;font-family:Arial,sans-serif;touch-action:none;';

                const button=document.createElement('button');
                button.textContent='🎨';
                button.title='ThemeHub — drag me';
                button.style.cssText='width:46px;height:46px;border:0;border-radius:50%;background:'+(t.accent||'#6366f1')+';color:#fff;font-size:22px;box-shadow:0 4px 14px #0008;cursor:grab;touch-action:none;';

                const panel=document.createElement('div');
                panel.style.cssText='display:none;position:absolute;right:0;bottom:56px;width:220px;padding:12px;border-radius:14px;background:'+(t.bg||'#0f172a')+';color:'+(t.text||'#fff')+';box-shadow:0 8px 30px #0009;border:1px solid '+(t.accent||'#6366f1')+'88;';
                const title=document.createElement('div');
                title.textContent='ThemeHub';
                title.style.cssText='font-weight:700;margin-bottom:9px;font-size:15px;';
                const select=document.createElement('select');
                select.style.cssText='width:100%;padding:8px;border-radius:8px;margin-bottom:8px;background:'+(t.bg||'#0f172a')+';color:'+(t.text||'#fff')+';border:1px solid '+(t.accent||'#6366f1')+';';
                fonts.forEach(f=>{const o=document.createElement('option');o.value=f;o.textContent=f;select.appendChild(o);});

                const rawSavedFont = String(t.font || 'Inter');
                const savedFont = rawSavedFont.split(',')[0].trim();
                if(fonts.includes(savedFont)) select.value=savedFont;

                const apply=document.createElement('button');
                apply.textContent='Apply theme';
                apply.style.cssText='width:100%;padding:9px;border:0;border-radius:8px;background:'+(t.accent||'#6366f1')+';color:#fff;font-weight:600;cursor:pointer;';
                const reset=document.createElement('button');
                reset.textContent='Reset';
                reset.style.cssText='width:100%;padding:8px;margin-top:7px;border:1px solid '+(t.accent||'#6366f1')+';border-radius:8px;background:transparent;color:'+(t.text||'#fff')+';cursor:pointer;';
                panel.append(title,select,apply,reset);
                root.append(button,panel);
                document.documentElement.appendChild(root);

                const removeTheme=()=>{
                  const s=document.getElementById('__themehub_style');if(s)s.remove();
                  const l=document.getElementById('__themehub_font');if(l)l.remove();
                };

                const applyTheme=async(fontName)=>{
                  removeTheme();
                  const cleanFont=String(fontName).split(',')[0].trim();
                  if(googleFonts.has(cleanFont)){
                    const link=document.createElement('link');
                    link.id='__themehub_font';
                    link.rel='stylesheet';
                    link.href='https://fonts.googleapis.com/css2?family='+encodeURIComponent(cleanFont).replace(/%20/g,'+')+'&display=swap';
                    link.onerror=()=>{link.remove();};
                    document.head.appendChild(link);
                  }
                  const fontStack=cleanFont==='Georgia'||cleanFont==='Times New Roman'||cleanFont==='Merriweather'||cleanFont==='Playfair Display' ? cleanFont+',serif' : cleanFont+',sans-serif';
                  const s=document.createElement('style');
                  s.id='__themehub_style';
                  s.textContent='html,body{background:'+(t.bg||'#fff')+' !important;color:'+(t.text||'#111')+' !important}body,body *:not(svg):not(path):not(img):not([aria-hidden="true"]){font-family:'+fontStack+' !important}a{color:'+(t.accent||'#6366f1')+' !important}';
                  document.documentElement.appendChild(s);
                  try { if(document.fonts){ await document.fonts.load('400 16px '+cleanFont); await document.fonts.ready; } } catch(e) {}
                };

                let dragging=false,moved=false,startX=0,startY=0,startLeft=0,startTop=0;
                button.addEventListener('pointerdown',e=>{
                  dragging=true;moved=false;button.setPointerCapture(e.pointerId);button.style.cursor='grabbing';
                  const r=root.getBoundingClientRect();startX=e.clientX;startY=e.clientY;startLeft=r.left;startTop=r.top;
                  e.preventDefault();
                });
                button.addEventListener('pointermove',e=>{
                  if(!dragging)return;
                  const dx=e.clientX-startX,dy=e.clientY-startY;
                  if(Math.abs(dx)>4||Math.abs(dy)>4)moved=true;
                  const maxX=window.innerWidth-root.offsetWidth,maxY=window.innerHeight-root.offsetHeight;
                  root.style.left=Math.max(0,Math.min(maxX,startLeft+dx))+'px';
                  root.style.top=Math.max(0,Math.min(maxY,startTop+dy))+'px';
                  root.style.right='auto';root.style.bottom='auto';
                  e.preventDefault();
                });
                button.addEventListener('pointerup',e=>{
                  dragging=false;button.style.cursor='grab';
                  if(!moved)panel.style.display=panel.style.display==='none'?'block':'none';
                  e.preventDefault();
                });
                button.addEventListener('pointercancel',()=>{dragging=false;button.style.cursor='grab';});

                apply.onclick=()=>applyTheme(select.value);
                reset.onclick=()=>removeTheme();
                applyTheme(savedFont || 'Inter');
              } catch(e) {}
            })();
        """.trimIndent()
        view.evaluateJavascript(js, null)
    }
}