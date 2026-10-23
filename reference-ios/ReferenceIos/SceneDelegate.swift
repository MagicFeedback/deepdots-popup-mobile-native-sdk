import UIKit
import DeepdotsSDK

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: windowScene)
        let tabs = MainTabBarController()
        window.rootViewController = tabs
        window.makeKeyAndVisible()
        self.window = window

        AppDelegate.sdk.attachContext(context: PlatformContext(viewController: tabs))
        HostLog.add("attachContext root")

        if let url = connectionOptions.urlContexts.first?.url { open(url) }
    }

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        if let url = URLContexts.first?.url { open(url) }
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        AppDelegate.sdk.onForeground()
        HostLog.add("onForeground")
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        AppDelegate.sdk.onBackground()
        HostLog.add("onBackground")
    }

    /// `deepdots-ref://product/{id}`: abre la ficha en la pestaña Home.
    private func open(_ url: URL) {
        guard url.host == "product", let id = Int(url.lastPathComponent),
              let tabs = window?.rootViewController as? MainTabBarController else { return }
        HostLog.add("deep link \(url.absoluteString)")
        tabs.openProduct(id)
    }
}

/// Pestañas con un `UINavigationController` cada una: la forma más común de una app UIKit.
final class MainTabBarController: UITabBarController {
    private let home = UINavigationController(rootViewController: HomeViewController())

    override func viewDidLoad() {
        super.viewDidLoad()
        home.tabBarItem = UITabBarItem(title: "Home", image: UIImage(systemName: "house"), tag: 0)
        let search = UINavigationController(rootViewController: SearchViewController())
        search.tabBarItem = UITabBarItem(title: "Search", image: UIImage(systemName: "magnifyingglass"), tag: 1)
        let account = UINavigationController(rootViewController: AccountViewController())
        account.tabBarItem = UITabBarItem(title: "Account", image: UIImage(systemName: "person"), tag: 2)
        viewControllers = [home, search, account]
    }

    func openProduct(_ id: Int) {
        selectedIndex = 0
        home.popToRootViewController(animated: false)
        home.pushViewController(ProductDetailViewController(product: Catalog.product(id)), animated: true)
    }
}
