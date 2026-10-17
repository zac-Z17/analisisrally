package ni.uni.nanobots.sentinel.ui.sentinela

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import ni.uni.nanobots.sentinel.domain.puertos.Lectura
import ni.uni.nanobots.sentinel.domain.reglas.EstadoCalibracion
import ni.uni.nanobots.sentinel.domain.reglas.Simulacion
import ni.uni.nanobots.sentinel.domain.reglas.Umbrales
import ni.uni.nanobots.sentinel.ui.comp.Anillo
import ni.uni.nanobots.sentinel.ui.comp.Banner
import ni.uni.nanobots.sentinel.ui.comp.Boton
import ni.uni.nanobots.sentinel.ui.comp.Cabecera
import ni.uni.nanobots.sentinel.ui.comp.Chip
import ni.uni.nanobots.sentinel.ui.comp.Confirmacion
import ni.uni.nanobots.sentinel.ui.comp.Dato
import ni.uni.nanobots.sentinel.ui.comp.EstiloBoton
import ni.uni.nanobots.sentinel.ui.comp.EtiquetaSeccion
import ni.uni.nanobots.sentinel.ui.comp.Fila
import ni.uni.nanobots.sentinel.ui.comp.IcoCaja
import ni.uni.nanobots.sentinel.ui.comp.IconoBateria
import ni.uni.nanobots.sentinel.ui.comp.Interruptor
import ni.uni.nanobots.sentinel.ui.comp.ListaTarjeta
import ni.uni.nanobots.sentinel.ui.comp.Opcion
import ni.uni.nanobots.sentinel.ui.comp.Segmentado
import ni.uni.nanobots.sentinel.ui.comp.Tarjeta
import ni.uni.nanobots.sentinel.ui.comp.TarjetaPermisos
import ni.uni.nanobots.sentinel.ui.comp.nombreClase
import ni.uni.nanobots.sentinel.ui.comp.resumenEstado
import ni.uni.nanobots.sentinel.ui.fabrica
import ni.uni.nanobots.sentinel.ui.tema.Fuentes
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo
import ni.uni.nanobots.sentinel.ui.tema.Tono
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun SentinelaPantalla(vm: SentinelaViewModel = viewModel(factory = fabrica { SentinelaViewModel(it) })) {
    val e by vm.estado.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize().background(Paleta.Negro)) {
        when {
            e.cal == null -> Calibracion(e, vm)
            ui.ajustes -> {
                BackHandler { vm.cerrarAjustes() }
                AjustesSentinela(e, ui, vm)
            }
            else -> Principal(e, vm)
        }
    }
}

@Composable
private fun Columna(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = contenido,
    )
}

// ---------- pantalla principal ----------

@Composable
private fun Principal(e: EstadoApp, vm: SentinelaViewModel) {
    val ahora by vm.ahora.collectAsStateWithLifecycle()
    val conectado by vm.conectado.collectAsStateWithLifecycle()
    val local by vm.local.collectAsStateWithLifecycle()
    val vig by vm.vigilancia.collectAsStateWithLifecycle()
    val pruebaHasta by vm.pruebaHasta.collectAsStateWithLifecycle()
    val sirena by vm.sirenaSonando.collectAsStateWithLifecycle()
    val inicio = remember { SystemClock.elapsedRealtime() }

    val est = resumenEstado(e.cfg, ahora)
    val prueba = ahora < pruebaHasta
    val tono = if (prueba) Tono.AZUL else est.tono
    val u = vm.umbrales(e)

    Columna {
        Chip("CENTINELA · EMISOR", Tono.AZUL, icono = Iconos.Escudo)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(e.yo.nombre, style = Tipo.Titulo.copy(fontSize = 24.sp))
            Text(
                "Hogar «${e.hogar?.nombre.orEmpty()}» · ${if (conectado == false) "sin conexión" else "conectado"}",
                style = Tipo.T2.copy(fontSize = 13.sp),
            )
        }
        Box(Modifier.padding(top = 10.dp, bottom = 2.dp)) {
            Anillo(if (prueba) Iconos.Vibrar else est.icono, tono, 168.dp, latido = vig == Vigilancia.VIGILANDO || prueba, tamIcono = 50.dp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (prueba) "MODO PRUEBA" else est.titulo.uppercase(),
                style = Tipo.Titulo.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.9.sp),
                color = tono.color,
            )
            Text(
                if (prueba) "Golpea o mueve la ${Formato.lugar(e.yo.lugar)} (15 s)" else est.sub,
                style = Tipo.Sub,
                textAlign = TextAlign.Center,
            )
        }
        when {
            !vm.tieneAcelerometro -> Banner("Este celular no tiene acelerómetro: no puede detectar golpes.", Iconos.Alerta, Tono.AMBAR)
            !local.sensoresOk && SystemClock.elapsedRealtime() - inicio > 4_000 ->
                Banner("No llegan datos de los sensores. Reinicia la app.", Iconos.Alerta, Tono.AMBAR)
            !vm.tieneGiroscopio -> Banner("Sin giroscopio: detecta golpes, pero no aperturas.", Iconos.Info, Tono.AMBAR)
        }
        TarjetaPermisos(pantallaCompleta = false)
        if (sirena) Boton("Apagar sirena", vm::apagarSirena, icono = Iconos.Volumen, estilo = EstiloBoton.PELIGRO)
        Tarjeta(relleno = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            GraficaVibracion(vm.lecturas, u)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tarjeta(Modifier.weight(1f), relleno = PaddingValues(0.dp)) {
                Dato(local.bat?.let { "$it %" } ?: "—", if (local.carga) "Cargando" else "Batería", Modifier.fillMaxWidth()) {
                    IconoBateria(local.bat, Paleta.Verde)
                }
            }
            Tarjeta(Modifier.weight(1f), relleno = PaddingValues(0.dp)) {
                val caliente = local.temp != null && local.temp!! > e.cfg.tempMax
                Dato(local.temp?.let { "$it °C" } ?: "N/D", "Temperatura", Modifier.fillMaxWidth()) {
                    Icon(Iconos.Termometro, null, tint = if (caliente) Paleta.Ambar else Paleta.Verde, modifier = Modifier.size(18.dp))
                }
            }
            Tarjeta(Modifier.weight(1f), relleno = PaddingValues(0.dp)) {
                Dato(if (conectado == false) "Sin red" else "Bien", "Señal", Modifier.fillMaxWidth()) {
                    Icon(if (conectado == false) Iconos.SinWifi else Iconos.Wifi, null, tint = if (conectado == false) Paleta.Ambar else Paleta.Verde, modifier = Modifier.size(18.dp))
                }
            }
        }
        e.alertas.firstOrNull()?.let {
            Text("Último evento: ${nombreClase(it.clase)} · ${Formato.fmtHora(it.t)}", style = Tipo.T2)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "La pantalla se puede apagar: Centinel sigue vigilando en segundo plano.",
            style = Tipo.T2.copy(fontSize = 12.sp),
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Boton("Probar", vm::probar, Modifier.weight(1f), Iconos.Vibrar, EstiloBoton.SECUNDARIO)
            Boton("Ajustes", vm::abrirAjustes, Modifier.weight(1f), Iconos.Candado, EstiloBoton.SECUNDARIO)
        }
    }
}

/** Línea de vibración de los últimos 4 s con los umbrales leve (ámbar) y golpe (rojo). */
@Composable
private fun GraficaVibracion(lecturas: Flow<Lectura>, u: Umbrales) {
    val muestras = remember { ArrayDeque<Lectura>() }
    var cuadro by remember { mutableLongStateOf(0L) }
    LaunchedEffect(lecturas) {
        lecturas.collect { l ->
            muestras.addLast(l)
            while (muestras.isNotEmpty() && l.t - muestras.first().t > 4_000) muestras.removeFirst()
        }
    }
    LaunchedEffect(Unit) {
        while (true) withFrameMillis { cuadro = it }
    }
    @Suppress("UNUSED_VARIABLE")
    val redibujar = cuadro
    val t = SystemClock.elapsedRealtime()
    val maximo = muestras.filter { t - it.t < 1_500 }.maxOfOrNull { it.a } ?: 0.0
    val (texto, tono) = when {
        maximo > u.fuerte -> "FUERTE" to Tono.ROJO
        maximo > u.leve -> "LEVE" to Tono.AMBAR
        else -> "NORMAL" to Tono.VERDE
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        EtiquetaSeccion("Vibración en vivo", modifier = Modifier.weight(1f))
        Chip(texto, tono)
    }
    Canvas(Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp)) {
        val tope = max(u.fuerte * 1.3, 1.5)
        fun y(v: Double) = (size.height - 4f - (v / tope).coerceIn(0.0, 1.0).toFloat() * (size.height - 8f))
        val guion = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        listOf(u.leve to Paleta.Ambar, u.fuerte to Paleta.Rojo).forEach { (v, c) ->
            drawLine(c.copy(alpha = 0.5f), Offset(0f, y(v)), Offset(size.width, y(v)), 1.dp.toPx(), pathEffect = guion)
        }
        val camino = Path()
        if (muestras.isEmpty()) {
            camino.moveTo(0f, y(0.0)); camino.lineTo(size.width, y(0.0))
        } else {
            muestras.forEachIndexed { i, s ->
                val x = size.width - (t - s.t) / 4_000f * size.width
                if (i == 0) camino.moveTo(x, y(s.a)) else camino.lineTo(x, y(s.a))
            }
        }
        drawPath(camino, tono.color, style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
    }
}

// ---------- calibración ----------

@Composable
private fun Calibracion(e: EstadoApp, vm: SentinelaViewModel) {
    val cal by vm.calibracion.collectAsStateWithLifecycle()
    val p = cal.paso
    Columna {
        Chip("CALIBRACIÓN · ${e.yo.nombre.ifBlank { "CENTINELA" }.uppercase()}", Tono.AZUL, icono = Iconos.Actividad)
        Pasos(cal)
        if (p > 3) {
            IcoCaja(Iconos.Check, Tono.VERDE, 64.dp, 32.dp)
            Text("¡Listo! Sensibilidad ajustada", style = Tipo.Titulo, textAlign = TextAlign.Center)
            ListaTarjeta(
                listOf(
                    Triple("Quieta", cal.ok1, if (cal.ok1) "Ruido medido" else "Sin datos"),
                    Triple("Abrir y cerrar", cal.ok2, if (cal.ok2) "Giro de ${cal.giro}°" else "No se detectó giro"),
                    Triple("Golpes suaves", cal.ok3, if (cal.ok3) "${cal.golpes} detectados" else "No se detectaron"),
                ).map { (paso, ok, txt) ->
                    {
                        Fila {
                            Text(paso, style = Tipo.Cuerpo, modifier = Modifier.weight(1f))
                            Text(txt, style = Tipo.Cuerpo, color = if (ok) Paleta.Verde else Paleta.Ambar)
                        }
                    }
                },
            )
            Spacer(Modifier.height(12.dp))
            Boton("Guardar y empezar a vigilar", { vm.guardarCalibracion(false) }, icono = Iconos.EscudoOk)
            Boton("Repetir", { vm.empezarPaso(1) }, estilo = EstiloBoton.SECUNDARIO)
            return@Columna
        }
        val (titulo, texto, icono) = when (p) {
            1 -> Triple("Deja la puerta quieta", "No toques la puerta ni el celular por 5 segundos. Así medimos el ruido normal de tu casa.", Iconos.Reloj)
            2 -> Triple("Ábrela y ciérrala", "Abre la puerta o ventana con normalidad y vuelve a cerrarla.", Iconos.PuertaAbierta)
            else -> Triple("Da 3 golpes suaves", "Toca 3 veces cerca del soporte, como si alguien llamara suave.", Iconos.Vibrar)
        }
        EtiquetaSeccion("Paso $p de 3")
        IcoCaja(icono, Tono.AZUL, 72.dp, 36.dp)
        Text(titulo, style = Tipo.Titulo, textAlign = TextAlign.Center)
        Text(texto, style = Tipo.Sub, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 320.dp))
        if (cal.midiendo) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${cal.restanteS}", style = Tipo.Titulo.copy(fontFamily = Fuentes.Sora, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, lineHeight = 50.sp), color = Paleta.AzulC)
                Text(" s", style = Tipo.Titulo.copy(fontSize = 20.sp), color = Paleta.AzulC)
            }
            Text(
                when (p) {
                    1 -> "Midiendo…"
                    2 -> "Giro medido: ${cal.contador}°"
                    else -> "Golpes: ${cal.contador}"
                },
                style = Tipo.T2,
            )
        }
        if (!vm.tieneAcelerometro) {
            Banner("Este celular no tiene acelerómetro. Sigue con los valores de fábrica.", Iconos.Alerta, Tono.AMBAR)
        } else if (p == 2 && !vm.tieneGiroscopio) {
            Banner("Este celular no tiene giroscopio: no podrá detectar aperturas, solo golpes.", Iconos.Info, Tono.AMBAR)
        }
        Spacer(Modifier.height(12.dp))
        if (!cal.midiendo) Boton(if (p == 1) "Empezar" else "Listo, empezar", { vm.empezarPaso(p) }, icono = Iconos.Play)
        Boton("Omitir y usar valores de fábrica", { vm.guardarCalibracion(true) }, estilo = EstiloBoton.SECUNDARIO)
    }
}

@Composable
private fun Pasos(cal: EstadoCalibracion) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..3).forEach { i ->
            val hecho = i < cal.paso || (i == cal.paso && cal.midiendo)
            Box(
                Modifier
                    .width(28.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (hecho) Paleta.AzulC else Paleta.Borde),
            )
        }
    }
}

// ---------- ajustes con PIN ----------

@Composable
private fun AjustesSentinela(e: EstadoApp, ui: UiSentinela, vm: SentinelaViewModel) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Cabecera("Ajustes del Centinela", alVolver = vm::cerrarAjustes)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = if (ui.pinOk) Alignment.Start else Alignment.CenterHorizontally,
        ) {
            if (!ui.pinOk) {
                Teclado(ui.pin, vm::tecla)
                return@Column
            }
            val aj = e.cfg.ajusteDe(e.yo.id)
            val u = vm.umbrales(e)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("¿Dónde está el soporte?", style = Tipo.T2.copy(fontWeight = FontWeight.SemiBold))
                Segmentado(listOf(Opcion(Lugar.PUERTA, "Puerta", Iconos.Puerta), Opcion(Lugar.VENTANA, "Ventana", Iconos.Ventana)), e.yo.lugar, vm::lugar)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Boton("Activar ahora", vm::activar, Modifier.weight(1f), Iconos.EscudoOk, chico = true)
                Boton("Detener", vm::detener, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
            }
            Tarjeta(relleno = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IcoCaja(Iconos.Sirena, Tono.ROJO, 34.dp, 17.dp)
                    Column(Modifier.weight(1f)) {
                        Text("Sirena en este equipo", style = Tipo.T1)
                        Text("Sensibilidad: ${aj.nivel.clave} (se cambia desde un Receptor)", style = Tipo.T2)
                    }
                    Interruptor(aj.sirena, { vm.sirena() }, "Sirena")
                }
            }
            Boton("Repetir calibración", vm::recalibrar, icono = Iconos.Actividad, estilo = EstiloBoton.SECUNDARIO)
            EtiquetaSeccion("Para probar sin forzar la puerta", modifier = Modifier.padding(top = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Boton("Simular golpe fuerte", { vm.simular(Simulacion.GOLPE) }, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
                Boton("Simular apertura", { vm.simular(Simulacion.APERTURA) }, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
            }
            Text(
                "Umbrales actuales: leve ${"%.2f".format(u.leve)} m/s² · golpe ${"%.2f".format(u.fuerte)} m/s² · giro ${u.ang.roundToInt()}°",
                style = Tipo.T2,
            )
            if (ui.confirmarSalir) {
                Confirmacion("¿Desvincular este Centinela?", "Desvincular", vm::salir, { vm.confirmarSalir(false) })
            } else {
                Boton("Desvincular este equipo", { vm.confirmarSalir(true) }, icono = Iconos.Basura, estilo = EstiloBoton.PELIGRO)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Teclado(pin: String, alTocar: (String) -> Unit) {
    IcoCaja(Iconos.Candado, Tono.AZUL, 52.dp, 26.dp)
    Text("Escribe el PIN del administrador", style = Tipo.T1)
    Text(
        "●".repeat(pin.length) + "○".repeat(4 - pin.length),
        style = Tipo.Mono.copy(fontSize = 30.sp, letterSpacing = 12.sp),
    )
    val teclas = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", SentinelaViewModel.BORRAR)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        teclas.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                fila.forEach { k ->
                    if (k.isEmpty()) {
                        Spacer(Modifier.size(72.dp, 56.dp))
                    } else {
                        Tarjeta(Modifier.size(72.dp, 56.dp), relleno = PaddingValues(0.dp), alTocar = { alTocar(k) }) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (k == SentinelaViewModel.BORRAR) {
                                    Icon(Iconos.Atras, "Borrar", tint = Paleta.Txt, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(k, style = Tipo.Cuerpo.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Text("El PIN se definió al crear el hogar en el celular administrador.", style = Tipo.T2, textAlign = TextAlign.Center)
}
