import SwiftUI
import WebKit
import ComposeApp

struct DeepdotsLogoView: View {
    let urlString: String
    let size: String? // "small" | "medium" | "large"
    let position: String? // "left" | "right" | "center"

    private var maxHeight: CGFloat {
        switch size?.lowercased() {
        case "small": return 80
        case "large": return 160
        default: return 120
        }
    }

    var body: some View {
        let alignment: HorizontalAlignment = {
            switch position?.lowercased() {
            case "left": return .leading
            case "right": return .trailing
            default: return .center
            }
        }()
        HStack {
            if alignment == .leading { /* left */ }
            if alignment == .center { Spacer() }
            AsyncImage(url: URL(string: urlString)) { phase in
                switch phase {
                case .empty:
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .frame(height: maxHeight)
                        .background(Color.gray.opacity(0.08))
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFit()
                        .frame(maxWidth: .infinity)
                        .frame(height: maxHeight)
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                case .failure:
                    Text("Image")
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity)
                        .frame(height: maxHeight)
                        .background(Color.gray.opacity(0.08))
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                @unknown default:
                    EmptyView()
                }
            }
            if alignment == .center { Spacer() }
            if alignment == .trailing { /* right */ }
        }
        .padding(.bottom, 42)
    }
}

struct MagicFeedbackWebView: UIViewRepresentable {
    let surveyId: String
    let productId: String
    let onEvent: (String) -> Void

    final class Coordinator: NSObject, WKScriptMessageHandler {
        let onEvent: (String) -> Void
        init(onEvent: @escaping (String) -> Void) { self.onEvent = onEvent }
        func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
            if message.name == "DeepdotsBridge" {
                if let s = message.body as? String { onEvent(s) }
                else { onEvent(String(describing: message.body)) }
            }
        }
    }

    func makeCoordinator() -> Coordinator { Coordinator(onEvent: onEvent) }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        let controller = WKUserContentController()
        controller.add(context.coordinator, name: "DeepdotsBridge")
        config.userContentController = controller
        let webView = WKWebView(frame: .zero, configuration: config)
        webView.isOpaque = false
        webView.backgroundColor = .clear
        let html = buildHtml(surveyId: surveyId, productId: productId)
        webView.loadHTMLString(html, baseURL: URL(string: "https://magicfeedback.app/"))
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) { /* no-op */ }

    // Convenience actions
    static func send(_ webView: WKWebView) { webView.evaluateJavaScript("window.DeepdotsActions?.send()") }
    static func back(_ webView: WKWebView) { webView.evaluateJavaScript("window.DeepdotsActions?.back()") }
    static func close(_ webView: WKWebView) { webView.evaluateJavaScript("window.DeepdotsActions?.close()") }
    static func startForm(_ webView: WKWebView) { webView.evaluateJavaScript("window.DeepdotsActions?.startForm?.()") }

    // Prefer KMP-provided HTML so behavior/events match Android/TS exactly.
    private func buildHtml(surveyId: String, productId: String) -> String {
        #if canImport(shared)
        // Top-level Kotlin function becomes a Swift symbol with the fully-qualified file name prefix.
        // This call provides HTML that emits events through window.webkit.messageHandlers.DeepdotsBridge.postMessage
        return ComDeepdotsSdkUiMagicFeedbackHtmlKt.platformSurveyHtml(surveyId: surveyId, productId: productId)
        #else
        // Fallback minimal HTML (uses window.magicfeedback global, not MagicFeedbackNative)
        return """
        <!doctype html>
        <html>
          <head>
            <meta charset=\"utf-8\" />
            <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
            <style>html,body{margin:0;padding:0;background:transparent}</style>
            <script>
              function emit(e){ try { window.webkit.messageHandlers.DeepdotsBridge.postMessage(e); } catch(_){} }
            </script>
            <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/@magicfeedback/native@2.1.2-beta.7/dist/styles/magicfeedback-default.css\" />
            <script src=\"https://cdn.jsdelivr.net/npm/@magicfeedback/native@2.1.2-beta.7/dist/magicfeedback-sdk.browser.js\"></script>
          </head>
          <body>
            <div id=\"mf-form\"></div>
            <script>
              (function(){
                try {
                  if (window.magicfeedback) {
                    window.magicfeedback.init({debug:true, env:'prod'});
                    var form = window.magicfeedback.form('\(surveyId)', '\(productId)');
                    window.DeepdotsActions = {
                      send: function(){ try { form.send(); } catch(e){} },
                      back: function(){ try { form.back(); } catch(e){} },
                      close: function(){ emit('popup_close'); },
                      startForm: function(){ try { if (typeof form.startForm==='function') form.startForm(); } catch(e){} }
                    };
                    form.generate('mf-form', {
                      addButton:false,
                      getMetaData:true,
                      onLoadedEvent: function(args){ try { emit('loaded'); } catch(_){}}
                    }).catch(function(){ emit('error:init') });
                  } else { emit('error:sdk'); }
                } catch(e){ emit('error:exception'); }
              })();
            </script>
          </body>
        </html>
        """
        #endif
    }
}

// Simple wrapper view with buttons mirroring TS example behavior
struct MagicFeedbackContainerView: View {
    let surveyId: String
    let productId: String
    @State private var events: [String] = []
    @State private var webView: WKWebView?
    // Style-driven logo for iOS
    @State private var styleLogoURL: String? = nil
    @State private var styleLogoSize: String? = nil
    @State private var styleLogoPosition: String? = nil

    var body: some View {
        VStack(spacing: 12) {
            // Render logo above web content when available
            if let logo = styleLogoURL {
                DeepdotsLogoView(urlString: logo, size: styleLogoSize, position: styleLogoPosition)
            }
            MagicFeedbackWebView(surveyId: surveyId, productId: productId) { ev in
                events.append(ev)
                // Parse JSON payload to update style
                if ev.trimmingCharacters(in: .whitespacesAndNewlines).hasPrefix("{") {
                    if let data = ev.data(using: .utf8),
                       let obj = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                        let name = (obj["name"] as? String) ?? ""
                        if name == "loaded" || name == "popup_clicked" {
                            if let style = obj["style"] as? [String: Any] {
                                // Prefer image over logo if both provided
                                if let image = style["image"] as? String, !image.isEmpty {
                                    styleLogoURL = image
                                } else if let logo = style["logo"] as? String, !logo.isEmpty {
                                    styleLogoURL = logo
                                }
                                if let sz = (style["imageSize"] as? String) ?? (style["logoSize"] as? String) {
                                    styleLogoSize = sz
                                }
                                if let pos = (style["imagePosition"] as? String) ?? (style["logoPosition"] as? String) {
                                    styleLogoPosition = pos
                                }
                            }
                        }
                    }
                }
            }
            .background(WebViewReader(webView: $webView))
            HStack {
                Button("Start") { if let w = webView { MagicFeedbackWebView.startForm(w) } }
                Button("Back") { if let w = webView { MagicFeedbackWebView.back(w) } }
                Button("Send") { if let w = webView { MagicFeedbackWebView.send(w) } }
                Button("Close") { if let w = webView { MagicFeedbackWebView.close(w) } }
            }
            List(events, id: \.self) { Text($0) }
        }
    }
}

// Helper to capture WKWebView instance created by UIViewRepresentable
private struct WebViewReader: UIViewRepresentable {
    @Binding var webView: WKWebView?
    func makeUIView(context: Context) -> UIView { UIView() }
    func updateUIView(_ uiView: UIView, context: Context) {
        if let superview = uiView.superview {
            for sub in superview.subviews {
                if let wv = sub as? WKWebView { if webView !== wv { webView = wv } }
            }
        }
    }
}
