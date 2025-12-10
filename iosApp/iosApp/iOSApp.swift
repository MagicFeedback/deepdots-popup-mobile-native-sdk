import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            // Use the demo view that already integrates Deepdots SDK on iOS
            DeepdotsDemoView()
        }
    }
}
