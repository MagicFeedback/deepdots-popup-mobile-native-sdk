import DeepdotsSDK
import UIKit

/// Owns the SDK for the whole process, as a host app would, and feeds it the app lifecycle.
final class SDKHost: ObservableObject {
    static let shared = SDKHost()

    let sdk = DeepdotsSDK.DeepdotsPopups()
    let config = LabConfig.resolve()
    @Published private(set) var initialized = false

    func initSDK() {
        guard !initialized else { return }
        initialized = true
        let c = config
        Lab.log("sdk", "init \(sdkVersion) (SPM) env=Development api=\(devAPI)")
        Lab.log("sdk", "scenario=\(c.scenario) key=\(c.publicKey) nav=\(c.nav) lang=\(c.lang)")
        sdk.on(event: Events.shared.popupShown) { e in Lab.log("popup", "shown id=\(e.popupId) survey=\(e.surveyId)") }
        sdk.on(event: Events.shared.popupClicked) { e in Lab.log("popup", "clicked id=\(e.popupId) action=\(e.extra["action"] ?? "")") }
        sdk.on(event: Events.shared.surveyCompleted) { e in Lab.log("popup", "survey_completed survey=\(e.surveyId)") }
        let lang = c.lang
        // Kotlin default arguments are not visible from Swift: every InitOptions parameter is passed.
        sdk.initialize(options: InitOptions(
            debug: true,
            environment: Environment.development,
            mode: Mode.server,
            popupOptions: PopupOptions(id: nil, publicKey: c.publicKey, popups: nil, companyId: nil),
            provideLang: { lang },
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
        if !c.user.isEmpty { sdk.setUserId(userId: c.user) }
        attachContext()
        Lab.log("sdk", "userId=\(sdk.getUserId() ?? "-")")
    }

    /// The popup is presented from this controller.
    func attachContext() {
        guard let root = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow }).first?.rootViewController else { return }
        sdk.attachContext(context: PlatformContext(viewController: root))
    }

    func setPath(_ path: String) {
        guard initialized else { return }
        sdk.setPath(path: path)
    }

    func triggerEvent(_ name: String) { if initialized { sdk.triggerEvent(eventName: name) } }
    func triggerClick(_ id: String) { if initialized { sdk.triggerClick(targetId: id) } }

    // scenePhase .background / back to .active, never .inactive: onBackground() ends the SDK session.
    func didEnterBackground() {
        Lab.log("life", "background")
        if initialized { sdk.onBackground() }
    }

    func willEnterForeground() {
        Lab.log("life", "foreground")
        if initialized { sdk.onForeground() }
    }

    /// Clears everything the SDK stored (cooldowns, user id) and the cookie choice; keeps the run config.
    func reset() {
        if let id = Bundle.main.bundleIdentifier { UserDefaults.standard.removePersistentDomain(forName: id) }
        LabConfig.consent = nil
        config.save()
    }
}
