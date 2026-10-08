package com.deepdots.sdk.ui

/**
 * Cambio de pagina dentro del survey. En cada envio la tarjeta daba tirones: el footer
 * desaparecia mientras cargaba (la tarjeta encogia y volvia a crecer), un velo con spinner
 * tapaba toda la tarjeta aunque la pagina llegase en 200 ms, y el alto seguia de golpe cada
 * altura intermedia que reportaba el WebView. Reportado por un cliente en iOS con 0.6.1.
 */
internal object PageTransition {
    /**
     * El spinner solo aparece si la pagina tarda mas que esto. En una red normal no llega a
     * salir, y una espera corta se ve como un parpadeo, no como una carga.
     */
    const val SPINNER_DELAY_MS: Long = 400L

    /** Duracion del ajuste del alto de la tarjeta al contenido de la pagina nueva. */
    const val HEIGHT_ANIMATION_MS: Int = 220

    /** Opacidad del footer mientras carga una pagina lenta (sus botones no responden). */
    const val FOOTER_BUSY_ALPHA: Float = 0.5f

    /**
     * Alto que manda en la tarjeta. Mientras se envia una pagina se mantiene el que habia al
     * empezar ([frozen]): el survey pasa por alturas intermedias al cambiar de pagina, y la
     * tarjeta iba detras de cada una. Sin congelar (o sin dato congelado) manda el reportado.
     */
    fun heightSource(reported: Int?, frozen: Int?, loading: Boolean): Int? =
        if (loading && frozen != null) frozen else reported
}
