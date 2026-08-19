import SwiftUI
import UniformTypeIdentifiers

struct ContentView: View {
    @State private var theme = Theme()
    @State private var saved: [Theme] = []
    @State private var showImporter = false
    @State private var shareURL: URL?
    @State private var showBrowser = false
    private let fonts = ["Arial", "Verdana", "Tahoma", "Georgia", "Times New Roman", "Courier New", "Impact", "Inter", "Roboto", "Poppins", "Montserrat", "Open Sans", "Lato", "Nunito", "Ubuntu", "Raleway", "Oswald", "Playfair Display", "Merriweather", "Roboto Slab", "Fira Code"]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    GroupBox("Theme Builder") {
                        VStack(spacing: 12) {
                            TextField("Theme name", text: $theme.name).textFieldStyle(.roundedBorder)
                            ColorPicker("Background", selection: hexBinding(keyPath: \\Theme.background), supportsOpacity: false)
                            ColorPicker("Text", selection: hexBinding(keyPath: \\Theme.text), supportsOpacity: false)
                            ColorPicker("Accent", selection: hexBinding(keyPath: \\Theme.accent), supportsOpacity: false)
                            Picker("Font", selection: $theme.font) { ForEach(fonts, id: \\.self) { Text($0).tag($0) } }
                                .pickerStyle(.menu)
                            HStack {
                                Button("💾 Save") { save() }
                                Button("📤 Export") { exportTheme() }
                                Button("📥 Import") { showImporter = true }
                            }
                            Button("🌐 Theme Web Pages") { showBrowser = true }
                                .buttonStyle(.borderedProminent)
                        }
                    }

                    GroupBox("Presets") {
                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 120))]) {
                            ForEach(Theme.presets) { preset in Button(preset.name) { theme = preset } }
                        }
                    }

                    GroupBox("Live Preview") {
                        VStack(spacing: 10) {
                            Text("ThemeHub Preview").font(.title2.bold())
                            Text("Your selected colors and font appear here.")
                            Button("Example Button") {}
                        }
                        .frame(maxWidth: .infinity).padding(20)
                        .background(Color(hex: theme.background))
                        .foregroundStyle(Color(hex: theme.text))
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                    }

                    if !saved.isEmpty {
                        GroupBox("Saved Themes") {
                            ForEach(saved) { item in Button(item.name) { theme = item } }
                        }
                    }
                }.padding()
            }
            .navigationTitle("🎨 ThemeHub")
            .fileImporter(isPresented: $showImporter, allowedContentTypes: [.json]) { result in importTheme(result) }
            .sheet(isPresented: $showBrowser) { BrowserView(theme: theme) }
            .sheet(isPresented: Binding(get: { shareURL != nil }, set: { if !$0 { shareURL = nil } })) {
                if let url = shareURL { ShareSheet(url: url) }
            }
            .onAppear { loadSaved() }
        }
    }

    private func save() {
        saved.append(theme)
        UserDefaults.standard.set(try? JSONEncoder().encode(saved), forKey: "savedThemes")
    }

    private func exportTheme() {
        do {
            let data = try JSONEncoder().encode(theme)
            let safeName = theme.name.isEmpty ? "theme" : theme.name.replacingOccurrences(of: "/", with: "_")
            let url = FileManager.default.temporaryDirectory.appendingPathComponent(safeName + ".json")
            try data.write(to: url, options: .atomic)
            shareURL = url
        } catch {}
    }

    private func importTheme(_ result: Result<URL, Error>) {
        guard case .success(let url) = result else { return }
        do {
            guard url.startAccessingSecurityScopedResource() else { return }
            defer { url.stopAccessingSecurityScopedResource() }
            theme = try JSONDecoder().decode(Theme.self, from: Data(contentsOf: url))
        } catch {}
    }

    private func loadSaved() {
        if let data = UserDefaults.standard.data(forKey: "savedThemes"), let value = try? JSONDecoder().decode([Theme].self, from: data) { saved = value }
    }

    private func hexBinding(keyPath: ReferenceWritableKeyPath<Theme, String>) -> Binding<Color> {
        Binding(get: { Color(hex: theme[keyPath: keyPath]) }, set: { theme[keyPath: keyPath] = $0.toHex() })
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    let url: URL
    func makeUIViewController(context: Context) -> UIActivityViewController { UIActivityViewController(activityItems: [url], applicationActivities: nil) }
    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}

extension Color {
    init(hex: String) {
        let cleaned = hex.replacingOccurrences(of: "#", with: "")
        var value: UInt64 = 0
        Scanner(string: cleaned).scanHexInt64(&value)
        self.init(.sRGB, red: Double((value >> 16) & 0xFF) / 255, green: Double((value >> 8) & 0xFF) / 255, blue: Double(value & 0xFF) / 255, opacity: 1)
    }
    func toHex() -> String {
        let ui = UIColor(self)
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        ui.getRed(&r, green: &g, blue: &b, alpha: &a)
        return String(format: "#%02X%02X%02X", Int(r * 255), Int(g * 255), Int(b * 255))
    }
}