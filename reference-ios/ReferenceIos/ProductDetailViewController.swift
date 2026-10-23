import UIKit
import DeepdotsSDK

/// Ficha de producto en UIKit, con un scroll largo enganchado al SDK con `bindScrollView`
/// (el helper de iOS), y botones de click y evento. Back = el de la barra de navegación.
final class ProductDetailViewController: StackViewController {

    private let product: Catalog.Product
    private var unbindScroll: (() -> Void)?
    override var trackedPath: String? { Catalog.productPath(product.id) }

    init(product: Catalog.Product) {
        self.product = product
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = product.name
        addLabel("\(product.price) · \(product.note)")
        addButton("Add to cart", id: "detail_add_to_cart", style: .filled()) { [product] in
            AppDelegate.sdk.triggerClick(targetId: Catalog.addToCartClick)
            AppDelegate.sdk.trackFunnelStep(funnel: "purchase", step: "add_to_cart", taskId: "order-\(product.id)", params: nil)
            HostLog.add("triggerClick \(Catalog.addToCartClick)")
        }
        addButton("Fire event", id: "detail_fire_event") { fireEvent() }
        addButton("Go to checkout", id: "detail_checkout") { [weak self] in
            self?.present(CheckoutViewController.wrapped(), animated: true)
        }
        for index in 1...30 {
            addLabel("Section \(index). Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do " +
                     "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.")
        }
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        unbindScroll = AppDelegate.sdk.bindScrollView(scrollView: scroll)
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        unbindScroll?()
        unbindScroll = nil
    }
}
