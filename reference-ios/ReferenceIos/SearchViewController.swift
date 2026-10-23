import SwiftUI
import UIKit

/// Pantalla en SwiftUI dentro de un `UIHostingController`, como las apps que migran por partes.
final class SearchViewController: UIHostingController<SearchView> {
    init() { super.init(rootView: SearchView()) }
    required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Search"
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        setPath(Catalog.search)
    }
}

struct SearchView: View {
    @State private var query = ""
    @State private var results: [Catalog.Product] = []
    @State private var searched = false
    @State private var probe = Probe.taps

    var body: some View {
        Form {
            Button("Probe taps: \(probe)") { Probe.tap(); probe = Probe.taps }
                .accessibilityIdentifier("probe_button")
            TextField("Search products", text: $query)
                .accessibilityIdentifier("search_input")
                .onSubmit(search)
            Button("Search", action: search).accessibilityIdentifier("search_button")
            if searched {
                if results.isEmpty { Text("No results") }
                ForEach(results, id: \.id) { Text($0.name) }
            }
        }
        .onAppear { probe = Probe.taps }
    }

    private func search() {
        let q = query.trimmingCharacters(in: .whitespaces)
        results = Catalog.products.filter { $0.name.localizedCaseInsensitiveContains(q) }
        searched = true
        AppDelegate.sdk.trackSearch(query: q, resultsCount: Int32(results.count), params: nil)
        HostLog.add("trackSearch '\(q)' results=\(results.count)")
    }
}
