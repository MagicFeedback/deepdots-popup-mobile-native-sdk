import UIKit

/// Checkout presentado a pantalla completa por encima de las pestañas: el equivalente iOS de abrir
/// otra Activity. El `rootViewController` que tiene el SDK ya está presentando esta pantalla.
final class CheckoutViewController: StackViewController {

    static func wrapped() -> UIViewController {
        let nav = UINavigationController(rootViewController: CheckoutViewController())
        nav.modalPresentationStyle = .fullScreen
        return nav
    }

    override var trackedPath: String? { Catalog.checkout }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Checkout"
        navigationItem.leftBarButtonItem = UIBarButtonItem(systemItem: .close, primaryAction: UIAction { [weak self] _ in
            self?.dismiss(animated: true)
        })
        navigationItem.leftBarButtonItem?.accessibilityIdentifier = "checkout_close"
        addButton("Place order", id: "checkout_place_order", style: .filled()) {
            AppDelegate.sdk.trackFunnelStep(funnel: "purchase", step: "order_placed", taskId: "order-checkout", params: nil)
            HostLog.add("trackFunnelStep purchase/order_placed")
        }
        addButton("Fire custom-event", id: "checkout_fire_event") { fireEvent() }
    }
}
