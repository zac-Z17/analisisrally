package ni.uni.nanobots.sentinel.domain.reglas

import ni.uni.nanobots.sentinel.domain.modelo.Calibracion
import ni.uni.nanobots.sentinel.domain.puertos.Lectura
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Lo que la pantalla de calibración necesita mostrar. */
data class EstadoCalibracion(
    /** 1, 2 o 3; 4 = terminada. */
    val paso: Int = 1,
    val midiendo: Boolean = false,
    val restanteS: Int = 0,
    /** Paso 2: grados de giro medidos. Paso 3: golpes detectados. */
    val contador: Int = 0,
    val ok1: Boolean = false,
    val ok2: Boolean = false,
    val ok3: Boolean = false,
    val giro: Int = 0,
    val golpes: Int = 0,
)

/**
 * Calibración de 3 pasos:
 * 1. Quieta 5 s → leve = clamp(p95 × 5; 0,35; 1,6).
 * 2. Abrir y cerrar (10 s) → ang = clamp(giroMáx × 0,45; 8; 30).
 * 3. Tres golpes suaves → fuerte = clamp(mediana(picos) × 1,25; 1,2; 6).
 */
class Calibrador {
    private var paso = 1
    private var midiendo = false
    private var fin = 0L
    private val datos = ArrayList<Double>()
    private val giro = AcumuladorGiro()
    private var maxAng = 0.0
    private var contador = 0

    private var leve: Double? = null
    private var ang: Double? = null
    private var fuerte: Double? = null
    private var ok1 = false
    private var ok2 = false
    private var ok3 = false
    private var giroMedido = 0
    private var golpes = 0

    val terminada: Boolean get() = paso > 3
    val estaMidiendo: Boolean get() = midiendo

    fun reiniciar() {
        paso = 1; midiendo = false; datos.clear(); maxAng = 0.0; contador = 0
        leve = null; ang = null; fuerte = null; ok1 = false; ok2 = false; ok3 = false; giroMedido = 0; golpes = 0
    }

    fun empezar(p: Int, ahora: Long) {
        if (p == 1) reiniciar()
        paso = p
        midiendo = true
        datos.clear()
        maxAng = 0.0
        contador = 0
        giro.reiniciar()
        fin = ahora + if (p == 1) 5_000 else 10_000
    }

    fun muestra(l: Lectura) {
        if (!midiendo) return
        datos.add(l.a)
        giro.sumar(l.r, l.dt)
        maxAng = max(maxAng, giro.ang)
    }

    /** Avanza el reloj. Devuelve true si el paso acaba de terminar. */
    fun tic(ahora: Long): Boolean {
        if (!midiendo) return false
        val resta = fin - ahora
        if (paso == 3) {
            val n = picosDe(datos, (leve ?: Calibracion.FABRICA.leve) * 0.8).size
            contador = min(n, 3)
            if (n >= 3) return terminar()
        }
        if (paso == 2) {
            contador = maxAng.roundToInt()
            if (maxAng > 25 && giro.ang < 3 && resta < 8_000) return terminar()
        }
        if (resta <= 0) return terminar()
        return false
    }

    fun estado(ahora: Long) = EstadoCalibracion(
        paso = paso,
        midiendo = midiendo,
        restanteS = if (midiendo) max(0, ((fin - ahora + 999) / 1000).toInt()) else 0,
        contador = contador,
        ok1 = ok1, ok2 = ok2, ok3 = ok3,
        giro = giroMedido,
        golpes = golpes,
    )

    fun resultado(): Calibracion {
        val f = fuerte ?: Calibracion.FABRICA.fuerte
        return Calibracion(
            leve = leve ?: Calibracion.FABRICA.leve,
            fuerte = f,
            muy = max(f * 3, 6.0),
            ang = ang ?: Calibracion.FABRICA.ang,
        )
    }

    private fun terminar(): Boolean {
        midiendo = false
        when (paso) {
            1 -> {
                val s = datos.sorted()
                val p95 = if (s.isEmpty()) 0.1 else s[(s.size * 0.95).toInt().coerceAtMost(s.size - 1)]
                leve = (p95 * 5).coerceIn(0.35, 1.6)
                ok1 = datos.size > 20
            }
            2 -> {
                giroMedido = maxAng.roundToInt()
                ok2 = maxAng > 6
                ang = if (ok2) (maxAng * 0.45).coerceIn(8.0, 30.0) else Calibracion.FABRICA.ang
            }
            3 -> {
                val pk = picosDe(datos, (leve ?: Calibracion.FABRICA.leve) * 0.8)
                golpes = min(pk.size, 3)
                ok3 = pk.isNotEmpty()
                fuerte = if (ok3) (mediana(pk) * 1.25).coerceIn(1.2, 6.0) else Calibracion.FABRICA.fuerte
            }
        }
        paso += 1
        return true
    }

    companion object {
        /** Máximos locales sobre el umbral, separados por más de 8 muestras. */
        fun picosDe(arr: List<Double>, umbral: Double): List<Double> {
            val p = ArrayList<Double>()
            var ult = -99
            for (i in 1 until arr.size - 1) {
                if (arr[i] > umbral && arr[i] >= arr[i - 1] && arr[i] >= arr[i + 1] && i - ult > 8) {
                    p.add(arr[i]); ult = i
                }
            }
            return p
        }

        fun mediana(arr: List<Double>): Double {
            val s = arr.sorted()
            return if (s.isEmpty()) 0.0 else s[s.size / 2]
        }
    }
}
