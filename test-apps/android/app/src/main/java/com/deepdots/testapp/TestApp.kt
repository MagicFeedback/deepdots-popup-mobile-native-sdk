package com.deepdots.testapp

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.deepdots.sdk.Deepdots
import com.deepdots.sdk.DeepdotsPopupsSdk
import com.deepdots.sdk.models.Environment
import com.deepdots.sdk.models.Event
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.Mode
import com.deepdots.sdk.models.PopupOptions

/** Owns the SDK for the whole process, as a host app would, and feeds it the app lifecycle. */
class TestApp : Application() {
    val sdk: DeepdotsPopupsSdk = Deepdots.create()
    var initialized = false
        private set
    lateinit var config: LabConfig

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            private var started = false

            // onStop/onStart of the whole process, never onPause: onBackground() ends the SDK session.
            override fun onStart(owner: LifecycleOwner) {
                if (started) {
                    Lab.log("life", "foreground")
                    if (initialized) sdk.onForeground()
                }
                started = true
            }

            override fun onStop(owner: LifecycleOwner) {
                Lab.log("life", "background")
                if (initialized) sdk.onBackground()
            }
        })
    }

    fun initSdk() {
        if (initialized) return
        initialized = true
        val c = config
        Lab.log("sdk", "init ${BuildConfig.SDK_VERSION} (${BuildConfig.SDK_SOURCE}) env=Development api=$DEV_API")
        Lab.log("sdk", "scenario=${c.scenario} key=${c.publicKey} nav=${c.nav} lang=${c.lang}")
        sdk.on(Event.PopupShown) {
            Lab.log("popup", "shown id=${it.popupId} survey=${it.surveyId}")
            Lab.countShown()
        }
        sdk.on(Event.PopupClicked) { Lab.log("popup", "clicked id=${it.popupId} action=${it.extra["action"]}") }
        sdk.on(Event.SurveyCompleted) {
            Lab.log("popup", "survey_completed survey=${it.surveyId}")
            Lab.countCompleted()
        }
        sdk.init(initOptions())
        if (c.user.isNotBlank()) sdk.setUserId(c.user)
        Lab.log("sdk", "userId=${sdk.getUserId()}")
    }

    /** A host that calls init() again (e.g. from a second entry point): the SDK must ignore it. */
    fun initAgain() {
        if (!initialized) return
        Lab.log("sdk", "init again (same options)")
        sdk.init(initOptions())
    }

    /**
     * Duplicate and concurrent triggers: the same event twice in a row (a double tap) and once
     * more 3 s later, while its popup is open. The SDK must show the popup once.
     */
    fun burst(event: String) {
        Lab.log("host", "triggerEvent($event) x2 now + 1 in 3 s")
        sdk.triggerEvent(event)
        sdk.triggerEvent(event)
        android.os.Handler(mainLooper).postDelayed({
            Lab.log("host", "triggerEvent($event) again (popup open)")
            sdk.triggerEvent(event)
        }, 3000)
    }

    private fun initOptions() = InitOptions(
        debug = true,
        environment = Environment.Development,
        mode = Mode.Server,
        popupOptions = PopupOptions(publicKey = config.publicKey),
        provideLang = { config.lang },
        autoLaunch = true,
    )

    /** Clears the SDK's own storage (cooldowns, user id) and the cookie choice, keeps the run config. */
    fun reset() {
        deleteSharedPreferences("deepdots_sdk")
        LabConfig.prefs(this).edit().remove("consent").commit()
    }

    fun restart(activity: Activity) {
        val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return
        activity.startActivity(Intent.makeRestartActivityTask(launch.component))
        Runtime.getRuntime().exit(0)
    }
}
