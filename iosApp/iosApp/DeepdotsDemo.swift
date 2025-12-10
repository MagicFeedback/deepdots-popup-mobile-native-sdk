import SwiftUI
import ComposeApp

private enum IOSScreen { case home, fake }

struct DeepdotsDemoView: View {
    @State private var counter: Int = 0
    @State private var screen: IOSScreen = .home
    private let popups: DeepdotsPopups // use popups API

    init() {
        // Popup 1
        let popupDef = PopupDefinition(
            id: "popup-welcome",
            title: "",
            message: "",
            trigger: Trigger.TimeOnPage(value: 3, condition: [Condition(answered: false, cooldownDays: 1)]),
            conditions: [],
            actions: Actions(
                accept: Action.Accept(label: "Send", surveyId: "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0"),
                decline: Action.Decline(label: "Cancel", cooldownDays: 1),
                start: nil,
                complete: nil,
                back: nil
            ),
            surveyId: "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0",
            productId: "02b809f20e024bce47c57f123cff8735",
            style: Style(theme: Theme.light, position: Position.center, imageUrl: nil, imageSize: ImageSize.medium, imageAlignment: ImageAlignment.center),
            segments: Segments(lang: ["en"], path: ["/home"])
        )
        // Popup 2
        let popupDef2 = PopupDefinition(
            id: "popup-demo-1",
            title: "",
            message: "",
            trigger: Trigger.TimeOnPage(value: 1, condition: [Condition(answered: false, cooldownDays: 1)]),
            conditions: [],
            actions: Actions(
                accept: Action.Accept(label: "Næste", surveyId: "eeb4e590-d0eb-11f0-b3ab-f13d725acff5"),
                decline: Action.Decline(label: "Tæt", cooldownDays: 1),
                start: Action.Start(label: "Start"),
                complete: Action.Complete(label: "Indsend"),
                back: Action.Back(label: "Tilbage")
            ),
            surveyId: "eeb4e590-d0eb-11f0-b3ab-f13d725acff5",
            productId: "e5c8241506ac83ddcf061a01f5b0f567",
            style: Style(theme: Theme.light, position: Position.center, imageUrl: nil, imageSize: ImageSize.medium, imageAlignment: ImageAlignment.center),
            segments: Segments(lang: ["en"], path: ["/fake"])
        )

        let options = InitOptions(
            debug: true,
            mode: Mode.client,
            popupOptions: PopupOptions(
                id: "main-options",
                publicKey: nil,
                popups: [popupDef, popupDef2],
                companyId: nil
            ),
            provideLang: { "en" },
            autoLaunch: true,
            storage: nil,
            metadata: nil
        )

        // Create popups instance and initialize with options
        let instance = DeepdotsPopups()
        instance.initialize(options: options)
        self.popups = instance

        // Set initial path
        self.popups.setPath(path: "/home")

        popups.on(event: Events.shared.popupShown) { event in
            print("[iOS] popupShown popupId=\(event.popupId)")
        }
        popups.on(event: Events.shared.popupClicked) { event in
            print("[iOS] popupClicked popupId=\(event.popupId) action=\(event.extra["action"] ?? "")")
        }
        popups.on(event: Events.shared.surveyCompleted) { event in
            print("[iOS] surveyCompleted surveyId=\(event.surveyId)")
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            switch screen {
            case .home:
                HomeView(onNavigate: { setPathAndGo(to: .fake) })
            case .fake:
                FakeView(onBack: { setPathAndGo(to: .home) }, onManualShow: manualShow)
            }
        }
        .onAppear { attachRootContext() }
    }

    private func setPathAndGo(to target: IOSScreen) {
        screen = target
        let path = (target == .home) ? "/home" : "/fake"
        popups.setPath(path: path)
    }

    private func manualShow() {
        counter += 1
        if let root = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
            .first?.rootViewController {
            popups.show(options: ShowOptions(surveyId: "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0", productId: "02b809f20e024bce47c57f123cff8735", data: ["source": "manual_button_ios"]), context: PlatformContext(viewController: root))
        }
    }

    private func attachRootContext() {
        if let root = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
            .first?.rootViewController {
            popups.attachContext(context: PlatformContext(viewController: root))
        }
    }
}

private struct HomeView: View {
    var onNavigate: () -> Void
    var body: some View {
        VStack(spacing: 24) {
            Spacer()
            ZStack {
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.blue.opacity(0.2))
                    .frame(width: 140, height: 140)
                Text("Deepdots")
                    .font(.headline)
            }
            Spacer()
            Button(action: onNavigate) {
                Text("Go to test page")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .padding(.horizontal, 24)
            .padding(.bottom, 24)
        }
    }
}

private struct FakeView: View {
    var onBack: () -> Void
    var onManualShow: () -> Void
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Test page").font(.title3)
            Text("Fake content with elements to try popups.")
            HStack(spacing: 12) {
                Button("Show popup manually", action: onManualShow)
                Button("Back", action: onBack)
                    .buttonStyle(.bordered)
            }
            Divider()
            Button("Action 1") {}
            Button("Action 2") {}
            Button("Open fake dialog") {}
            Spacer()
        }
        .padding(16)
    }
}

struct DeepdotsDemoView_Previews: PreviewProvider {
    static var previews: some View {
        DeepdotsDemoView()
    }
}
