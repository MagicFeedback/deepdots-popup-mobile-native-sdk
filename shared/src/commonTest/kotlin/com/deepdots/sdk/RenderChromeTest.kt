package com.deepdots.sdk

import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `InitOptions.renderChrome` alimenta `SdkRuntime.renderChrome`, que `PopupView` lee para
 * decidir si pinta el scrim + la tarjeta (chrome) o deja el popup transparente para que el host
 * controle el marco. Paridad con Web/RN (`DeepdotsInitParams.renderChrome`).
 */
class RenderChromeTest {

    private fun initWith(renderChrome: Boolean?) {
        DeepdotsPopups().init(
            InitOptions(
                popupOptions = PopupOptions(publicKey = "pk-rc"),
                storage = InMemoryStorage(),
                renderChrome = renderChrome,
            ),
        )
    }

    @Test
    fun default_true_paints_chrome() {
        initWith(null)
        assertEquals(true, SdkRuntime.renderChrome)
    }

    @Test
    fun explicit_true_paints_chrome() {
        initWith(true)
        assertEquals(true, SdkRuntime.renderChrome)
    }

    @Test
    fun false_disables_chrome_host_manages_frame() {
        initWith(false)
        assertEquals(false, SdkRuntime.renderChrome)
    }
}
