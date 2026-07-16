package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FontHtmlParityTest {
    @Test fun formatFromExtension() {
        assertEquals("woff2", fontFormatFromUrl("https://x.com/Inter.woff2"))
        assertEquals("truetype", fontFormatFromUrl("https://x.com/Inter.ttf"))
        assertEquals("opentype", fontFormatFromUrl("https://x.com/Inter.otf"))
        assertEquals("woff2", fontFormatFromUrl("https://x.com/Inter.woff2?v=3#a"))
        assertNull(fontFormatFromUrl("https://x.com/Inter.eot"))
    }

    @Test fun familyValueAddsFallback() {
        assertEquals("\"Inter\", -apple-system, system-ui, sans-serif", buildFontFamilyValue("Inter"))
    }

    @Test fun familyIsSanitized() {
        assertEquals(
            "\"Interstylescript\", -apple-system, system-ui, sans-serif",
            buildFontFamilyValue("Inter\";}</style><script>"),
        )
    }

    @Test fun fontFaceCss() {
        assertEquals("", buildFontFaceCss("Inter", null))
        assertEquals(
            "@font-face{font-family:\"Inter\";src:url(\"https://x.com/Inter.woff2\") format(\"woff2\");font-display:swap;}",
            buildFontFaceCss("Inter", "https://x.com/Inter.woff2"),
        )
        assertEquals(
            "@font-face{font-family:\"Inter\";src:url(\"https://x.com/Inter.eot\");font-display:swap;}",
            buildFontFaceCss("Inter", "https://x.com/Inter.eot"),
        )
    }

    @Test fun rejectsUnsafeUrls() {
        assertEquals("", buildFontFaceCss("Inter", "javascript:alert(1)"))
        assertEquals("", buildFontFaceCss("Inter", "https://x.com/a\".woff2"))
        assertEquals("", buildFontFaceCss("Inter", "https://x.com/a.woff2\n}body{color:red}"))
        assertEquals("", buildFontFaceCss("Inter", "https://x.com/a b.woff2"))
    }

    @Test fun acceptsDataUrl() {
        assertTrue(
            buildFontFaceCss("Inter", "data:font/woff2;base64,AAAA")
                .contains("@font-face{font-family:\"Inter\";src:url(\"data:font/woff2;base64,AAAA\")"),
        )
    }
}
