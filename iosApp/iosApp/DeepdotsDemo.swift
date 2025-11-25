import SwiftUI
import ComposeApp

struct DeepdotsDemoView: View {
    @State private var counter: Int = 0
    private let popups: DeepdotsPopups

    init() {
        self.popups = Deepdots.shared.createInitializedSimple(
            id: "popup-ios",
            title: "Bienvenido iOS",
            messageHtml: "<p><b>Gracias</b> por usar la demo iOS.</p><p>¿Participas?</p>",
            surveyId: "survey-ios",
            productId: "product-ios",
            triggerSeconds: 3,
            acceptLabel: "Sí",
            declineLabel: "No",
            declineCooldownDays: 1,
            debug: true,
            autoLaunch: true,
            lang: "es",
            path: "/home"
        )
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
        VStack(spacing: 16) {
            Text("Deepdots iOS Demo")
            Button("Mostrar popup manual (\(counter))") {
                counter += 1
                if let root = UIApplication.shared.connectedScenes
                    .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                    .first?.rootViewController {
                    popups.show(options: ShowOptions(surveyId: "survey-ios", productId: "product-ios", data: ["source": "manual_button"]), context: PlatformContext(viewController: root))
                }
            }
        }
        .padding()
        .onAppear {
            if let root = UIApplication.shared.connectedScenes
                .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                .first?.rootViewController {
                popups.attachContext(context: PlatformContext(viewController: root))
            }
        }
    }
}

struct DeepdotsDemoView_Previews: PreviewProvider {
    static var previews: some View {
        DeepdotsDemoView()
    }
}
