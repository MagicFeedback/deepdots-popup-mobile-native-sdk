import Foundation
import os

/// Backend the app talks to. There is no production option: test data stays in MAGICFEEDBACK_DEV.
let devAPI = "https://api-dev.deepdots.com"

/// SDK version the app was generated with (`SDK_VERSION=… xcodegen generate`).
let sdkVersion = Bundle.main.object(forInfoDictionaryKey: "LabSDKVersion") as? String ?? "?"

/// Scenario name -> DEV public key / host events, from test-apps/scenarios.json (bundled).
enum Scenarios {
    private struct File: Decodable {
        struct Scenario: Decodable { let publicKey: String; let events: [String]? }
        let scenarios: [String: Scenario]
    }

    private static let file: File = {
        guard let url = Bundle.main.url(forResource: "scenarios", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let file = try? JSONDecoder().decode(File.self, from: data) else {
            fatalError("scenarios.json missing from the bundle")
        }
        return file
    }()

    static var names: [String] { file.scenarios.keys.sorted() }
    static func publicKey(_ name: String) -> String? { file.scenarios[name]?.publicKey }
    static func events(_ name: String) -> [String] { file.scenarios[name]?.events ?? [] }
}

/// What a run tests. Maestro passes it as launch arguments; the SDK Lab panel edits it. It is
/// saved, so a relaunch without arguments (re-display, background tests) keeps the same run.
struct LabConfig: Equatable {
    var scenario = "checkout-time"
    /// `history`: every screen calls setPath(). `silent`: the app never tells the SDK it navigated.
    var nav = "history"
    var lang = "en"
    /// `immediate`: init at launch. `consent`: init after "Accept all" in the cookie dialog.
    var load = "immediate"
    var user = ""
    /// First screen: home, shop, cart, checkout or account (one-shot, not saved).
    var start = "home"

    var publicKey: String { Scenarios.publicKey(scenario) ?? "" }

    private static let keys = ["scenario", "nav", "lang", "load", "user"]
    private static let defaults = UserDefaults(suiteName: "lab")!

    func save() {
        let d = LabConfig.defaults
        d.set(scenario, forKey: "scenario"); d.set(nav, forKey: "nav"); d.set(lang, forKey: "lang")
        d.set(load, forKey: "load"); d.set(user, forKey: "user")
    }

    static var consent: String? {
        get { defaults.string(forKey: "consent") }
        set { defaults.set(newValue, forKey: "consent") }
    }

    /// Saved config, overridden by any launch argument (`-scenario x`, `scenario x` or `scenario=x`).
    static func resolve(arguments: [String] = ProcessInfo.processInfo.arguments) -> LabConfig {
        let known = keys + ["start"]
        var args: [String: String] = [:]
        var i = 1
        while i < arguments.count {
            let raw = arguments[i]
            let key = raw.hasPrefix("-") ? String(raw.dropFirst()) : raw
            if let eq = key.firstIndex(of: "="), known.contains(String(key[..<eq])) {
                args[String(key[..<eq])] = String(key[key.index(after: eq)...])
            } else if known.contains(key), i + 1 < arguments.count {
                args[key] = arguments[i + 1]
                i += 1
            }
            i += 1
        }
        func read(_ key: String, _ fallback: String) -> String {
            args[key] ?? defaults.string(forKey: key) ?? fallback
        }
        let base = LabConfig()
        var config = LabConfig(
            scenario: read("scenario", base.scenario),
            nav: read("nav", base.nav),
            lang: read("lang", base.lang),
            load: read("load", base.load),
            user: read("user", base.user),
            start: args["start"] ?? base.start
        )
        if Scenarios.publicKey(config.scenario) == nil { config.scenario = base.scenario }
        if keys.contains(where: { args[$0] != nil }) { config.save() }
        return config
    }
}

/// Run log: on screen (SDK Lab) and in the unified log with the `[lab]` prefix, which is what CI keeps.
final class Lab: ObservableObject {
    static let shared = Lab()
    @Published private(set) var lines: [String] = []
    private let logger = Logger(subsystem: "com.deepdots.testapp", category: "lab")
    private let clock: DateFormatter = {
        let f = DateFormatter(); f.dateFormat = "HH:mm:ss"; f.locale = Locale(identifier: "en_US_POSIX"); return f
    }()

    static func log(_ tag: String, _ message: String) { shared.append(tag, message) }

    private func append(_ tag: String, _ message: String) {
        logger.info("[lab] \(tag, privacy: .public) \(message, privacy: .public)")
        print("[lab] \(tag) \(message)")
        let line = "\(clock.string(from: Date())) \(tag) \(message)"
        DispatchQueue.main.async {
            self.lines.append(line)
            if self.lines.count > 200 { self.lines.removeFirst() }
        }
    }
}
