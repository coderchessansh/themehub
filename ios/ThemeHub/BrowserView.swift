import SwiftUI
import WebKit

struct BrowserView: View {
    let theme: Theme
    @Environment(\.dismiss) private var dismiss
    @State private var tabs: [String] = ["https://www.google.com"]
    @State private var selected = 0

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack {
                        ForEach(Array(tabs.enumerated()), id: \.offset) { index, url in
                            HStack(spacing: 5) {
                                Button(url.hostName) { selected = index }
                                Button("×") { close(index) }
                            }
                            .padding(.horizontal, 10).padding(.vertical, 7)
                            .background(index == selected ? Color(hex: theme.accent) : Color.gray.opacity(0.2))
                            .clipShape(Capsule())
                        }
                        Button("+") { tabs.append("https://www.google.com"); selected = tabs.count - 1 }
                    }.padding(8)
                }
                BrowserWebView(url: tabs[selected], theme: theme)
            }
            .navigationTitle("Theme Web Pages")
            .toolbar { ToolbarItem(placement: .topBarTrailing) { Button("Done") { dismiss() } } }
        }
    }

    private func close(_ index: Int) {
        guard tabs.count > 1 else { return }
        tabs.remove(at: index)
        selected = min(selected, tabs.count - 1)
    }
}

struct BrowserWebView: UIViewRepresentable {
    let url: String
    let theme: Theme

    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .default()
        let view = WKWebView(frame: .zero, configuration: configuration)
        view.navigationDelegate = context.coordinator
        view.allowsBackForwardNavigationGestures = true
        return view
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        guard webView.url?.absoluteString != url else { return }
        if let requestURL = URL(string: url) { webView.load(URLRequest(url: requestURL)) }
    }

    func makeCoordinator() -> Coordinator { Coordinator(theme: theme) }

    final class Coordinator: NSObject, WKNavigationDelegate {
        let theme: Theme
        init(theme: Theme) { self.theme = theme }
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            let bg = theme.background.replacingOccurrences(of: "#", with: "")
            let text = theme.text.replacingOccurrences(of: "#", with: "")
            let accent = theme.accent.replacingOccurrences(of: "#", with: "")
            let font = theme.font.replacingOccurrences(of: "'", with: "")
            let js = """
            (() => {
              const old=document.getElementById('__themehub_ios'); if(old) old.remove();
              const s=document.createElement('style'); s.id='__themehub_ios';
              s.textContent=`html,body{background:#\(bg)!important;color:#\(text)!important}body,body *:not(svg):not(path):not(img){font-family:'\(font)',sans-serif!important}a{color:#\(accent)!important}`;
              document.documentElement.appendChild(s);
              const b=document.createElement('button'); b.textContent='🎨'; b.title='ThemeHub';
              b.style='position:fixed;right:16px;bottom:18px;z-index:2147483647;width:46px;height:46px;border:0;border-radius:50%;background:#\(accent);color:white;font-size:22px;box-shadow:0 4px 14px #0008';
              b.onclick=()=>alert('ThemeHub theme applied: \(font)'); document.body.appendChild(b);
            })();
            """
            webView.evaluateJavaScript(js)
        }
    }
}

private extension String {
    var hostName: String { URL(string: self)?.host ?? "New tab" }
}