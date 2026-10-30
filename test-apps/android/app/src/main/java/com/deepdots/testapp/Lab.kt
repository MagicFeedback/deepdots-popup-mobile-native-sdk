package com.deepdots.testapp

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Backend the app talks to. There is no production option: test data stays in MAGICFEEDBACK_DEV. */
const val DEV_API = "https://api-dev.deepdots.com"

/** Scenario name -> DEV public key / host events, from test-apps/scenarios.json (baked in at build time). */
object Scenarios {
    val keys: Map<String, String> = parse(BuildConfig.SCENARIO_KEYS)
    val events: Map<String, List<String>> = parse(BuildConfig.SCENARIO_EVENTS)
        .mapValues { (_, v) -> v.split(",").filter { it.isNotBlank() } }
    val names: List<String> get() = keys.keys.toList()

    private fun parse(raw: String) = raw.split(";").filter { it.contains("=") }
        .associate { it.substringBefore("=") to it.substringAfter("=") }
}

/**
 * What a run tests. Maestro passes it as launch arguments (intent extras); the SDK Lab panel
 * edits it. It is saved, so a relaunch without arguments (re-display, background tests) keeps
 * the same run.
 */
data class LabConfig(
    val scenario: String = "checkout-time",
    /** `history`: every screen calls setPath(). `silent`: the app never tells the SDK it navigated. */
    val nav: String = "history",
    val lang: String = "en",
    /** `immediate`: init at launch. `consent`: init after "Accept all" in the cookie dialog. */
    val load: String = "immediate",
    val user: String = "",
    /** First screen: home, shop, cart, checkout or account (one-shot, not saved). */
    val start: String = "home",
) {
    val publicKey: String get() = Scenarios.keys[scenario] ?: error("Unknown scenario $scenario")

    fun save(context: Context) {
        prefs(context).edit()
            .putString("scenario", scenario).putString("nav", nav).putString("lang", lang)
            .putString("load", load).putString("user", user).apply()
    }

    companion object {
        private val KEYS = listOf("scenario", "nav", "lang", "load", "user")

        fun prefs(context: Context) = context.getSharedPreferences("lab", Context.MODE_PRIVATE)

        /** Saved config, overridden by any launch argument. */
        fun resolve(context: Context, intent: Intent?): LabConfig {
            val p = prefs(context)
            val extras = intent?.extras
            fun read(key: String, default: String) =
                extras?.get(key)?.toString() ?: p.getString(key, null) ?: default
            val base = LabConfig()
            val config = LabConfig(
                scenario = read("scenario", base.scenario).takeIf { it in Scenarios.keys } ?: base.scenario,
                nav = read("nav", base.nav),
                lang = read("lang", base.lang),
                load = read("load", base.load),
                user = read("user", base.user),
                start = extras?.getString("start") ?: base.start,
            )
            if (extras != null && KEYS.any { extras.containsKey(it) }) config.save(context)
            return config
        }
    }
}

/** Run log: on screen (SDK Lab) and in logcat with the `[lab]` prefix, which is what CI keeps. */
object Lab {
    val lines = mutableStateListOf<String>()
    private val main = Handler(Looper.getMainLooper())
    private val clock = SimpleDateFormat("HH:mm:ss", Locale.US)

    /** SDK events seen in this process (Lab bar `lab-counts`): the regression flows assert on them. */
    var shown by mutableIntStateOf(0)
        private set
    var completed by mutableIntStateOf(0)
        private set

    fun countShown() = onMain { shown++ }
    fun countCompleted() = onMain { completed++ }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }

    fun log(tag: String, message: String) {
        Log.i("lab", "[lab] $tag $message")
        val line = "${clock.format(Date())} $tag $message"
        onMain { append(line) }
    }

    private fun append(line: String) {
        lines.add(line)
        if (lines.size > 200) lines.removeAt(0)
    }
}
