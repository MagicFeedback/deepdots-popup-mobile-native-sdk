package com.deepdots.reference

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.deepdots.sdk.Deepdots
import com.deepdots.sdk.DeepdotsPopupsSdk
import com.deepdots.sdk.models.Environment
import com.deepdots.sdk.models.Event
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.platform.PlatformContext

/**
 * Integración del SDK tal como la haría un cliente con varias Activities:
 *
 * - Una sola instancia, creada e inicializada en `Application.onCreate` (arranque en frío).
 * - El contexto del SDK sigue a la Activity visible (`ActivityLifecycleCallbacks.onResumed`):
 *   si no, el popup se pintaría en una Activity que ya no está en pantalla.
 * - Foreground/background a nivel de PROCESO (`ProcessLifecycleOwner`), no por Activity:
 *   pasar de una Activity a otra no debe cerrar la sesión de analytics.
 *
 * Todo lo que el SDK hace se apunta en [HostLog], que la pantalla de diagnóstico enseña y los
 * E2E leen.
 */
class ReferenceApp : Application() {

    lateinit var sdk: DeepdotsPopupsSdk
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        sdk = Deepdots.create()
        registerActivityLifecycleCallbacks(ContextFollower())

        sdk.on(Event.PopupShown) { HostLog.add("PopupShown popup=${it.popupId}") }
        sdk.on(Event.PopupClicked) { HostLog.add("PopupClicked popup=${it.popupId} action=${it.extra["action"]}") }
        sdk.on(Event.SurveyCompleted) { HostLog.add("SurveyCompleted survey=${it.surveyId}") }

        sdk.init(
            InitOptions(
                debug = true,
                environment = if (BuildConfig.DEEPDOTS_ENV == "prod") Environment.Production else Environment.Development,
                popupOptions = PopupOptions(publicKey = BuildConfig.DEEPDOTS_PUBLIC_KEY),
                autoLaunch = true,
                provideLang = { "en" },
                metadata = Session.userId(this)?.let { mapOf("userId" to it) },
            )
        )
        HostLog.add("init env=${BuildConfig.DEEPDOTS_ENV} user=${sdk.getUserId()}")

        // Disparo ANTES de que lleguen las definiciones de popups: el SDK debe guardarlo y
        // reproducirlo al cargar. Opcional para no abrir un popup en cada arranque.
        if (Settings.fireEventOnColdStart(this)) {
            sdk.triggerEvent(Catalog.CUSTOM_EVENT)
            HostLog.add("triggerEvent ${Catalog.CUSTOM_EVENT} (cold start)")
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                sdk.onForeground()
                HostLog.add("onForeground")
            }

            override fun onStop(owner: LifecycleOwner) {
                sdk.onBackground()
                HostLog.add("onBackground")
            }
        })
    }

    private inner class ContextFollower : ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            sdk.attachContext(PlatformContext(activity))
            Log.d(HostLog.TAG, "attachContext ${activity.javaClass.simpleName}")
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }

    companion object {
        lateinit var instance: ReferenceApp
            private set

        val sdk: DeepdotsPopupsSdk get() = instance.sdk
    }
}
