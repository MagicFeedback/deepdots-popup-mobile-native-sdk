package com.deepdots.reference

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Casos de prueba de bugs ya arreglados: cada uno dice cómo reproducirlo y qué tiene que pasar.
 * Si un caso vuelve a fallar, es una regresión. Los botones tienen ids estables para los E2E.
 */
class TestCasesActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        column.addView(Button(this).apply { id = R.id.cases_probe; bindProbe() })

        addCase(
            column,
            title = "1. Popup fired while a host bottom sheet is open",
            steps = "Opens a bottom sheet and fires custom-event one second later.",
            expected = "Nothing appears under the sheet. Close the sheet (Back or tap outside): " +
                "the popup appears on top and can be used.",
            buttonId = R.id.case_sheet_event,
        ) {
            BottomSheetDialog(this).apply {
                setContentView(TextView(this@TestCasesActivity).apply {
                    text = "Host bottom sheet. custom-event fires in 1 s."
                    setPadding(dp(24), dp(32), dp(24), dp(48))
                })
            }.show()
            fireEventLater()
        }

        addCase(
            column,
            title = "2. Popup fired while a host dialog is open",
            steps = "Opens a dialog and fires custom-event one second later.",
            expected = "The dialog stays on top and usable. Close it: the popup appears.",
            buttonId = R.id.case_dialog_event,
        ) {
            AlertDialog.Builder(this)
                .setTitle("Host dialog")
                .setMessage("custom-event fires in 1 s.")
                .setPositiveButton("Close", null)
                .show()
            fireEventLater()
        }

        addCase(
            column,
            title = "3. Tap outside the popup",
            steps = "Fires custom-event. When the popup is open, tap the dimmed area over the probe.",
            expected = "The probe count does not change and the popup stays open. Close the popup: " +
                "the probe works again.",
            buttonId = R.id.case_scrim_event,
        ) { fireEvent() }

        addCase(
            column,
            title = "4. Survey that cannot load",
            steps = "With the app already started, cut the network " +
                "(adb shell cmd connectivity airplane-mode enable) and fire custom-event. " +
                "With the current api-dev integration every survey fails to load, network or not.",
            expected = "The popup shows \"We couldn't load this survey.\", the X and a Cancel button. " +
                "Both close it; then the probe works.",
            buttonId = R.id.case_load_error_event,
        ) { fireEvent() }

        addCase(
            column,
            title = "5. Writing in the survey with the keyboard open",
            steps = "Fires custom-event. Answer the rating; on the text page the keyboard opens. " +
                "Type something, then press the system Back once.",
            expected = "With the keyboard open, Back and Send stay visible above it. The first Back " +
                "only hides the keyboard: the popup stays open and keeps the text. A second Back " +
                "closes it. Send works with the keyboard still open.",
            buttonId = R.id.case_keyboard_event,
        ) { fireEvent() }

        setContentView(ScrollView(this).apply {
            fitsSystemWindows = true
            addView(column)
        })
    }

    override fun onResume() {
        super.onResume()
        findViewById<Button>(R.id.cases_probe).bindProbe()
        ReferenceApp.sdk.setPath(Catalog.TEST_CASES)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun fireEventLater() = handler.postDelayed({ fireEvent() }, 1000)

    private fun addCase(
        column: LinearLayout,
        title: String,
        steps: String,
        expected: String,
        buttonId: Int,
        run: () -> Unit,
    ) {
        column.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setPadding(0, dp(20), 0, dp(4))
        })
        column.addView(TextView(this).apply { text = steps })
        column.addView(TextView(this).apply {
            text = "Expected: $expected"
            setPadding(0, dp(4), 0, dp(4))
        })
        column.addView(Button(this).apply {
            id = buttonId
            text = "Run"
            setOnClickListener {
                HostLog.add("test case: $title")
                run()
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
