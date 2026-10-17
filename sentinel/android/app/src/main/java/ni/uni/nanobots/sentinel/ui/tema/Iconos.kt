package ni.uni.nanobots.sentinel.ui.tema

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Íconos Lucide (licencia ISC), los mismos trazos que el prototipo web. */
object Iconos {
    private fun circulo(cx: Float, cy: Float, r: Float) =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float) =
        "M${x + rx} ${y}h${w - 2 * rx}a$rx $rx 0 0 1 $rx ${rx}v${h - 2 * rx}a$rx $rx 0 0 1 ${-rx} ${rx}" +
            "h${-(w - 2 * rx)}a$rx $rx 0 0 1 ${-rx} ${-rx}v${-(h - 2 * rx)}a$rx $rx 0 0 1 $rx ${-rx}z"

    private fun lucide(nombre: String, vararg trazos: String): ImageVector =
        ImageVector.Builder(nombre, 24.dp, 24.dp, 24f, 24f).apply {
            trazos.forEach {
                addPath(
                    pathData = addPathNodes(it),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    private const val ESCUDO = "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"
    private const val ALTAVOZ = "M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z"

    val Escudo by lazy { lucide("escudo", ESCUDO) }
    val EscudoOk by lazy { lucide("escudoOk", ESCUDO, "m9 12 2 2 4-4") }
    val Campana by lazy {
        lucide(
            "campana", "M10.268 21a2 2 0 0 0 3.464 0",
            "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326",
        )
    }
    val Casa by lazy {
        lucide(
            "casa", "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8",
            "M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z",
        )
    }
    val Reloj by lazy { lucide("reloj", circulo(12f, 12f, 10f), "M12 6v6l4 2") }
    val Personas by lazy {
        lucide(
            "personas", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2", circulo(9f, 7f, 4f),
            "M22 21v-2a4 4 0 0 0-3-3.87", "M16 3.13a4 4 0 0 1 0 7.75",
        )
    }
    val Celular by lazy { lucide("celular", rect(5f, 2f, 14f, 20f, 2f), "M12 18h.01") }
    val Monitor by lazy { lucide("monitor", rect(2f, 3f, 20f, 14f, 2f), "M8 21h8", "M12 17v4") }
    val Controles by lazy {
        lucide(
            "controles", "M21 4h-7", "M10 4H3", "M21 12h-9", "M8 12H3", "M21 20h-5", "M12 20H3",
            "M14 2v4", "M8 10v4", "M16 18v4",
        )
    }
    val Termometro by lazy { lucide("termometro", "M14 4v10.54a4 4 0 1 1-4 0V4a2 2 0 0 1 4 0Z") }
    val Wifi by lazy {
        lucide("wifi", "M12 20h.01", "M2 8.82a15 15 0 0 1 20 0", "M5 12.859a10 10 0 0 1 14 0", "M8.5 16.429a5 5 0 0 1 7 0")
    }
    val SinWifi by lazy {
        lucide(
            "sinWifi", "M12 20h.01", "M8.5 16.429a5 5 0 0 1 7 0", "M5 12.859a10 10 0 0 1 5.17-2.69",
            "M19 12.859a10 10 0 0 0-2.007-1.523", "M2 8.82a15 15 0 0 1 4.177-2.643",
            "M22 8.82a15 15 0 0 0-11.288-3.764", "m2 2 20 20",
        )
    }
    val Mas by lazy { lucide("mas", "M5 12h14", "M12 5v14") }
    val Menos by lazy { lucide("menos", "M5 12h14") }
    val Alerta by lazy {
        lucide("alerta", "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3", "M12 9v4", "M12 17h.01")
    }
    val Luna by lazy { lucide("luna", "M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z") }
    val Rayo by lazy {
        lucide(
            "rayo",
            "M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z",
        )
    }
    val Llamar by lazy {
        lucide(
            "llamar",
            "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z",
        )
    }
    val Sirena by lazy { lucide("sirena", ALTAVOZ, "M16 9a5 5 0 0 1 0 6", "M19.364 18.364a9 9 0 0 0 0-12.728") }
    val Volumen by lazy { lucide("volumen", ALTAVOZ, "M16 9a5 5 0 0 1 0 6") }
    val Puerta by lazy { lucide("puerta", "M18 20V6a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v14", "M2 20h20", "M14 12v.01") }
    val PuertaAbierta by lazy {
        lucide(
            "puertaAbierta", "M13 4h3a2 2 0 0 1 2 2v14", "M2 20h3", "M13 20h9", "M10 12v.01",
            "M13 4.562v16.157a1 1 0 0 1-1.242.97L5 20V5.562a2 2 0 0 1 1.515-1.94l4-1A2 2 0 0 1 13 4.561Z",
        )
    }
    val Ventana by lazy { lucide("ventana", rect(3f, 3f, 18f, 18f, 2f), "M3 12h18", "M12 3v18") }
    val Vibrar by lazy { lucide("vibrar", "m2 8 2 2-2 2 2 2-2 2", "m22 8-2 2 2 2-2 2 2 2", rect(8f, 5f, 8f, 14f, 1f)) }
    val Escanear by lazy {
        lucide(
            "escanear", "M3 7V5a2 2 0 0 1 2-2h2", "M17 3h2a2 2 0 0 1 2 2v2", "M21 17v2a2 2 0 0 1-2 2h-2",
            "M7 21H5a2 2 0 0 1-2-2v-2", "M7 12h10",
        )
    }
    val Flecha by lazy { lucide("flecha", "m9 18 6-6-6-6") }
    val Atras by lazy { lucide("atras", "m12 19-7-7 7-7", "M19 12H5") }
    val Adelante by lazy { lucide("adelante", "M5 12h14", "m12 5 7 7-7 7") }
    val Mensaje by lazy { lucide("mensaje", "M7.9 20A9 9 0 1 0 4 16.1L2 22Z") }
    val Pausa by lazy { lucide("pausa", rect(14f, 4f, 4f, 16f, 1f), rect(6f, 4f, 4f, 16f, 1f)) }
    val Play by lazy { lucide("play", "M6 3v18l15-9z") }
    val Candado by lazy { lucide("candado", rect(3f, 11f, 18f, 11f, 2f), "M7 11V7a5 5 0 0 1 10 0v4") }
    val Actividad by lazy {
        lucide(
            "actividad",
            "M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2",
        )
    }
    val AgregarPersona by lazy {
        lucide("agregarPersona", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2", circulo(9f, 7f, 4f), "M19 8v6", "M22 11h-6")
    }
    val Qr by lazy {
        lucide(
            "qr", rect(3f, 3f, 5f, 5f, 1f), rect(16f, 3f, 5f, 5f, 1f), rect(3f, 16f, 5f, 5f, 1f),
            "M21 16h-3a2 2 0 0 0-2 2v3", "M21 21v.01", "M12 7v3a2 2 0 0 1-2 2H7", "M3 12h.01", "M12 3h.01",
            "M12 16v.01", "M16 12h1", "M21 12v.01", "M12 21v-1",
        )
    }
    val Cerrar by lazy { lucide("cerrar", "M18 6 6 18", "m6 6 12 12") }
    val Check by lazy { lucide("check", "M20 6 9 17l-5-5") }
    val Basura by lazy {
        lucide("basura", "M3 6h18", "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6", "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2")
    }
    val Info by lazy { lucide("info", circulo(12f, 12f, 10f), "M12 16v-4", "M12 8h.01") }
    val Bateria by lazy { lucide("bateria", rect(2f, 7f, 16f, 10f, 2f), "M22 11v2") }
}
