package com.deepdots.sdk

import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Sin `analytics` en init el canal queda en dry-run: no envía nada y el payload solo se
 * imprime con `debug`, para no ensuciar la consola de la app host en producción.
 * Paridad con Web (src/core/deepdots-popups.analytics.test.ts, 1.8.4).
 */
class DeepdotsPopupsDryRunTest {

    private fun sdk(debug: Boolean, printed: MutableList<String>): DeepdotsPopups =
        DeepdotsPopups().apply {
            debugDryRunLog = { printed += it }
            init(
                InitOptions(
                    debug = debug,
                    popupOptions = PopupOptions(publicKey = "pk-1"),
                    storage = InMemoryStorage(),
                ),
            )
        }

    @Test
    fun without_debug_the_dry_run_prints_nothing_and_empties_the_buffer() {
        val printed = mutableListOf<String>()
        val s = sdk(debug = false, printed = printed)
        s.track("page_view", mapOf("screen" to "/home"))
        s.flushAnalytics()

        assertTrue(printed.none { it.contains("/sdk/feedback") }, "dry-run printed without debug: $printed")
        assertTrue(s.previewAnalytics().events.isEmpty())
    }

    @Test
    fun with_debug_the_dry_run_prints_the_payload_and_empties_the_buffer() {
        val printed = mutableListOf<String>()
        val s = sdk(debug = true, printed = printed)
        s.track("page_view", mapOf("screen" to "/home"))
        s.flushAnalytics()

        assertTrue(printed.any { it.contains("[DeepdotsAnalytics]") && it.contains("/sdk/feedback") })
        assertTrue(s.previewAnalytics().events.isEmpty())
    }
}
