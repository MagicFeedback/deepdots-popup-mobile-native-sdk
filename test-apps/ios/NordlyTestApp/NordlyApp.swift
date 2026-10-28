import SwiftUI

@main
struct NordlyApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @State private var wentToBackground = false

    var body: some Scene {
        WindowGroup {
            ContentView().preferredColorScheme(.light)
        }
        .onChange(of: scenePhase) { phase in
            switch phase {
            case .background:
                wentToBackground = true
                SDKHost.shared.didEnterBackground()
            case .active where wentToBackground:
                wentToBackground = false
                SDKHost.shared.willEnterForeground()
            default:
                break
            }
        }
    }
}

struct Product: Identifiable, Equatable {
    let id: String, name: String, detail: String, price: Int
}

let products = [
    Product(id: "fjell-jacket", name: "Fjell Jacket", detail: "Waterproof · M · Green", price: 89),
    Product(id: "wool-beanie", name: "Wool Beanie", detail: "One size · Grey", price: 24),
    Product(id: "ull-socks", name: "Ull Socks", detail: "39–42", price: 12),
]

/// Screens. `path` is what the app passes to setPath(); segments.path in DEV matches on it.
enum Route: Equatable {
    case home, shop, product(Product), cart, checkout, account

    var path: String {
        switch self {
        case .home: return "/"
        case .shop: return "/shop"
        case .product(let p): return "/product/\(p.id)"
        case .cart: return "/cart"
        case .checkout: return "/checkout"
        case .account: return "/account"
        }
    }

    var title: String {
        switch self {
        case .home: return "NORDLY"
        case .shop: return "Shop"
        case .product(let p): return p.name
        case .cart: return "Cart"
        case .checkout: return "Checkout"
        case .account: return "Account"
        }
    }

    static func stack(start: String) -> [Route] {
        switch start {
        case "shop": return [.shop]
        case "cart": return [.cart]
        case "checkout": return [.cart, .checkout]
        case "account": return [.account]
        default: return [.home]
        }
    }
}

let ink = Color(red: 0.07, green: 0.09, blue: 0.15)
let labBg = Color(red: 0.06, green: 0.07, blue: 0.08)
let labText = Color(red: 0.79, green: 0.80, blue: 0.83)

struct ContentView: View {
    @ObservedObject private var host = SDKHost.shared
    @ObservedObject private var lab = Lab.shared
    @State private var stack: [Route] = Route.stack(start: SDKHost.shared.config.start)
    @State private var cart: [Product] = [products[0], products[1]]
    @State private var previousPath: String?
    @State private var showLab = false
    @State private var showConsent = LabConfig.consent == nil
    @State private var showConfirm = false
    @State private var rateProduct: Product?
    @State private var toast: String?

    private var config: LabConfig { host.config }
    private var current: Route { stack.last ?? .home }

    var body: some View {
        VStack(spacing: 0) {
            labBar
            header
            ZStack(alignment: .bottom) {
                screen.frame(maxWidth: .infinity, maxHeight: .infinity)
                if let toast { Text(toast).padding(12).background(ink).foregroundColor(.white).cornerRadius(10).padding(12) }
            }
            labLog
            if current != .checkout { tabBar }
        }
        .background(Color(red: 0.98, green: 0.98, blue: 0.98))
        .onAppear {
            if config.load == "immediate" || LabConfig.consent == "all" { host.initSDK() }
            navigated()
        }
        .onChange(of: current.path) { _ in navigated() }
        .alert("Cookies", isPresented: $showConsent) {
            Button("Necessary only", role: .cancel) {
                LabConfig.consent = "necessary"
                Lab.log("host", "consent necessary only")
            }
            Button("Accept all") {
                LabConfig.consent = "all"
                Lab.log("host", "consent accepted")
                host.initSDK()
                if config.nav != "silent" { host.setPath(current.path) }
            }
        } message: { Text("We use cookies to improve your experience.") }
        .alert("Place order?", isPresented: $showConfirm) {
            Button("Cancel", role: .cancel) {}
            Button("Confirm") {
                cart.removeAll()
                Lab.log("host", "order placed")
                flash("Order placed")
                stack = [.home]
            }
        } message: { Text("Total \(cart.reduce(0) { $0 + $1.price }),00 €") }
        .sheet(item: $rateProduct) { product in
            RateSheet(product: product) { stars in
                Lab.log("host", "rate \(product.id) stars=\(stars) → triggerEvent(rate_product)")
                rateProduct = nil
                // The SDK presents from the root controller: wait until the sheet is gone.
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) { host.triggerEvent("rate_product") }
            }
            .presentationDetents([.medium])
        }
        .fullScreenCover(isPresented: $showLab) {
            LabPanel(onClose: { showLab = false }, onEvent: { e in
                showLab = false
                Lab.log("host", "triggerEvent(\(e))")
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) { host.triggerEvent(e) }
            })
        }
    }

    private func navigated() {
        let path = current.path
        Lab.log("nav", "\(previousPath ?? "launch") → \(path)\(config.nav == "silent" ? " (silent: no setPath)" : "")")
        if config.nav != "silent" { host.setPath(path) }
        previousPath = path
    }

    private func flash(_ message: String) {
        toast = message
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) { toast = nil }
    }

    private func push(_ r: Route) { stack.append(r) }
    private func tab(_ r: Route) { stack = [r] }

    // MARK: chrome

    private var labBar: some View {
        HStack(spacing: 8) {
            Text("SDK Lab").bold().foregroundColor(.white)
            Text("\(sdkVersion) · SPM").foregroundColor(labText).accessibilityIdentifier("lab-sdk-version")
            Text("DEV").foregroundColor(Color(red: 0.43, green: 0.91, blue: 0.66)).accessibilityIdentifier("lab-env")
            Text(config.scenario).foregroundColor(Color(white: 0.62)).accessibilityIdentifier("lab-scenario")
            Spacer()
            Button { showLab = true } label: { Image(systemName: "gearshape.fill").foregroundColor(labText) }
                .frame(width: 44, height: 36)
                .accessibilityLabel("Open SDK Lab").accessibilityIdentifier("lab-open")
        }
        .font(.system(size: 12))
        .padding(.horizontal, 12)
        .frame(height: 40)
        .background(labBg)
    }

    private var header: some View {
        HStack(spacing: 4) {
            if stack.count > 1 {
                Button { stack.removeLast() } label: { Image(systemName: "chevron.left").font(.system(size: 18, weight: .semibold)) }
                    .frame(width: 44, height: 44)
                    .accessibilityLabel("Back").accessibilityIdentifier("back")
            }
            Text(current.title).font(.system(size: 22, weight: .bold))
            Spacer()
        }
        .padding(.horizontal, stack.count > 1 ? 4 : 16)
        .frame(height: 52)
        .foregroundColor(ink)
    }

    private var labLog: some View {
        VStack(alignment: .leading, spacing: 2) {
            ForEach(Array(lab.lines.suffix(3).enumerated()), id: \.offset) { _, line in
                Text(line).lineLimit(1)
            }
        }
        .font(.system(size: 10, design: .monospaced))
        .foregroundColor(labText)
        .frame(maxWidth: .infinity, minHeight: 56, alignment: .topLeading)
        .padding(.horizontal, 12).padding(.vertical, 6)
        .background(labBg)
        .accessibilityIdentifier("lab-log")
    }

    private var tabBar: some View {
        HStack {
            tabButton("Home", "house.fill", "tab-home", .home)
            tabButton("Shop", "bag.fill", "tab-shop", .shop)
            tabButton("Cart (\(cart.count))", "cart.fill", "tab-cart", .cart)
            tabButton("Account", "person.crop.circle.fill", "tab-account", .account)
        }
        .padding(.top, 6)
        .background(Color(white: 0.97))
    }

    private func tabButton(_ label: String, _ icon: String, _ id: String, _ root: Route) -> some View {
        let selected = stack.first == root
        return Button { tab(root) } label: {
            VStack(spacing: 2) { Image(systemName: icon); Text(label).font(.system(size: 11)) }
                .frame(maxWidth: .infinity, minHeight: 44)
                .foregroundColor(selected ? .blue : Color(white: 0.35))
        }
        .accessibilityIdentifier(id)
    }

    // MARK: screens

    @ViewBuilder private var screen: some View {
        switch current {
        case .home:
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("New season, same rain.").font(.system(size: 24, weight: .semibold))
                    Text("Free shipping over 60 € · 30-day returns").foregroundColor(.secondary)
                    primaryButton("Shop new arrivals", "home-shop") { tab(.shop) }
                    Text("Featured").font(.headline).padding(.top, 8)
                    ForEach(products.prefix(2)) { p in productRow(p) { push(.product(p)) } }
                }.padding(16)
            }
        case .shop:
            ScrollView { VStack(spacing: 4) { ForEach(products) { p in productRow(p) { push(.product(p)) } } }.padding(16) }
        case .product(let p):
            ScrollView {
                VStack(alignment: .leading, spacing: 8) {
                    Rectangle().fill(Color(red: 0.84, green: 0.83, blue: 0.79)).frame(height: 240)
                    VStack(alignment: .leading, spacing: 8) {
                        Text(p.name).font(.system(size: 22, weight: .semibold))
                        Text("\(p.price),00 €")
                        Text(p.detail).foregroundColor(.secondary)
                        HStack(spacing: 8) {
                            primaryButton("Add to cart", "add-to-cart") {
                                cart.append(p)
                                Lab.log("host", "add to cart \(p.id) (cart \(cart.count))")
                                flash("Added to cart")
                            }
                            secondaryButton("Rate", "rate-product") { rateProduct = p }
                        }.padding(.top, 8)
                    }.padding(16)
                }
            }
        case .cart:
            ScrollView {
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(cart.count) items").foregroundColor(.secondary).accessibilityIdentifier("cart-count")
                    ForEach(Array(cart.enumerated()), id: \.offset) { _, p in productRow(p) {} }
                    primaryButton("Go to checkout", "go-checkout") { push(.checkout) }
                        .disabled(cart.isEmpty).padding(.top, 12)
                }.padding(16)
            }
        case .checkout:
            CheckoutView(total: cart.reduce(0) { $0 + $1.price }) { showConfirm = true }
        case .account:
            VStack(alignment: .leading, spacing: 12) {
                Text("Test User").font(.system(size: 20, weight: .semibold))
                Text("SDK user id: \(host.initialized ? host.sdk.getUserId() ?? "—" : "—")")
                    .font(.footnote).foregroundColor(.secondary).accessibilityIdentifier("account-user-id")
                Divider()
                Text("Orders"); Text("Addresses")
                Divider()
                secondaryButton("Help", "help-button") {
                    Lab.log("host", "click help-button")
                    host.triggerClick("help-button")
                }
                Spacer()
            }.padding(16)
        }
    }

    private func productRow(_ p: Product, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 12) {
                RoundedRectangle(cornerRadius: 8).fill(Color(red: 0.84, green: 0.83, blue: 0.79)).frame(width: 56, height: 56)
                VStack(alignment: .leading) {
                    Text(p.name).foregroundColor(ink)
                    Text(p.detail).font(.footnote).foregroundColor(.secondary)
                }
                Spacer()
                Text("\(p.price),00 €").foregroundColor(ink)
            }.padding(.vertical, 8)
        }
        .accessibilityIdentifier("product-\(p.id)")
    }
}

func primaryButton(_ title: String, _ id: String, action: @escaping () -> Void) -> some View {
    Button(action: action) {
        Text(title).fontWeight(.semibold).frame(maxWidth: .infinity, minHeight: 48)
            .background(ink).foregroundColor(.white).cornerRadius(24)
    }.accessibilityIdentifier(id)
}

func secondaryButton(_ title: String, _ id: String, action: @escaping () -> Void) -> some View {
    Button(action: action) {
        Text(title).frame(maxWidth: .infinity, minHeight: 48)
            .overlay(RoundedRectangle(cornerRadius: 24).stroke(Color(white: 0.8)))
            .foregroundColor(ink)
    }.accessibilityIdentifier(id)
}

struct CheckoutView: View {
    let total: Int
    let onPlaceOrder: () -> Void
    @State private var email = "qa+ios@deepdots.dev"
    @State private var address = "1 Test Street"

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Cart › Shipping › Payment").font(.caption).foregroundColor(.secondary)
                TextField("Email", text: $email).textFieldStyle(.roundedBorder).accessibilityIdentifier("checkout-email")
                TextField("Address", text: $address).textFieldStyle(.roundedBorder)
                HStack { Text("Total").bold(); Spacer(); Text("\(total),00 €").bold() }
                primaryButton("Place order", "place-order", action: onPlaceOrder)
            }.padding(16)
        }
    }
}

struct RateSheet: View {
    let product: Product
    let onSubmit: (Int) -> Void
    @State private var stars = 4

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Rate the \(product.name)").font(.system(size: 18, weight: .semibold))
            HStack(spacing: 8) {
                ForEach(1...5, id: \.self) { n in
                    Button { stars = n } label: {
                        Text("\(n)").frame(width: 52, height: 52)
                            .background(n == stars ? ink : Color.white)
                            .foregroundColor(n == stars ? .white : ink)
                            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(white: 0.8)))
                            .cornerRadius(8)
                    }.accessibilityIdentifier("star-\(n)")
                }
            }
            primaryButton("Submit rating", "submit-rating") { onSubmit(stars) }
            Spacer()
        }
        .padding(20)
    }
}
