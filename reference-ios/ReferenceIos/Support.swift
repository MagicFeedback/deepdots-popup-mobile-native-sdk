import UIKit
import DeepdotsSDK

/// Rutas que la app pasa a `setPath()` y nombres de trigger. Las segmenta la configuración de
/// pruebas en api-dev ("Reference app (native SDK tests)", survey "Reference app - Basic"):
///  - `/detail/1` → time on page (2 s)
///  - `/detail/2` → scroll (50 %)
///  - `/detail/3` → exit (al salir de la ficha)
///  - evento `custom-event` → cualquier ruta
///  - click `add-to-cart` → cualquier ficha (`/detail`)
enum Catalog {
    struct Product { let id: Int; let name: String; let price: String; let note: String }

    static let products = [
        Product(id: 1, name: "Cordless drill 18V", price: "1.299 kr", note: "Popup by time on page (2 s)"),
        Product(id: 2, name: "Wood screws 4x40 (200)", price: "89 kr", note: "Popup by scroll (50 %)"),
        Product(id: 3, name: "Safety gloves", price: "129 kr", note: "Popup on exit (when you leave)"),
        Product(id: 4, name: "Spirit level 60 cm", price: "249 kr", note: "Popup on Add to cart (click)"),
        Product(id: 5, name: "Measuring tape 5 m", price: "119 kr", note: "No popup configured"),
        Product(id: 6, name: "Work light LED", price: "399 kr", note: "No popup configured"),
    ]

    static func product(_ id: Int) -> Product { products.first { $0.id == id } ?? products[0] }
    static func productPath(_ id: Int) -> String { "/detail/\(id)" }

    static let home = "/home"
    static let search = "/search"
    static let account = "/account"
    static let checkout = "/checkout"
    static let diagnostics = "/diagnostics"
    static let testCases = "/test-cases"

    static let customEvent = "custom-event"
    static let addToCartClick = "add-to-cart"
}

/// Registro de lo que pasa en la app y en el SDK (pantalla de diagnóstico y consola, prefijo
/// `[DeepdotsRef]`). Los listeners del SDK pueden llegar desde otro hilo.
enum HostLog {
    static let didChange = Notification.Name("HostLogDidChange")
    private static var lines: [String] = []
    private static let lock = NSLock()
    private static let clock: DateFormatter = {
        let f = DateFormatter(); f.dateFormat = "HH:mm:ss.SSS"; return f
    }()

    static func add(_ message: String) {
        print("[DeepdotsRef] \(message)")
        lock.lock()
        lines.insert("\(clock.string(from: Date()))  \(message)", at: 0)
        if lines.count > 200 { lines.removeLast() }
        lock.unlock()
        DispatchQueue.main.async { NotificationCenter.default.post(name: didChange, object: nil) }
    }

    static func snapshot() -> [String] { lock.lock(); defer { lock.unlock() }; return lines }
}

/// Sonda de respuesta: si tras abrir y cerrar un popup el contador deja de subir al pulsar, la app
/// ha quedado bloqueada.
enum Probe {
    static let didChange = Notification.Name("ProbeDidChange")
    private(set) static var taps = 0
    static func tap() {
        taps += 1
        HostLog.add("probe tap #\(taps)")
        NotificationCenter.default.post(name: didChange, object: nil)
    }
    static var label: String { "Probe taps: \(taps)" }
}

/// Usuario "logueado" de la app (persistido, como haría un cliente).
enum Session {
    private static let key = "reference.userId"
    static var userId: String? {
        get { UserDefaults.standard.string(forKey: key) }
        set { UserDefaults.standard.set(newValue, forKey: key) }
    }
}

enum Settings {
    private static let key = "reference.fireEventOnColdStart"
    static var fireEventOnColdStart: Bool {
        get { UserDefaults.standard.bool(forKey: key) }
        set { UserDefaults.standard.set(newValue, forKey: key) }
    }
}

func fireEvent() {
    AppDelegate.sdk.triggerEvent(eventName: Catalog.customEvent)
    HostLog.add("triggerEvent \(Catalog.customEvent)")
}

func setPath(_ path: String) {
    AppDelegate.sdk.setPath(path: path)
    HostLog.add("setPath \(path)")
}

/// Pantalla de botones apilados con scroll. Todas llevan la sonda arriba y un
/// `accessibilityIdentifier` estable en cada control, para los E2E.
class StackViewController: UIViewController {
    let stack = UIStackView()
    let scroll = UIScrollView()
    private let probe = UIButton(type: .system)
    /// Ruta que se pasa a `setPath()` al aparecer (nil = la pantalla no informa ruta).
    var trackedPath: String? { nil }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        scroll.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scroll)
        stack.axis = .vertical
        stack.spacing = 12
        stack.translatesAutoresizingMaskIntoConstraints = false
        scroll.addSubview(stack)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            stack.topAnchor.constraint(equalTo: scroll.contentLayoutGuide.topAnchor, constant: 16),
            stack.bottomAnchor.constraint(equalTo: scroll.contentLayoutGuide.bottomAnchor, constant: -16),
            stack.leadingAnchor.constraint(equalTo: scroll.frameLayoutGuide.leadingAnchor, constant: 16),
            stack.trailingAnchor.constraint(equalTo: scroll.frameLayoutGuide.trailingAnchor, constant: -16),
        ])
        probe.configuration = .tinted()
        probe.accessibilityIdentifier = "probe_button"
        probe.addAction(UIAction { _ in Probe.tap() }, for: .touchUpInside)
        stack.addArrangedSubview(probe)
        NotificationCenter.default.addObserver(self, selector: #selector(refreshProbe), name: Probe.didChange, object: nil)
        refreshProbe()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        refreshProbe()
        if let path = trackedPath { setPath(path) }
    }

    @objc private func refreshProbe() { probe.setTitle(Probe.label, for: .normal) }

    @discardableResult
    func addButton(_ title: String, id: String, style: UIButton.Configuration = .bordered(), action: @escaping () -> Void) -> UIButton {
        let button = UIButton(type: .system)
        button.configuration = style
        button.setTitle(title, for: .normal)
        button.accessibilityIdentifier = id
        button.addAction(UIAction { _ in action() }, for: .touchUpInside)
        stack.addArrangedSubview(button)
        return button
    }

    @discardableResult
    func addLabel(_ text: String, font: UIFont = .preferredFont(forTextStyle: .body), id: String? = nil) -> UILabel {
        let label = UILabel()
        label.text = text
        label.font = font
        label.numberOfLines = 0
        label.accessibilityIdentifier = id
        stack.addArrangedSubview(label)
        return label
    }
}
