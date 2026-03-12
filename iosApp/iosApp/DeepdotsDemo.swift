import SwiftUI
import ComposeApp
import UIKit

private enum IOSScreen { case login, home, detail(EventItem) }

private struct EventItem: Identifiable {
    let id = UUID()
    let title: String
    let description: String
    let path: String
    let eventName: String?
}

struct DeepdotsDemoView: View {
    @State private var screen: IOSScreen = .login
    @State private var selectedUserId: String = "alpha-01"
    @State private var customUserId: String = ""
    @State private var events: [EventItem] = [
        EventItem(title: "Event 1", description: "Popup on enter", path: "/detail/1", eventName: nil),
        EventItem(title: "Event 2", description: "Popup on scroll", path: "/detail/2", eventName: nil),
        EventItem(title: "Event 3", description: "Popup on exit", path: "/detail/3", eventName: nil),
        EventItem(title: "Event 4", description: "Popup on custom event", path: "/detail/4", eventName: "custom-event")
    ]
    @State private var popups: ComposeApp.DeepdotsPopups? = nil

    // Server config
    private let publicKey: String = "12mGEGK4YXHXHrxZ45bJOsH6fiOl6ew1"

    var body: some View {
        VStack(spacing: 0) {
            switch screen {
            case .login:
                LoginView(
                    selectedUserId: $selectedUserId,
                    customUserId: $customUserId,
                    onStart: startDemo,
                    onSelectUser: { _ in startDemo() }
                )
            case .home:
                HomeView(events: events, onSelect: { item in
                    setPath(item.path)
                    screen = .detail(item)
                }, onLogout: {
                    // Cerrar sesión: limpiar instancia y volver a selector
                    popups = nil
                    screen = .login
                })
            case .detail(let item):
                DetailView(item: item, onBack: {
                    setPath("/detail"); screen = .home
                }, onScrollPercent: { pct in
                    popups?.onScroll(percentage: Int32(pct))
                }, onTriggerEvent: { eventName in
                    popups?.triggerEvent(eventName: eventName)
                })
            }
        }
        .onAppear { attachRootContextIfPossible() }
    }

    // Inicializa el SDK con el userId elegido y navega a Home
    private func startDemo() {
        let uid = customUserId.isEmpty ? selectedUserId : customUserId
        let provideLang: () -> String = { Locale.current.language.languageCode?.identifier ?? "en" }
        let options = InitOptions(
            debug: true,
            mode: Mode.server,
            popupOptions: PopupOptions(
                id: nil,
                publicKey: publicKey,
                popups: nil,
                companyId: nil
            ),
            provideLang: provideLang,
            autoLaunch: true,
            storage: nil,
            metadata: ["userId": uid]
        )
        let instance = ComposeApp.DeepdotsPopups()
        instance.initialize(options: options)
        wireEvents(instance)
        popups = instance
        setPath("/home")
        attachRootContextIfPossible()
        screen = .home
    }

    private func wireEvents(_ instance: ComposeApp.DeepdotsPopups) {
        instance.on(event: Events.shared.popupShown) { event in
            print("[iOS] popupShown popupId=\(event.popupId)")
        }
        instance.on(event: Events.shared.popupClicked) { event in
            print("[iOS] popupClicked popupId=\(event.popupId) action=\(event.extra["action"] ?? "")")
        }
        instance.on(event: Events.shared.surveyCompleted) { event in
            print("[iOS] surveyCompleted surveyId=\(event.surveyId)")
        }
    }

    private func attachRootContextIfPossible() {
        guard let instance = popups else { return }
        if let root = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
            .first?.rootViewController {
            instance.attachContext(context: PlatformContext(viewController: root))
        }
    }

    private func setPath(_ path: String) {
        popups?.setPath(path: path)
    }
}

// MARK: - Login
private struct LoginView: View {
    @Binding var selectedUserId: String
    @Binding var customUserId: String
    var onStart: () -> Void
    var onSelectUser: (String) -> Void

    private let presets = ["alpha-01", "beta-01", "gamma-01"]

    var body: some View {
        VStack(spacing: 16) {
            Text("deepdots").font(.largeTitle).bold()
            Text("Select a User ID or enter a custom one")
                .font(.subheadline)
                .foregroundColor(.secondary)

            // Vertical block for User ID selection
            VStack(alignment: .leading, spacing: 8) {
                Text("User ID").font(.caption).foregroundColor(.secondary)
                Picker("User ID", selection: $selectedUserId) {
                    ForEach(presets, id: \.self) { id in Text(id).tag(id) }
                }
                .pickerStyle(.segmented)
                .onChange(of: selectedUserId) { oldValue, newValue in onSelectUser(newValue) }
            }
            .padding(.horizontal, 24)

            // Separation line between picker and text field
            Divider()
                .padding(.horizontal, 24)

            TextField("Custom User ID", text: $customUserId)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal, 24)

            Button(action: onStart) {
                Text("Start").frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .padding(.horizontal, 24)
            .padding(.top, 8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        .padding(.horizontal, 16)
    }
}

// MARK: - Home
private struct HomeView: View {
    var events: [EventItem]
    var onSelect: (EventItem) -> Void
    var onLogout: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack {
                Text("deepdots").font(.largeTitle).bold()
                Spacer()
                Button("Sign out", action: onLogout)
            }
            .padding(16)

            // Demo entries
            ScrollView {
                VStack(spacing: 12) {
                    ForEach(events) { item in
                        EventCard(item: item) { onSelect(item) }
                    }
                }
                .padding(16)
            }
        }
    }
}

private struct EventCard: View {
    var item: EventItem
    var onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack {
                VStack(alignment: .leading) {
                    Text(item.title).font(.headline)
                    Text(item.description).font(.caption).foregroundColor(.secondary)
                }
                Spacer()
                Image(systemName: "chevron.right").foregroundColor(.secondary)
            }
            .padding(16)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.gray.opacity(0.12)))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Detail
private struct DetailView: View {
    var item: EventItem
    var onBack: () -> Void
    var onScrollPercent: (Int) -> Void = { _ in }
    var onTriggerEvent: (String) -> Void = { _ in }

    private let lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. "

    @State private var contentHeight: CGFloat = 1
    @State private var scrollOffset: CGFloat = 0
    @State private var lastScrollPct: Int = -1
    @State private var viewportHeight: CGFloat = 1

    private func emitScrollPercent(offset: CGFloat? = nil, height: CGFloat? = nil, viewport: CGFloat? = nil) {
        if let o = offset { scrollOffset = o }
        if let h = height { contentHeight = h }
        if let v = viewport { viewportHeight = v }
        let visibleHeight = viewportHeight
        let pct: Int
        if contentHeight <= visibleHeight {
            pct = 0
        } else {
            let range = max(contentHeight - visibleHeight, 1)
            let raw = (scrollOffset / range) * 100
            pct = Int(min(max(raw, 0), 100))
        }
        // print("[iOS Demo] scroll debug offset=\(scrollOffset) contentHeight=\(contentHeight) viewport=\(visibleHeight) pct=\(pct)")
        if pct != lastScrollPct {
            lastScrollPct = pct
            onScrollPercent(pct)
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button("Back", action: onBack)
                Spacer()
                Text(item.title).font(.headline)
                Spacer()
            }
            .padding(16)

            OffsettableScrollView(
                contentHeight: $contentHeight,
                viewportHeight: $viewportHeight,
                onScroll: { offset in emitScrollPercent(offset: offset) }
            ) {
                VStack(alignment: .leading, spacing: 12) {
                    if let eventName = item.eventName {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Trigger demo event")
                                .font(.headline)
                            Text("Tap the button below to fire \(eventName) from the host app and test event-based popups.")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            Button("Launch \(eventName)") {
                                onTriggerEvent(eventName)
                            }
                            .buttonStyle(.borderedProminent)
                        }
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color.gray.opacity(0.12)))
                    }
                    ForEach(0..<20) { _ in
                        Text(lorem)
                    }
                }
                .padding(16)
            }
        }
    }
}

// MARK: - OffsettableScrollView (UIKit bridge to get reliable scroll offset)
private struct OffsettableScrollView<Content: View>: UIViewRepresentable {
    @Binding var contentHeight: CGFloat
    @Binding var viewportHeight: CGFloat
    var onScroll: (CGFloat) -> Void
    let content: Content

    init(contentHeight: Binding<CGFloat>, viewportHeight: Binding<CGFloat>, onScroll: @escaping (CGFloat) -> Void, @ViewBuilder content: () -> Content) {
        self._contentHeight = contentHeight
        self._viewportHeight = viewportHeight
        self.onScroll = onScroll
        self.content = content()
    }

    func makeCoordinator() -> Coordinator { Coordinator(parent: self) }

    func makeUIView(context: Context) -> UIScrollView {
        let scrollView = UIScrollView()
        scrollView.alwaysBounceVertical = true
        scrollView.delegate = context.coordinator
        scrollView.showsVerticalScrollIndicator = true

        let host = context.coordinator.hostingController
        host.view.translatesAutoresizingMaskIntoConstraints = false
        host.view.backgroundColor = .clear
        host.rootView = content
        scrollView.addSubview(host.view)

        NSLayoutConstraint.activate([
            host.view.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor),
            host.view.trailingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.trailingAnchor),
            host.view.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor),
            host.view.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor),
            host.view.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor)
        ])

        return scrollView
    }

    func updateUIView(_ scrollView: UIScrollView, context: Context) {
        context.coordinator.parent = self
        context.coordinator.hostingController.rootView = content
        scrollView.layoutIfNeeded()
        context.coordinator.updateMetrics(scrollView)
    }

    class Coordinator: NSObject, UIScrollViewDelegate {
        var parent: OffsettableScrollView
        let hostingController: UIHostingController<Content>

        init(parent: OffsettableScrollView) {
            self.parent = parent
            self.hostingController = UIHostingController(rootView: parent.content)
            self.hostingController.view.backgroundColor = .clear
        }

        func updateMetrics(_ scrollView: UIScrollView) {
            let viewportHeight = scrollView.bounds.height
            let contentHeight = scrollView.contentSize.height

            DispatchQueue.main.async {
                if self.parent.viewportHeight != viewportHeight {
                    self.parent.viewportHeight = viewportHeight
                }
                if self.parent.contentHeight != contentHeight {
                    self.parent.contentHeight = contentHeight
                }
            }
        }

        func scrollViewDidScroll(_ scrollView: UIScrollView) {
            updateMetrics(scrollView)
            let offset = scrollView.contentOffset.y
            DispatchQueue.main.async {
                self.parent.onScroll(offset)
            }
        }

        func scrollViewDidLayoutSubviews(_ scrollView: UIScrollView) {
            updateMetrics(scrollView)
        }
    }
}

struct DeepdotsDemoView_Previews: PreviewProvider {
    static var previews: some View {
        DeepdotsDemoView()
    }
}
