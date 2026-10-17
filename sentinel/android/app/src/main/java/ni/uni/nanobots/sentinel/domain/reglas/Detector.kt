package ni.uni.nanobots.sentinel.domain.reglas

import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.AjusteEquipo
import ni.uni.nanobots.sentinel.domain.modelo.Calibracion
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.puertos.Lectura
import kotlin.math.max
import kotlin.math.roundToInt

/** Umbrales efectivos: la calibración ajustada por la sensibilidad elegida. */
data class Umbrales(val leve: Double, val fuerte: Double, val muy: Double, val ang: Double) {
    companion object {
        fun de(cal: Calibracion?, aj: AjusteEquipo): Umbrales {
            val c = cal ?: Calibracion.FABRICA
            val n = aj.nivel
            return Umbrales(
                leve = c.leve * n.k * aj.ajuste,
                fuerte = c.fuerte * n.k,
                muy = c.muy * n.k,
                ang = c.ang * n.ang / 15.0,
            )
        }
    }
}

data class Deteccion(val clase: ClaseAlerta, val detalle: String, val pico: Double)

enum class Simulacion { GOLPE, APERTURA }

/** Giro acumulado: suma mientras gira a más de 4 °/s y baja 12 °/s cuando se detiene. */
class AcumuladorGiro {
    var ang = 0.0
        private set

    fun sumar(r: Double, dt: Double) {
        ang = if (r > 4) ang + r * dt else max(0.0, ang - 12 * dt)
    }

    fun reiniciar() {
        ang = 0.0
    }
}

/**
 * Algoritmo de detección del Centinela (igual al del prototipo web):
 * - INTENTO: la puerta gira más que el umbral, un golpe muy fuerte, o 3 golpes en 4 s (forcejeo).
 * - LEVE: vibración sobre el umbral leve sin intento en los 2,5 s siguientes; máximo una cada 30 s.
 * No guarda referencias a Android: se prueba con lecturas falsas.
 */
class Detector {
    private val giro = AcumuladorGiro()
    private val golpes = ArrayDeque<Long>()
    private var ultGolpe = 0L
    private var ultIntento = NUNCA
    private var ultLeve = NUNCA
    private var levePend = SIN_LEVE
    private var picoLeve = 0.0

    fun procesar(l: Lectura, u: Umbrales, vigilando: Boolean, prueba: Boolean, lugar: Lugar): Deteccion? {
        giro.sumar(l.r, l.dt)
        val t = l.t
        if (!vigilando && !prueba) {
            giro.reiniciar(); golpes.clear(); levePend = SIN_LEVE
            return null
        }
        if (t - ultIntento < if (prueba) ESPERA_PRUEBA_MS else ESPERA_INTENTO_MS) return null
        if (giro.ang > u.ang) {
            return intento(t, "La ${Formato.lugar(lugar)} se abrió (giró ${giro.ang.roundToInt()}°)", l.a, prueba)
        }
        if (l.a > u.muy) return intento(t, "Golpe muy fuerte", l.a, prueba)
        if (l.a > u.fuerte && t - ultGolpe > ENTRE_GOLPES_MS) {
            ultGolpe = t
            golpes.addLast(t)
        }
        while (golpes.isNotEmpty() && t - golpes.first() >= VENTANA_GOLPES_MS) golpes.removeFirst()
        if (golpes.size >= 3) return intento(t, "Golpes fuertes repetidos (forcejeo)", l.a, prueba)
        if (l.a > u.leve) {
            picoLeve = max(picoLeve, l.a)
            if (levePend == SIN_LEVE) levePend = t
        }
        return null
    }

    /** Se llama cada medio segundo: decide si una vibración leve pendiente se avisa. */
    fun revisarLeve(t: Long, vigilando: Boolean, prueba: Boolean, lugar: Lugar): Deteccion? {
        if (levePend == SIN_LEVE || t - levePend < ESPERA_LEVE_MS) return null
        val pico = picoLeve
        levePend = SIN_LEVE
        picoLeve = 0.0
        if (t - ultIntento < 5_000) return null
        if (prueba) return Deteccion(ClaseAlerta.PRUEBA, "Vibración leve detectada", pico)
        if (t - ultLeve > ENTRE_LEVES_MS && vigilando) {
            ultLeve = t
            return Deteccion(ClaseAlerta.LEVE, "Vibración rara en la ${Formato.lugar(lugar)}", pico)
        }
        return null
    }

    fun simular(tipo: Simulacion, t: Long, prueba: Boolean, lugar: Lugar): Deteccion {
        ultIntento = NUNCA
        val detalle = when (tipo) {
            Simulacion.APERTURA -> "La ${Formato.lugar(lugar)} se abrió (simulado)"
            Simulacion.GOLPE -> "Golpe muy fuerte (simulado)"
        }
        return intento(t, detalle, 9.0, prueba)
    }

    private fun intento(t: Long, detalle: String, a: Double, prueba: Boolean): Deteccion {
        ultIntento = t
        golpes.clear()
        giro.reiniciar()
        levePend = SIN_LEVE
        return Deteccion(if (prueba) ClaseAlerta.PRUEBA else ClaseAlerta.INTENTO, detalle, a)
    }

    private companion object {
        const val NUNCA = -1_000_000_000L
        const val SIN_LEVE = Long.MIN_VALUE
        const val ESPERA_INTENTO_MS = 20_000L
        const val ESPERA_PRUEBA_MS = 3_000L
        const val ENTRE_GOLPES_MS = 180L
        const val VENTANA_GOLPES_MS = 4_000L
        const val ESPERA_LEVE_MS = 2_500L
        const val ENTRE_LEVES_MS = 30_000L
    }
}
