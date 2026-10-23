package com.deepdots.reference

import android.widget.Button

/** Engancha un botón a la sonda de respuesta (ver [Probe]). */
fun Button.bindProbe() {
    text = Probe.label()
    setOnClickListener {
        Probe.tap()
        text = Probe.label()
    }
}
