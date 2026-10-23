import UIKit

/// Casos de prueba: cada uno dice cómo reproducirlo y qué tiene que pasar. Los mismos que la app
/// de referencia de Android, para que las dos plataformas se prueben igual.
final class TestCasesViewController: StackViewController {

    override var trackedPath: String? { Catalog.testCases }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Test cases"

        addCase("1. Popup fired while a host sheet is open",
                steps: "Opens a sheet and fires custom-event one second later.",
                expected: "The popup does not end up hidden or lost: it shows on top of the sheet, or " +
                    "right after the sheet is closed, and it can be used.",
                id: "case_sheet_event") { [weak self] in self?.presentSheet(fireAfter: true) }

        addCase("2. Popup fired while a host alert is open",
                steps: "Opens an alert and fires custom-event one second later.",
                expected: "The alert stays usable. The popup shows once the alert is closed, or on top " +
                    "of it; it is never lost.",
                id: "case_alert_event") { [weak self] in self?.presentAlert(fireAfter: true) }

        addCase("3. Tap outside the popup",
                steps: "Fires custom-event. When the popup is open, tap the dimmed area.",
                expected: "Nothing in the app reacts and the popup stays open. Close the popup: the " +
                    "probe works again.",
                id: "case_scrim_event") { fireEvent() }

        addCase("4. Survey that cannot load",
                steps: "Turn the network off (Mac: disconnect Wi-Fi, the simulator shares it; device: " +
                    "airplane mode) after the app has started, then fire custom-event.",
                expected: "The popup shows \"We couldn't load this survey.\", the X and a Cancel button. " +
                    "Both close it; then the probe works.",
                id: "case_load_error_event") { fireEvent() }

        addCase("5. Writing in the survey with the keyboard open",
                steps: "Fires custom-event. Answer the rating; on the text page the keyboard opens.",
                expected: "Back and Send stay visible above the keyboard and Send works with the " +
                    "keyboard open.",
                id: "case_keyboard_event") { fireEvent() }

        addCase("6. Popup fired from a full-screen screen",
                steps: "Opens Checkout (presented full screen over the tabs) and fires custom-event there.",
                expected: "The popup shows over Checkout and can be used.",
                id: "case_fullscreen_event") { [weak self] in
            self?.present(CheckoutViewController.wrapped(), animated: true) {
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) { fireEvent() }
            }
        }
    }

    private func addCase(_ title: String, steps: String, expected: String, id: String, run: @escaping () -> Void) {
        addLabel(title, font: .preferredFont(forTextStyle: .headline))
        addLabel(steps)
        addLabel("Expected: \(expected)", font: .preferredFont(forTextStyle: .footnote))
        addButton("Run", id: id) {
            HostLog.add("test case: \(title)")
            run()
        }
    }
}
