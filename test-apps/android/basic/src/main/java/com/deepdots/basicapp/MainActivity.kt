package com.deepdots.basicapp

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.deepdots.sdk.Deepdots
import com.deepdots.sdk.models.Environment
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.Mode
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.platform.PlatformContext

/** One screen. The SDK is loaded the minimum way: init, attachContext, setPath("/"). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "Basic app · SDK ${BuildConfig.SDK_VERSION}"
            contentDescription = "basic-home"
            gravity = Gravity.CENTER
            textSize = 20f
        })
        if (sdk == null) {
            sdk = Deepdots.create().apply {
                init(
                    InitOptions(
                        environment = Environment.Development,
                        mode = Mode.Server,
                        popupOptions = PopupOptions(publicKey = BuildConfig.PUBLIC_KEY),
                        autoLaunch = true,
                    )
                )
            }
        }
        sdk?.attachContext(PlatformContext(this))
        sdk?.setPath("/")
    }

    companion object {
        private var sdk: com.deepdots.sdk.DeepdotsPopupsSdk? = null
    }
}
