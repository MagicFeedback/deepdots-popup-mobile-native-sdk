package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsIdentity
import com.deepdots.sdk.analytics.AnalyticsManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SdkIdentityTest {
    @AfterTest
    fun reset() = SdkIdentity.clearWrapper()

    @Test
    fun native_sdk_is_platform_at_build_version() {
        assertTrue(SdkIdentity.version.isNotBlank())
        assertTrue(SdkIdentity.sdk == "android@${SdkIdentity.version}" || SdkIdentity.sdk == "ios@${SdkIdentity.version}")
        assertEquals(SdkIdentity.core, SdkIdentity.sdk)
        assertNull(SdkIdentity.sdkCore)
    }

    @Test
    fun a_wrapper_becomes_the_sdk_and_the_native_core_moves_to_sdk_core() {
        SdkIdentity.setWrapper("flutter", " 0.1.0-beta.3 ")
        assertEquals("flutter@0.1.0-beta.3", SdkIdentity.sdk)
        assertEquals(SdkIdentity.core, SdkIdentity.sdkCore)

        SdkIdentity.setWrapper("flutter", "")
        assertEquals(SdkIdentity.core, SdkIdentity.sdk)
        assertNull(SdkIdentity.sdkCore)
    }

    @Test
    fun analytics_batches_carry_the_current_sdk_identity() {
        val am = AnalyticsManager(platform = "android")
        val identity = AnalyticsIdentity(userId = "u-1", sessionId = null)
        assertEquals(SdkIdentity.core, am.buildPayload(identity).context.sdk)
        assertNull(am.buildPayload(identity).context.sdkCore)

        SdkIdentity.setWrapper("flutter", "0.1.0")
        val ctx = am.buildPayload(identity).context
        assertEquals("flutter@0.1.0", ctx.sdk)
        assertEquals(SdkIdentity.core, ctx.sdkCore)
    }
}
