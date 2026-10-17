package ni.uni.nanobots.sentinel.servicio

import kotlinx.coroutines.flow.StateFlow
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.puertos.Efectos

/** Implementación Android de los efectos: sirena, vibración y notificaciones. */
class EfectosAndroid(
    private val sirena: Sirena,
    private val vibrador: Vibrador,
    private val notificador: Notificador,
    private val visibilidad: VisibilidadApp,
) : Efectos {
    override val sirenaSonando: StateFlow<Boolean> = sirena.sonando

    override fun sirena(segundos: Int) {
        sirena.sonar(segundos)
    }

    override fun callar() {
        sirena.callar()
    }

    override fun bip(frecuencia: Int, duracionMs: Int, volumen: Float) {
        sirena.bip(frecuencia, duracionMs, volumen)
    }

    override fun vibrar(vararg patronMs: Long) = vibrador.vibrar(patronMs)

    override fun detenerVibracion() = vibrador.detener()

    /** Sirena de 60 s, vibración larga y la pantalla roja (a pantalla completa si el celular está bloqueado). */
    override fun mostrarAlarma(alerta: Alerta) {
        sirena.sonar(60)
        vibrador.vibrar(longArrayOf(600, 200, 600, 200, 600, 200, 600))
        notificador.alarma(alerta)
    }

    override fun cerrarAlarma() {
        sirena.callar()
        vibrador.detener()
        notificador.cerrarAlarma()
    }

    override fun notificar(texto: String) {
        if (!visibilidad.visible) notificador.aviso(texto)
    }
}
