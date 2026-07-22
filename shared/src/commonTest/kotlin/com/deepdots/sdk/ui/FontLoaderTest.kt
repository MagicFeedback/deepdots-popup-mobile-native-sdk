package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily
import com.deepdots.sdk.models.PopupFont
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class FontLoaderTest {

    private val fakeFamily: FontFamily = FontFamily.Monospace

    private class Counter { var calls = 0 }

    private fun loader(
        counter: Counter,
        bytes: ByteArray? = byteArrayOf(1, 2, 3),
        throwOnFetch: Boolean = false,
        build: (String, ByteArray) -> FontFamily? = { _, _ -> FontFamily.Monospace },
    ) = FontLoader(
        fetch = { _ ->
            counter.calls++
            if (throwOnFetch) throw RuntimeException("boom")
            bytes
        },
        buildFamily = build,
    )

    @Test
    fun null_font_returns_null_without_fetch() = runBlocking {
        val c = Counter()
        assertNull(loader(c).load(null))
        assertEquals(0, c.calls)
    }

    @Test
    fun family_only_no_url_returns_null_without_fetch() = runBlocking {
        val c = Counter()
        assertNull(loader(c).load(PopupFont(family = "Inter", url = null)))
        assertEquals(0, c.calls)
    }

    @Test
    fun unsafe_url_returns_null_without_fetch() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "javascript:alert(1)")
        assertNull(loader(c).load(font))
        assertEquals(0, c.calls)
    }

    @Test
    fun safe_url_builds_family() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertSame(fakeFamily, loader(c).load(font))
        assertEquals(1, c.calls)
    }

    @Test
    fun same_url_fetched_once_thanks_to_cache() = runBlocking {
        val c = Counter()
        val l = loader(c)
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        l.load(font)
        l.load(font)
        assertEquals(1, c.calls)
    }

    @Test
    fun fetch_failure_returns_null() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertNull(loader(c, throwOnFetch = true).load(font))
    }

    @Test
    fun empty_bytes_returns_null() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertNull(loader(c, bytes = byteArrayOf()).load(font))
    }

    @Test
    fun null_bytes_returns_null() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertNull(loader(c, bytes = null).load(font))
    }
}
