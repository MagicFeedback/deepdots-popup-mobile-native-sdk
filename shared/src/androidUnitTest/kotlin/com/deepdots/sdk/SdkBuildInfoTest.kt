package com.deepdots.sdk

import java.io.File
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

/** La versión de `deepdots_sdk` sale del build (PUBLISHING_VERSION), no de una constante a mano. */
class SdkBuildInfoTest {
    @Test
    fun version_matches_publishing_version_in_gradle_properties() {
        val file = listOf(File("gradle.properties"), File("../gradle.properties")).first { it.isFile && it.readText().contains("PUBLISHING_VERSION") }
        val expected = Properties().apply { file.inputStream().use(::load) }.getProperty("PUBLISHING_VERSION")
        assertEquals(expected, SdkIdentity.version)
    }
}
