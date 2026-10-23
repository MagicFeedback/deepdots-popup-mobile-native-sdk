package com.deepdots.reference

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.deepdots.sdk.platform.PlatformContext

/** Estado del SDK y registro de eventos, para pruebas manuales y para los E2E. */
class DiagnosticsActivity : AppCompatActivity() {

    private lateinit var identity: TextView
    private lateinit var log: TextView
    private val refresh: () -> Unit = { render() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)
        identity = findViewById(R.id.diag_identity)
        log = findViewById<TextView>(R.id.diag_log).apply { movementMethod = ScrollingMovementMethod() }

        findViewById<Button>(R.id.diag_fire_event).setOnClickListener { fireEvent() }
        findViewById<Button>(R.id.diag_close_popup).setOnClickListener {
            ReferenceApp.sdk.close(PlatformContext(this))
            HostLog.add("close()")
        }
        findViewById<Button>(R.id.diag_flush).setOnClickListener {
            ReferenceApp.sdk.flushAnalytics()
            HostLog.add("flushAnalytics")
        }
        findViewById<CheckBox>(R.id.diag_cold_event).apply {
            isChecked = Settings.fireEventOnColdStart(this@DiagnosticsActivity)
            setOnCheckedChangeListener { _, checked -> Settings.setFireEventOnColdStart(this@DiagnosticsActivity, checked) }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<Button>(R.id.probe_button).bindProbe()
        ReferenceApp.sdk.setPath(Catalog.DIAGNOSTICS)
        HostLog.listen(refresh)
        render()
    }

    override fun onPause() {
        HostLog.unlisten(refresh)
        super.onPause()
    }

    private fun render() {
        val sdk = ReferenceApp.sdk
        identity.text = buildString {
            appendLine("env: ${BuildConfig.DEEPDOTS_ENV}  key: ${BuildConfig.DEEPDOTS_PUBLIC_KEY.take(8)}…")
            appendLine("user_id: ${sdk.getUserId()}")
            appendLine("session_id: ${sdk.getSessionId()}")
            append("popups loaded: ${sdk.debugListPopups().size}")
        }
        log.text = HostLog.snapshot().joinToString("\n")
    }
}
