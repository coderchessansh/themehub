import Foundation

struct Theme: Codable, Identifiable, Equatable {
    var id = UUID()
    var name = "My Theme"
    var background = "#0F172A"
    var text = "#FFFFFF"
    var accent = "#5865F2"
    var font = "Inter"
}

extension Theme {
    static let presets: [Theme] = [
        Theme(name: "Discord", background: "#313338", text: "#FFFFFF", accent: "#5865F2", font: "Inter"),
        Theme(name: "Midnight", background: "#111111", text: "#FFFFFF", accent: "#6666FF", font: "Montserrat"),
        Theme(name: "Light", background: "#FFFFFF", text: "#000000", accent: "#5865F2", font: "Open Sans"),
        Theme(name: "Matrix", background: "#000000", text: "#00FF00", accent: "#00AA00", font: "Fira Code"),
        Theme(name: "Ocean", background: "#082F49", text: "#E0F2FE", accent: "#06B6D4", font: "Nunito"),
        Theme(name: "Sunset", background: "#431407", text: "#FFF7ED", accent: "#F97316", font: "Montserrat"),
        Theme(name: "Cyberpunk", background: "#120024", text: "#FDF4FF", accent: "#E879F9", font: "Oswald")
    ]
}