package com.deepdots.reference

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Activity "plana" (`android.app.Activity`, no `ComponentActivity`): sin los owners de
 * lifecycle/saved-state que Compose necesita, como la `FlutterActivity` por defecto. El SDK
 * tiene que poder pintar el popup igual.
 */
class CheckoutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
            fitsSystemWindows = true
        }
        root.addView(TextView(this).apply {
            text = "Checkout (android.app.Activity)"
            textSize = 20f
        })
        root.addView(Button(this).apply {
            id = R.id.checkout_probe
            bindProbe()
        }, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        root.addView(Button(this).apply {
            id = R.id.checkout_place_order
            text = "Place order"
            setOnClickListener {
                ReferenceApp.sdk.trackFunnelStep("purchase", "order_placed", "order-checkout")
                HostLog.add("trackFunnelStep purchase/order_placed")
            }
        })
        root.addView(Button(this).apply {
            id = R.id.checkout_fire_event
            text = "Fire custom-event"
            setOnClickListener { fireEvent() }
        })
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        ReferenceApp.sdk.setPath(Catalog.CHECKOUT)
        HostLog.add("setPath ${Catalog.CHECKOUT}")
    }
}
