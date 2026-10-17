package ni.uni.nanobots.sentinel

import android.app.Application
import ni.uni.nanobots.sentinel.di.Contenedor

class SentinelApp : Application() {
    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
        registerActivityLifecycleCallbacks(contenedor.visibilidad)
        contenedor.notificador.crearCanales()
    }
}
