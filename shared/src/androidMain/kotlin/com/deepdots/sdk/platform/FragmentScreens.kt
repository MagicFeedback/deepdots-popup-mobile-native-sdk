package com.deepdots.sdk.platform

import android.app.Activity
import android.view.View

/**
 * Pantallas por Fragment, solo si la app trae `androidx.fragment` (el SDK lo tiene como
 * `compileOnly`: no lo añade a la app). Cuenta el Fragment reanudado con vista visible; se
 * saltan los contenedores de Navigation (`NavHostFragment`) y los diálogos (`DialogFragment`,
 * bottom sheets), que al cerrarse no devuelven la pantalla de debajo.
 */
internal object FragmentScreens {
    private val available: Boolean by lazy {
        try {
            Class.forName("androidx.fragment.app.FragmentActivity")
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun attach(activity: Activity) {
        if (!available) return
        try {
            val fa = activity as? androidx.fragment.app.FragmentActivity ?: return
            fa.supportFragmentManager.registerFragmentLifecycleCallbacks(Callbacks, true)
        } catch (_: Throwable) {
        }
    }

    internal fun isScreen(className: String, isDialog: Boolean, view: View?): Boolean =
        !isDialog && view != null && !className.endsWith("NavHostFragment")

    private object Callbacks : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: androidx.fragment.app.FragmentManager, f: androidx.fragment.app.Fragment) {
            try {
                val name = f.javaClass.simpleName
                if (!isScreen(name, f is androidx.fragment.app.DialogFragment, f.view) || f.isHidden) return
                ActivityTracker.fragmentScreen(f.activity, name)
            } catch (_: Throwable) {
            }
        }
    }
}
