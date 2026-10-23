import UIKit
import DeepdotsSDK

/// Integración del SDK tal como la haría un cliente con una app UIKit:
///
/// - Una sola instancia, creada e inicializada en `application(_:didFinishLaunchingWithOptions:)`
///   (arranque en frío).
/// - El contexto es el `rootViewController` de la ventana (`SceneDelegate`), como en el demo y en
///   la guía de integración: el SDK presenta el popup desde ahí.
/// - Foreground/background en el `SceneDelegate` (`sceneWillEnterForeground` /
///   `sceneDidEnterBackground`), nunca en `sceneWillResignActive`: `inactive` es transitorio en iOS.
///
/// Todo lo que pasa se apunta en `HostLog`, que la pantalla de diagnóstico enseña y los E2E leen.
@main
final class AppDelegate: UIResponder, UIApplicationDelegate {

    static let sdk = DeepdotsPopups()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        let sdk = AppDelegate.sdk
        sdk.on(event: Events.shared.popupShown) { HostLog.add("PopupShown popup=\($0.popupId)") }
        sdk.on(event: Events.shared.popupClicked) {
            HostLog.add("PopupClicked popup=\($0.popupId) action=\($0.extra["action"] ?? "")")
        }
        sdk.on(event: Events.shared.surveyCompleted) { HostLog.add("SurveyCompleted survey=\($0.surveyId)") }

        let info = Bundle.main.infoDictionary ?? [:]
        let publicKey = info["DeepdotsPublicKey"] as? String ?? ""
        let env = info["DeepdotsEnv"] as? String ?? "dev"
        let userId = Session.userId
        // Kotlin no exporta los valores por defecto a Swift: hay que pasar todos los parámetros.
        let options = InitOptions(
            debug: true,
            environment: env == "prod" ? Environment.production : Environment.development,
            mode: Mode.server,
            popupOptions: PopupOptions(id: nil, publicKey: publicKey, popups: nil, companyId: nil),
            provideLang: { "en" },
            autoLaunch: true,
            storage: nil,
            trackingEnabled: true,
            analytics: nil,
            onFeedbackSession: nil,
            geolocation: true,
            contactAttributes: nil,
            renderChrome: true,
            showProgressBar: nil,
            metadata: userId.map { ["userId": $0] }
        )
        sdk.initialize(options: options)
        HostLog.add("init env=\(env) user=\(sdk.getUserId() ?? "-")")

        // Disparo ANTES de que lleguen las definiciones: el SDK debe guardarlo y reproducirlo.
        if Settings.fireEventOnColdStart {
            sdk.triggerEvent(eventName: Catalog.customEvent)
            HostLog.add("triggerEvent \(Catalog.customEvent) (cold start)")
        }
        return true
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        UISceneConfiguration(name: "Default", sessionRole: connectingSceneSession.role)
    }
}
