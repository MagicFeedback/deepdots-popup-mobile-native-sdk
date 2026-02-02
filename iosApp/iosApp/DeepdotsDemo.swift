import SwiftUI
import ComposeApp

private enum IOSScreen { case login, home, detail(String) }

private struct EventItem: Identifiable {
    let id = UUID()
    let title: String
    let description: String
    let path: String
}

struct DeepdotsDemoView: View {
    @State private var screen: IOSScreen = .login
    @State private var selectedUserId: String = "alpha-01"
    @State private var customUserId: String = ""
    @State private var events: [EventItem] = [
        EventItem(title: "Event 1", description: "Popup on enter", path: "/detail/1"),
        EventItem(title: "Event 2", description: "Popup on scroll", path: "/detail/2"),
        EventItem(title: "Event 3", description: "Popup on exit", path: "/detail/3")
    ]
    @State private var popups: DeepdotsPopups? = nil

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
                    screen = .detail(item.title)
                }, onLogout: {
                    // Cerrar sesión: limpiar instancia y volver a selector
                    popups = nil
                    screen = .login
                })
            case .detail(let title):
                DetailView(title: title, onBack: {
                    setPath("/detail"); screen = .home
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
        let instance = DeepdotsPopups()
        instance.initialize(options: options)
        wireEvents(instance)
        popups = instance
        setPath("/home")
        attachRootContextIfPossible()
        screen = .home
    }

    private func wireEvents(_ instance: DeepdotsPopups) {
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

            // Cards (max 3)
            ScrollView {
                VStack(spacing: 12) {
                    ForEach(events.prefix(3)) { item in
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
    var title: String
    var onBack: () -> Void

    private let lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. "

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button("Back", action: onBack)
                Spacer()
                Text(title).font(.headline)
                Spacer()
            }
            .padding(16)

            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    ForEach(0..<20) { _ in
                        Text(lorem)
                    }
                }
                .padding(16)
            }
        }
    }
}

struct DeepdotsDemoView_Previews: PreviewProvider {
    static var previews: some View {
        DeepdotsDemoView()
    }
}
