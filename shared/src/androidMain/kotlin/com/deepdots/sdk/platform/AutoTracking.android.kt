package com.deepdots.sdk.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.deepdots.sdk.tracking.AutoSignals
import java.lang.ref.WeakReference

private const val TAG = "DeepdotsPopups"

/**
 * Activity actual y pantallas, por `ActivityLifecycleCallbacks` (+ `FragmentLifecycleCallbacks`
 * si la app usa `androidx.fragment`). Lo registra [DeepdotsInitProvider] antes de
 * `Application.onCreate`, así que ve la primera Activity aunque el host llame a `init()` tarde.
 */
internal object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private val main by lazy { Handler(Looper.getMainLooper()) }
    @Volatile private var current: WeakReference<Activity>? = null
    @Volatile private var installed = false
    /** Activity cuyo nombre de pantalla está pendiente (se cancela si un Fragment da uno). */
    private var pendingScreenFor: WeakReference<Activity>? = null

    fun install(app: Application) {
        synchronized(this) {
            if (installed) return
            installed = true
        }
        app.registerActivityLifecycleCallbacks(this)
    }

    fun currentActivity(): Activity? =
        current?.get()?.takeUnless { it.isFinishing || it.isDestroyed }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        FragmentScreens.attach(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        current = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
        // El nombre de la Activity sale en el siguiente ciclo del hilo principal: los Fragments
        // se reanudan después de onResume (onPostResume) y, si uno da su nombre, la Activity
        // (un contenedor) no cuenta como pantalla.
        pendingScreenFor = WeakReference(activity)
        main.post {
            if (pendingScreenFor?.get() === activity) {
                pendingScreenFor = null
                AutoSignals.screen(activity.javaClass.simpleName)
            }
        }
        AutoSignals.contextAvailable()
    }

    /** Un Fragment de [activity] se reanudó y dio su nombre. */
    fun fragmentScreen(activity: Activity?, name: String) {
        if (activity != null && pendingScreenFor?.get() === activity) pendingScreenFor = null
        AutoSignals.screen(name)
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) {
        if (current?.get() === activity) current = null
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (current?.get() === activity) current = null
        if (pendingScreenFor?.get() === activity) pendingScreenFor = null
    }
}

/** Ciclo de vida del PROCESO: ON_START / ON_STOP (este último llega ~700 ms después, sin rotaciones). */
private object ProcessLifecycle : DefaultLifecycleObserver {
    @Volatile var installed = false

    override fun onStart(owner: LifecycleOwner) = AutoSignals.foreground()

    override fun onStop(owner: LifecycleOwner) = AutoSignals.background()
}

internal actual fun installPlatformAutoTracking(lifecycle: Boolean, screens: Boolean) {
    // Sin application context (tests JVM, provider quitado del manifest) no hay nada que observar.
    val app = AppContextHolder.applicationContext as? Application ?: return
    try {
        ActivityTracker.install(app)
    } catch (t: Throwable) {
        Log.w(TAG, "Activity tracking unavailable: ${t.message}")
    }
    if (!lifecycle) return
    synchronized(ProcessLifecycle) {
        if (ProcessLifecycle.installed) return
        ProcessLifecycle.installed = true
    }
    val install: () -> Unit = {
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(ProcessLifecycle)
        } catch (t: Throwable) {
            // p. ej. el host quitó el InitializationProvider de androidx.startup.
            Log.w(TAG, "Automatic lifecycle unavailable (ProcessLifecycleOwner): ${t.message}")
        }
    }
    if (Looper.myLooper() == Looper.getMainLooper()) install() else Handler(Looper.getMainLooper()).post { install() }
}

internal actual fun detectedPlatformContext(): PlatformContext? =
    ActivityTracker.currentActivity()?.let { PlatformContext(it) }

internal actual fun hasDetectedPlatformContext(): Boolean = ActivityTracker.currentActivity() != null
