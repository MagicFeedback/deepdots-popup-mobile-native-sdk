import UIKit

/// Cuenta: login/logout (setUserId), modales del host y acceso al resto de pantallas.
final class AccountViewController: StackViewController {

    private lazy var userLabel = addLabel("", font: .preferredFont(forTextStyle: .headline), id: "account_user")
    override var trackedPath: String? { Catalog.account }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Account"
        _ = userLabel
        addButton("Sign in as test-user-1", id: "account_sign_in") { [weak self] in
            Session.userId = "test-user-1"
            AppDelegate.sdk.setUserId(userId: "test-user-1")
            HostLog.add("setUserId test-user-1")
            self?.refreshUser()
        }
        addButton("Sign out", id: "account_sign_out") { [weak self] in
            Session.userId = nil
            AppDelegate.sdk.setUserId(userId: nil)
            HostLog.add("setUserId nil")
            self?.refreshUser()
        }

        addLabel("Host modals (the SDK must not touch them)", font: .preferredFont(forTextStyle: .subheadline))
        addButton("Open alert", id: "open_alert") { [weak self] in self?.presentAlert(fireAfter: false) }
        addButton("Open sheet", id: "open_sheet") { [weak self] in self?.presentSheet(fireAfter: false) }
        addButton("Open date picker", id: "open_date_picker") { [weak self] in self?.presentDatePicker() }

        addLabel("Other screens", font: .preferredFont(forTextStyle: .subheadline))
        addButton("Checkout (full screen)", id: "open_checkout") { [weak self] in
            self?.present(CheckoutViewController.wrapped(), animated: true)
        }
        addButton("Test cases", id: "open_test_cases") { [weak self] in
            self?.navigationController?.pushViewController(TestCasesViewController(), animated: true)
        }
        addButton("Diagnostics", id: "open_diagnostics") { [weak self] in
            self?.navigationController?.pushViewController(DiagnosticsViewController(), animated: true)
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refreshUser()
    }

    private func refreshUser() {
        userLabel.text = "Signed in as: \(Session.userId ?? "anonymous")\nSDK user_id: \(AppDelegate.sdk.getUserId() ?? "-")"
    }

    private func presentDatePicker() {
        let vc = UIViewController()
        let picker = UIDatePicker()
        picker.preferredDatePickerStyle = .inline
        vc.view = picker
        vc.view.backgroundColor = .systemBackground
        vc.sheetPresentationController?.detents = [.medium()]
        present(vc, animated: true)
        HostLog.add("host date picker opened")
    }
}

extension UIViewController {
    /// Alerta del host. Con `fireAfter`, dispara `custom-event` un segundo después de abrirla.
    func presentAlert(fireAfter: Bool) {
        let alert = UIAlertController(
            title: "Host alert",
            message: fireAfter ? "custom-event fires in 1 s." : "An alert owned by the app.",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "Close", style: .cancel))
        alert.view.accessibilityIdentifier = "host_alert"
        present(alert, animated: true)
        HostLog.add("host alert opened")
        if fireAfter { DispatchQueue.main.asyncAfter(deadline: .now() + 1) { fireEvent() } }
    }

    /// Sheet del host (`UISheetPresentationController`). Con `fireAfter`, dispara el evento al segundo.
    func presentSheet(fireAfter: Bool) {
        let sheet = UIViewController()
        sheet.view.backgroundColor = .systemBackground
        let label = UILabel()
        label.text = fireAfter ? "Host sheet. custom-event fires in 1 s." : "Host sheet"
        label.numberOfLines = 0
        let button = UIButton(type: .system)
        button.configuration = .bordered()
        button.setTitle("Fire custom-event from the sheet", for: .normal)
        button.accessibilityIdentifier = "sheet_fire_event"
        button.addAction(UIAction { _ in fireEvent() }, for: .touchUpInside)
        let stack = UIStackView(arrangedSubviews: [label, button])
        stack.axis = .vertical
        stack.spacing = 16
        stack.translatesAutoresizingMaskIntoConstraints = false
        sheet.view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: sheet.view.topAnchor, constant: 32),
            stack.leadingAnchor.constraint(equalTo: sheet.view.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(equalTo: sheet.view.trailingAnchor, constant: -24),
        ])
        sheet.view.accessibilityIdentifier = "host_sheet"
        sheet.sheetPresentationController?.detents = [.medium()]
        present(sheet, animated: true)
        HostLog.add("host sheet opened")
        if fireAfter { DispatchQueue.main.asyncAfter(deadline: .now() + 1) { fireEvent() } }
    }
}
