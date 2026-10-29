import SwiftUI

/// SDK Lab: what this run is (version, env, ids), what to test next, and the run log.
struct LabPanel: View {
    let onClose: () -> Void
    let onEvent: (String) -> Void
    @ObservedObject private var host = SDKHost.shared
    @ObservedObject private var lab = Lab.shared
    @State private var draft = SDKHost.shared.config
    @State private var restartNote = false

    var body: some View {
        NavigationView {
            Form {
                Section {
                    info("SDK", "\(sdkVersion) · SPM", "lab-info-version")
                    info("Environment", "DEV · \(devAPI.replacingOccurrences(of: "https://", with: ""))", "lab-info-env")
                    info("Public key", host.config.publicKey, "lab-info-key")
                    info("User id", host.initialized ? host.sdk.getUserId() ?? "—" : "not initialized", "lab-info-user")
                    info("Session id", host.initialized ? host.sdk.getSessionId() ?? "—" : "—", "lab-info-session")
                }
                Section("Run") {
                    Picker("Scenario", selection: $draft.scenario) {
                        ForEach(Scenarios.names, id: \.self) { Text($0).tag($0) }
                    }.accessibilityIdentifier("lab-scenario-picker")
                    Picker("Navigation", selection: $draft.nav) { Text("history").tag("history"); Text("silent").tag("silent") }
                    Picker("Language", selection: $draft.lang) { Text("en").tag("en"); Text("es").tag("es") }
                    Picker("Load", selection: $draft.load) { Text("immediate").tag("immediate"); Text("consent").tag("consent") }
                    Button("Apply & quit") { draft.save(); restartNote = true }
                        .accessibilityIdentifier("lab-apply")
                    Button("Reset", role: .destructive) { host.reset(); restartNote = true }
                        .accessibilityIdentifier("lab-reset")
                }
                let events = Scenarios.events(host.config.scenario)
                if !events.isEmpty {
                    Section("Host events") {
                        ForEach(events, id: \.self) { e in
                            Button(e) { onEvent(e) }.accessibilityIdentifier("lab-event-\(e)")
                        }
                    }
                }
                Section("Log · os_log [lab]") {
                    VStack(alignment: .leading, spacing: 3) {
                        ForEach(Array(lab.lines.suffix(40).enumerated()), id: \.offset) { _, line in Text(line) }
                    }
                    .font(.system(size: 10, design: .monospaced))
                    .accessibilityIdentifier("lab-panel-log")
                }
            }
            .navigationTitle("SDK Lab")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Close", action: onClose).accessibilityIdentifier("lab-close")
                }
            }
            // iOS apps cannot relaunch themselves: the new config applies on the next launch.
            .alert("Saved", isPresented: $restartNote) {
                Button("Quit app") { exit(0) }
            } message: { Text("The app will quit. Launch it again to start the new run.") }
        }
    }

    private func info(_ label: String, _ value: String, _ id: String) -> some View {
        HStack {
            Text(label)
            Spacer()
            Text(value).font(.footnote.weight(.semibold)).multilineTextAlignment(.trailing).accessibilityIdentifier(id)
        }
    }
}
