package com.deepdots.sdk

import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/** The popup JSON as the API sends it (MOM: event + delaySeconds). Uses only APIs that exist on dev, so it doubles as the failure test of the fix. */
class TriggerDelayApiPayloadTest {
    private fun sdkFor(trigger: String) = DeepdotsPopups().apply {
        init(InitOptions(debug = true, popupOptions = PopupOptions(), storage = InMemoryStorage()))
        debugLoadPopups(
            debugParseServerPayload(
                """[{"id":"p","title":"t","message":"m","surveyId":"s","productId":"x","triggers":[$trigger]}]""",
            ),
        )
    }

    @Test
    fun event_with_delaySeconds_is_not_queued_at_once() {
        val sdk = sdkFor("""{"type":"event","value":"RATING","delaySeconds":60}""")
        sdk.triggerEvent("RATING")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "queued at once: ${sdk.debugQueuedPopupIds()}")
    }

    @Test
    fun click_with_delaySeconds_is_not_queued_at_once() {
        val sdk = sdkFor("""{"type":"click","value":"help","delaySeconds":60}""")
        sdk.triggerClick("help")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "queued at once: ${sdk.debugQueuedPopupIds()}")
    }

    @Test
    fun scroll_with_delaySeconds_is_not_queued_at_once() {
        val sdk = sdkFor("""{"type":"scroll","value":50,"delaySeconds":60}""")
        sdk.onScroll(80)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "queued at once: ${sdk.debugQueuedPopupIds()}")
    }

    @Test
    fun exit_as_the_admin_saves_it_waits_its_delaySeconds() {
        val sdk = sdkFor("""{"type":"exit","value":0,"delaySeconds":60}""")
        sdk.setPath("https://app.test/#/login")
        sdk.setPath("https://app.test/#/home")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "queued at once: ${sdk.debugQueuedPopupIds()}")
    }

    @Test
    fun event_with_delaySeconds_then_leaving_the_screen_never_queues() = runBlocking {
        val sdk = sdkFor("""{"type":"event","value":"RATING","delaySeconds":0.3}""")
        sdk.setPath("https://app.test/#/rating")
        sdk.triggerEvent("RATING")
        sdk.setPath("https://app.test/#/home")
        delay(500)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "queued: ${sdk.debugQueuedPopupIds()}")
    }
}
