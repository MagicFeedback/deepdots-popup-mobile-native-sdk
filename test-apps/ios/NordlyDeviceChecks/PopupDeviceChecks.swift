import XCTest

/// DD-273: the key popup checks on a real iPhone / iPad (Firebase Test Lab, see
/// `testlab/devices.yml`) or on a simulator. Same checks as the Android instrumented tests
/// (`shared/src/androidInstrumentedTest/.../PopupDeviceChecksTest.kt`):
///
/// - the popup opens with the survey loaded;
/// - X, Send and Back respond to a finger-like tap (down, a small move, up);
/// - with the keyboard open on the text question, Send stays above it and the X closes;
/// - on an iPad the survey fills its slot (DD-270: no blank strip down to Send).
///
/// NORDLY on DEV, scenario `help-click` (Account › Help), survey «SDK Test App · Simple»: page 1
/// NPS 0–10 (picking a value moves on), page 2 free text. No client data. The Send check leaves
/// one answer in MAGICFEEDBACK_DEV, like the Maestro regression.
final class PopupDeviceChecks: XCTestCase {
    private let title = "Can we help?"
    private let npsQuestion = "How likely are you to recommend"
    private let textQuestion = "What could we improve?"
    /// First open on a cold device: WebKit start + survey bundle from the CDN + DEV API.
    private let loadTimeout: TimeInterval = 60
    private var app: XCUIApplication!

    override func setUp() {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["-scenario", "help-click", "-lang", "en", "-load", "immediate",
                               "-consent", "all", "-start", "account", "-reset", "1"]
        app.launch()
    }

    // MARK: - Checks

    func testPopupOpensWithTheSurvey() {
        openPopup()
        XCTAssertTrue(closeButton.exists)
        XCTAssertTrue(button("Send").exists)
    }

    func testCloseRespondsToAFingerTap() {
        openPopup()
        fingerTap(closeButton)
        assertPopupClosed()
    }

    func testBackRespondsToAFingerTap() {
        openPopup()
        goToTextPage()
        fingerTap(button("Back"))
        XCTAssertTrue(waitForSurveyText(npsQuestion, timeout: 15), "Back did not return to page 1")
    }

    func testSendRespondsToAFingerTap() {
        openPopup()
        goToTextPage()
        dismissKeyboardIfAny()
        fingerTap(button("Send"))
        // The native "Complete survey" button replaces Send on the thank-you screen.
        XCTAssertTrue(button("Complete survey").waitForExistence(timeout: 20), "Send did not submit the survey")
    }

    func testKeyboardOverTheTextQuestionLeavesSendAndCloseUsable() {
        openPopup()
        goToTextPage()
        let box = app.webViews.textViews.firstMatch.exists ? app.webViews.textViews.firstMatch : app.webViews.textFields.firstMatch
        if app.keyboards.count == 0 {
            XCTAssertTrue(box.waitForExistence(timeout: 10), "no text box on page 2")
            fingerTap(box)
        }
        let keyboard = app.keyboards.firstMatch
        XCTAssertTrue(keyboard.waitForExistence(timeout: 10), "keyboard did not open")
        app.typeText("device check")
        // Let the popup move up with the keyboard.
        Thread.sleep(forTimeInterval: 1)

        let send = button("Send")
        XCTAssertTrue(send.isHittable, "Send is not tappable with the keyboard open")
        XCTAssertLessThanOrEqual(send.frame.maxY, keyboard.frame.minY + 1,
                                 "Send (\(send.frame)) is under the keyboard (\(keyboard.frame))")
        XCTAssertTrue(closeButton.isHittable, "X is not tappable with the keyboard open")
        fingerTap(closeButton)
        assertPopupClosed()
    }

    func testSurveyFillsItsSlotOnAnIPad() throws {
        try XCTSkipUnless(UIDevice.current.userInterfaceIdiom == .pad, "iPad only")
        openPopup()
        let web = app.webViews.firstMatch
        let send = button("Send")
        XCTAssertTrue(web.exists)
        // The survey slot ends where the footer starts; a WebView shorter than its slot leaves a
        // blank strip between the survey and Send (the 520 pt bug on tablets).
        let gap = send.frame.minY - web.frame.maxY
        XCTAssertLessThanOrEqual(gap, 48, "blank strip of \(gap) pt between the survey (\(web.frame)) and Send (\(send.frame))")
        XCTAssertGreaterThan(web.frame.height, 100, "survey has no height (\(web.frame))")
    }

    // MARK: - Steps

    private var closeButton: XCUIElement { app.buttons["Close popup"].firstMatch }

    private func button(_ label: String) -> XCUIElement { app.buttons[label].firstMatch }

    /// Survey text inside the WebView. WebKit exposes it as a static text or as a heading/other
    /// element depending on the version, so match any element whose label contains it.
    private func staticText(_ text: String) -> XCUIElement {
        app.webViews.descendants(matching: .any).matching(NSPredicate(format: "label CONTAINS %@", text)).firstMatch
    }

    private func openPopup() {
        let help = app.buttons["help-button"].firstMatch
        XCTAssertTrue(help.waitForExistence(timeout: 30), "NORDLY did not open on Account")
        // The SDK loads the popup config after init; tap Help until the popup appears.
        let deadline = Date().addingTimeInterval(30)
        repeat {
            help.tap()
            if app.staticTexts[title].waitForExistence(timeout: 5) { break }
        } while Date() < deadline
        XCTAssertTrue(app.staticTexts[title].exists, "popup '\(title)' did not open")
        if !waitForSurveyText(npsQuestion, timeout: loadTimeout) {
            attachHierarchy("survey-not-loaded")
            XCTFail("survey did not load")
        }
    }

    /// WebKit sometimes paints the survey but leaves its accessibility tree empty until something
    /// changes on screen; a small scroll at the top of the survey (no visible change) refreshes it.
    private func waitForSurveyText(_ text: String, timeout: TimeInterval) -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if staticText(text).waitForExistence(timeout: 8) { return true }
            let web = app.webViews.firstMatch
            if web.exists {
                let top = web.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.2))
                top.press(forDuration: 0.05, thenDragTo: top.withOffset(CGVector(dx: 0, dy: 30)))
            }
        }
        return staticText(text).exists
    }

    /// The accessibility tree, for the test report when a step fails.
    private func attachHierarchy(_ name: String) {
        let a = XCTAttachment(string: app.debugDescription)
        a.name = name
        a.lifetime = .keepAlways
        add(a)
    }

    /// Page 1 → page 2: a real tap on an NPS chip (picking a value moves on). The radio inputs
    /// behind the chips carry the same label off screen, so take the first chip that can be tapped.
    private func goToTextPage() {
        let chips = app.webViews.staticTexts.matching(NSPredicate(format: "label IN {'0','1','2','3','4','5'}"))
        XCTAssertTrue(chips.firstMatch.waitForExistence(timeout: 10), "no NPS chips")
        // isHittable is false for WebView texts on some iPads: take the first chip drawn inside
        // the survey and tap its centre.
        let webFrame = app.webViews.firstMatch.frame
        let chip = chips.allElementsBoundByIndex.first {
            !$0.frame.isEmpty && webFrame.contains(CGPoint(x: $0.frame.midX, y: $0.frame.midY))
        }
        guard let chip else {
            attachHierarchy("no-tappable-chip")
            return XCTFail("no NPS chip inside the survey")
        }
        chip.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        if !waitForSurveyText(textQuestion, timeout: 20) {
            attachHierarchy("page-2-missing")
            XCTFail("page 2 did not open")
        }
        XCTAssertTrue(button("Back").waitForExistence(timeout: 10))
    }

    private func dismissKeyboardIfAny() {
        guard app.keyboards.count > 0 else { return }
        // iPhone: the WebView keyboard toolbar has "Done". iPad: hide-keyboard key.
        if app.toolbars.buttons["Done"].exists { app.toolbars.buttons["Done"].tap(); return }
        let hide = app.keyboards.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'keyboard'")).firstMatch
        if hide.exists { hide.tap() }
    }

    private func assertPopupClosed() {
        let gone = NSPredicate(format: "exists == false")
        expectation(for: gone, evaluatedWith: app.staticTexts[title])
        waitForExpectations(timeout: 10)
        XCTAssertTrue(app.buttons["help-button"].isHittable, "host app is not usable after closing")
    }

    /// A finger-like tap: down, a 2–3 pt move, up. A plain `tap()` has no movement, and that is
    /// how the emulator tests missed DD-209 (the X / Send / Back lost finger taps on devices).
    private func fingerTap(_ element: XCUIElement) {
        XCTAssertTrue(element.waitForExistence(timeout: 10), "\(element) not found")
        let start = element.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        let end = start.withOffset(CGVector(dx: 2, dy: 3))
        start.press(forDuration: 0.05, thenDragTo: end)
    }
}
