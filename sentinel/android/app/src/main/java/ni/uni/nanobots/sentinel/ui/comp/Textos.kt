package ni.uni.nanobots.sentinel.ui.comp

import androidx.compose.ui.graphics.vector.ImageVector
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Equipo
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import ni.uni.nanobots.sentinel.domain.reglas.Horario
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Tono
import kotlin.math.ceil
import kotlin.math.max

/** Textos de estado de la vigilancia (siempre los mismos: diseño, sección 7). */
data class ResumenEstado(val estado: Vigilancia, val titulo: String, val sub: String, val tono: Tono) {
    val icono: ImageVector
        get() = when (estado) {
            Vigilancia.VIGILANDO -> Iconos.EscudoOk
            Vigilancia.PAUSA -> Iconos.Pausa
            Vigilancia.REPOSO -> Iconos.Luna
        }
}

fun resumenEstado(cfg: Config, ahora: Long): ResumenEstado {
    return when (Horario.estado(cfg, ahora)) {
        Vigilancia.PAUSA -> {
            val min = max(1, ceil((cfg.pausaHasta - ahora) / 60_000.0).toInt())
            ResumenEstado(Vigilancia.PAUSA, "En pausa", "Vuelve en $min min", Tono.AMBAR)
        }
        Vigilancia.VIGILANDO -> {
            val sub = when {
                cfg.fuera -> "Modo fuera de casa: todo el día"
                cfg.modo == Modo.ON -> "Activada a mano"
                else -> "Hasta las ${Formato.fmtMin(Formato.aMin(cfg.fin))}"
            }
            ResumenEstado(Vigilancia.VIGILANDO, "Vigilando", sub, Tono.VERDE)
        }
        Vigilancia.REPOSO -> {
            val pi = Horario.proximoInicio(cfg, ahora)
            var sub = if (pi > 0) {
                "Se activa ${Formato.cuando(pi, ahora)} a las ${Formato.fmtHora(pi)}"
            } else {
                "El horario automático está apagado"
            }
            if (cfg.modo == Modo.OFF && cfg.modoHasta > ahora) sub = "Detenida a mano. $sub"
            ResumenEstado(Vigilancia.REPOSO, "En reposo", sub, Tono.GRIS)
        }
    }
}

fun nombreClase(c: ClaseAlerta): String = when (c) {
    ClaseAlerta.INTENTO -> "Intento de entrada"
    ClaseAlerta.LEVE -> "Vibración leve"
    ClaseAlerta.BATERIA -> "Batería baja"
    ClaseAlerta.CONEXION -> "Sin conexión"
    ClaseAlerta.CALOR -> "Temperatura alta"
    ClaseAlerta.TRANQUILA -> "Noche tranquila"
    ClaseAlerta.PRUEBA -> "Prueba"
}

fun iconoClase(c: ClaseAlerta): ImageVector = when (c) {
    ClaseAlerta.INTENTO -> Iconos.PuertaAbierta
    ClaseAlerta.LEVE -> Iconos.Vibrar
    ClaseAlerta.BATERIA -> Iconos.Bateria
    ClaseAlerta.CONEXION -> Iconos.SinWifi
    ClaseAlerta.CALOR -> Iconos.Termometro
    ClaseAlerta.TRANQUILA -> Iconos.EscudoOk
    ClaseAlerta.PRUEBA -> Iconos.Check
}

fun tonoClase(c: ClaseAlerta): Tono = when (c) {
    ClaseAlerta.INTENTO -> Tono.ROJO
    ClaseAlerta.LEVE, ClaseAlerta.BATERIA, ClaseAlerta.CALOR -> Tono.AMBAR
    ClaseAlerta.TRANQUILA -> Tono.VERDE
    else -> Tono.GRIS
}

fun iconoLugar(l: Lugar): ImageVector = if (l == Lugar.VENTANA) Iconos.Ventana else Iconos.Puerta

const val SIN_CONEXION_MS = 150_000L

fun sinConexion(e: Equipo, ahora: Long) = !e.demo && ahora - e.visto > SIN_CONEXION_MS

/** Estado de un Sentinela tal como lo ve un receptor. */
fun estadoEquipo(e: Equipo, cfg: Config, ahora: Long): Pair<String, Tono> {
    if (sinConexion(e, ahora)) return "Sin conexión" to Tono.GRIS
    if (!e.sensoresOk) return "Sin datos de sensores" to Tono.AMBAR
    if (e.temp != null && cfg.avisoTemp && e.temp > cfg.tempMax) return "Muy caliente · ${e.temp} °C" to Tono.AMBAR
    if (e.bat != null && e.bat < cfg.batMin && !e.carga) return "Batería baja" to Tono.AMBAR
    val v = when (e.vig) {
        Vigilancia.VIGILANDO -> "Vigilando"
        Vigilancia.REPOSO -> "En reposo"
        Vigilancia.PAUSA -> "En pausa"
        null -> "Conectado"
    }
    val tono = when (e.vig) {
        Vigilancia.VIGILANDO -> Tono.VERDE
        Vigilancia.PAUSA -> Tono.AMBAR
        else -> Tono.GRIS
    }
    return (if (e.carga) "$v · cargando" else v) to tono
}
