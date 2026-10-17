package ni.uni.nanobots.sentinel.servicio

import android.app.Activity
import android.app.Application
import android.os.Bundle

/** Sabe si alguna pantalla de la app está a la vista (para decidir entre aviso en pantalla o notificación). */
class VisibilidadApp : Application.ActivityLifecycleCallbacks {
    @Volatile
    private var iniciadas = 0

    val visible: Boolean get() = iniciadas > 0

    override fun onActivityStarted(activity: Activity) {
        iniciadas++
    }

    override fun onActivityStopped(activity: Activity) {
        iniciadas = (iniciadas - 1).coerceAtLeast(0)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
