import UIKit
import DeepdotsSDK

/// Estado del SDK y registro de eventos, para pruebas manuales y para los E2E.
final class DiagnosticsViewController: StackViewController {

    private lazy var identity = addLabel("", font: .monospacedSystemFont(ofSize: 12, weight: .regular), id: "diag_identity")
    private let log = UILabel()
    override var trackedPath: String? { Catalog.diagnostics }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Diagnostics"
        _ = identity
        addButton("Fire event", id: "diag_fire_event") { fireEvent() }
        addButton("close()", id: "diag_close_popup") { [weak self] in
            guard let root = self?.view.window?.rootViewController else { return }
            AppDelegate.sdk.close(context: PlatformContext(viewController: root))
            HostLog.add("close()")
        }
        addButton("Flush analytics", id: "diag_flush") {
            AppDelegate.sdk.flushAnalytics(meta: AnalyticsFlushMeta(final: false, sessionEnd: false))
            HostLog.add("flushAnalytics")
        }
        let toggle = UISwitch()
        toggle.isOn = Settings.fireEventOnColdStart
        toggle.accessibilityIdentifier = "diag_cold_event"
        toggle.addAction(UIAction { _ in Settings.fireEventOnColdStart = toggle.isOn }, for: .valueChanged)
        let row = UIStackView(arrangedSubviews: [UILabel.with("Fire custom-event on next cold start"), toggle])
        row.spacing = 8
        stack.addArrangedSubview(row)
        log.numberOfLines = 0
        log.font = .monospacedSystemFont(ofSize: 11, weight: .regular)
        log.accessibilityIdentifier = "diag_log"
        stack.addArrangedSubview(log)
        NotificationCenter.default.addObserver(self, selector: #selector(render), name: HostLog.didChange, object: nil)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        render()
    }

    @objc private func render() {
        let sdk = AppDelegate.sdk
        let info = Bundle.main.infoDictionary ?? [:]
        identity.text = """
        env: \(info["DeepdotsEnv"] as? String ?? "-")  key: \((info["DeepdotsPublicKey"] as? String ?? "").prefix(8))…
        user_id: \(sdk.getUserId() ?? "-")
        session_id: \(sdk.getSessionId() ?? "-")
        popups loaded: \(sdk.debugListPopups().count)
        """
        log.text = HostLog.snapshot().joined(separator: "\n")
    }
}

extension UILabel {
    static func with(_ text: String) -> UILabel {
        let label = UILabel(); label.text = text; label.numberOfLines = 0; return label
    }
}
