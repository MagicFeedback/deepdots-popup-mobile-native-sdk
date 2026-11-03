import DeepdotsSDK
import SwiftUI
import UIKit

/// One screen. The SDK is loaded the minimum way: init, attachContext, setPath("/").
@main
struct BasicApp: App {
    private let sdkVersion = Bundle.main.object(forInfoDictionaryKey: "BasicSDKVersion") as? String ?? "?"

    var body: some Scene {
        WindowGroup {
            Text("Basic app · SDK \(sdkVersion)")
                .font(.title3)
                .accessibilityIdentifier("basic-home")
                .onAppear { BasicSDK.shared.start() }
        }
    }
}

final class BasicSDK {
    static let shared = BasicSDK()
    private let sdk = DeepdotsSDK.DeepdotsPopups()
    private var started = false

    func start() {
        guard !started else { return }
        started = true
        // Kotlin default arguments are not visible from Swift: every InitOptions parameter is passed.
        sdk.initialize(options: InitOptions(
            debug: true,
            environment: Environment.development,
            mode: Mode.server,
            popupOptions: PopupOptions(id: nil, publicKey: BasicSDK.publicKey(), popups: nil, companyId: nil),
            provideLang: { nil },
            autoLaunch: true,
            storage: nil,
            trackingEnabled: true,
            analytics: nil,
            onFeedbackSession: nil,
            geolocation: true,
            contactAttributes: nil,
            renderChrome: true,
            showProgressBar: nil,
            metadata: nil
        ))
        if let root = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow }).first?.rootViewController {
            sdk.attachContext(context: PlatformContext(viewController: root))
        }
        sdk.setPath(path: "/")
    }

    /// The client-exact-launch key from test-apps/scenarios.json (bundled).
    private static func publicKey() -> String {
        guard let url = Bundle.main.url(forResource: "scenarios", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let scenarios = json["scenarios"] as? [String: Any],
              let scenario = scenarios["client-exact-launch"] as? [String: Any],
              let key = scenario["publicKey"] as? String else { fatalError("client-exact-launch missing") }
        return key
    }
}
