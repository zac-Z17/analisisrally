package ni.uni.nanobots.sentinel.ui.receptor

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Equipo
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.MAX_VECINOS
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Pendiente
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import ni.uni.nanobots.sentinel.ui.comp.Anillo
import ni.uni.nanobots.sentinel.ui.comp.Boton
import ni.uni.nanobots.sentinel.ui.comp.Chip
import ni.uni.nanobots.sentinel.ui.comp.EstiloBoton
import ni.uni.nanobots.sentinel.ui.comp.EtiquetaSeccion
import ni.uni.nanobots.sentinel.ui.comp.Fila
import ni.uni.nanobots.sentinel.ui.comp.IcoCaja
import ni.uni.nanobots.sentinel.ui.comp.IconoBateria
import ni.uni.nanobots.sentinel.ui.comp.IconoBoton
import ni.uni.nanobots.sentinel.ui.comp.IconoLetra
import ni.uni.nanobots.sentinel.ui.comp.Interruptor
import ni.uni.nanobots.sentinel.ui.comp.ListaTarjeta
import ni.uni.nanobots.sentinel.ui.comp.Punto
import ni.uni.nanobots.sentinel.ui.comp.SeccionTitulo
import ni.uni.nanobots.sentinel.ui.comp.Tarjeta
import ni.uni.nanobots.sentinel.ui.comp.TarjetaPermisos
import ni.uni.nanobots.sentinel.ui.comp.estadoEquipo
import ni.uni.nanobots.sentinel.ui.comp.iconoClase
import ni.uni.nanobots.sentinel.ui.comp.iconoLugar
import ni.uni.nanobots.sentinel.ui.comp.nombreClase
import ni.uni.nanobots.sentinel.ui.comp.resumenEstado
import ni.uni.nanobots.sentinel.ui.comp.tonoClase
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo
import ni.uni.nanobots.sentinel.ui.tema.Tono

// ---------- Inicio ----------

@Composable
fun PestanaInicio(e: EstadoApp, ahora: Long, vm: ReceptorViewModel) {
    if (!e.demo) TarjetaPermisos(pantallaCompleta = true)
    val est = resumenEstado(e.cfg, ahora)
    val ss = e.sentinelas
    val vv = e.vecinosActivos
    val resaltada = est.estado != Vigilancia.REPOSO
    Tarjeta(
        borde = if (resaltada) est.tono.color.copy(alpha = 0.35f) else Paleta.Borde,
        fondo = if (resaltada) Brush.linearGradient(0f to est.tono.fondo, 0.6f to Paleta.Sup) else Brush.linearGradient(listOf(Paleta.Sup, Paleta.Sup)),
        relleno = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Anillo(est.icono, est.tono, 84.dp, latido = est.estado == Vigilancia.VIGILANDO, tamIcono = 26.dp)
            Column {
                Text(est.titulo, style = Tipo.Titulo.copy(fontWeight = FontWeight.ExtraBold), color = est.tono.color)
                Text(est.sub, style = Tipo.Sub)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip("${ss.size} SENTINELA${if (ss.size == 1) "" else "S"}", Tono.AZUL)
                    Chip("${vv.size} VECINO${if (vv.size == 1) "" else "S"}", Tono.VIOLETA)
                }
            }
        }
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (est.estado) {
                Vigilancia.PAUSA -> {
                    Boton("Reanudar ahora", vm::reanudar, Modifier.weight(1.25f), Iconos.Play, chico = true)
                    Boton("Detener", vm::detener, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
                }
                Vigilancia.VIGILANDO -> {
                    Boton("Pausar 10 min", vm::pausar, Modifier.weight(1.25f), Iconos.Pausa, chico = true)
                    Boton("Detener", vm::detener, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
                }
                Vigilancia.REPOSO -> {
                    Boton("Activar ahora", vm::activar, Modifier.weight(1.25f), Iconos.EscudoOk, chico = true)
                    if (e.cfg.modo != Modo.AUTO) {
                        Boton("Volver al horario", vm::volverAlHorario, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
                    } else {
                        Boton("Ver horario", { vm.pestana(Pestana.HORARIO) }, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
                    }
                }
            }
        }
        if (est.estado == Vigilancia.VIGILANDO) {
            Text(
                "«Pausar» sirve para abrir la puerta de noche sin que suene la alarma.",
                style = Tipo.T2.copy(fontSize = 11.5.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        EtiquetaSeccion("Sentinelas", modifier = Modifier.weight(1f))
        if (ss.isNotEmpty()) {
            Text(
                "GESTIONAR",
                style = Tipo.Etiqueta,
                color = Paleta.AzulC,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { vm.pestana(Pestana.EQUIPOS) }.padding(4.dp),
            )
        }
    }
    if (ss.isNotEmpty()) {
        ListaTarjeta(ss.map { s -> { FilaSentinela(s, e, ahora) { vm.abrirDetalle(s.id) } } })
    } else {
        Tarjeta(relleno = PaddingValues(horizontal = 16.dp, vertical = 22.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                IcoCaja(Iconos.Escudo, Tono.AZUL, 48.dp, 24.dp)
                Text("Aún no hay Sentinelas", style = Tipo.T1)
                Text(
                    "Toma un celular viejo, instala Sentinel en él y elige «Sentinela». Luego escanea el código que te mostramos.",
                    style = Tipo.T2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 280.dp),
                )
                Box(Modifier.widthIn(max = 240.dp)) {
                    Boton("Vincular el primero", { vm.vincular(Rol.SENTINELA) }, icono = Iconos.Mas, chico = true)
                }
            }
        }
    }
    e.alertas.firstOrNull()?.let { ult ->
        Row(Modifier.padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            val intento = ult.clase == ClaseAlerta.INTENTO
            Icon(if (intento) Iconos.Alerta else Iconos.Actividad, null, tint = if (intento) Paleta.Rojo else Paleta.Ambar, modifier = Modifier.size(16.dp))
            Text(
                "${Formato.fmtHora(ult.t)} · ${nombreClase(ult.clase)} en ${ult.nombre}${ult.que?.let { " (${it.clave})" }.orEmpty()}",
                style = Tipo.T2,
            )
        }
    }
    if (e.demo) Boton("Simular intento de entrada", vm::simularIntento, icono = Iconos.Alerta, estilo = EstiloBoton.PELIGRO)
}

@Composable
private fun FilaSentinela(s: Equipo, e: EstadoApp, ahora: Long, alTocar: () -> Unit) {
    val (txt, tono) = estadoEquipo(s, e.cfg, ahora)
    Fila(alTocar = alTocar) {
        IcoCaja(iconoLugar(s.lugar), Tono.AZUL, 38.dp, 19.dp)
        Column(Modifier.weight(1f)) {
            Text(s.nombre, style = Tipo.T1)
            Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Punto(tono.color)
                Text(txt, style = Tipo.T2, color = tono.color)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (s.bat != null) {
                val baja = s.bat < e.cfg.batMin && !s.carga
                val color = if (baja) Paleta.Ambar else Paleta.Txt
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    IconoBateria(s.bat, color)
                    Text("${s.bat} %", style = Tipo.T2, color = color)
                }
            } else {
                Text("—", style = Tipo.T2)
            }
            Text(s.temp?.let { "$it °C" } ?: Formato.hace(s.visto, ahora), style = Tipo.T2, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

// ---------- Equipos ----------

@Composable
fun PestanaEquipos(e: EstadoApp, ahora: Long, vm: ReceptorViewModel) {
    val ss = e.sentinelas
    val rr = e.receptores
    val vv = e.vecinos
    SeccionTitulo("Sentinelas · emisores", Tono.AZUL, ss.size)
    ListaTarjeta(
        ss.map { s ->
            {
                val (txt, tono) = estadoEquipo(s, e.cfg, ahora)
                FilaEquipo(s.nombre, iconoLugar(s.lugar), Tono.AZUL, alTocar = { vm.abrirDetalle(s.id) }) {
                    Text(txt + (s.bat?.let { " · $it %" } ?: ""), style = Tipo.T2, color = tono.color)
                }
            }
        },
        vacio = "Ninguno todavía.",
    )
    SeccionTitulo("Receptores", Tono.CIAN, rr.size + 1)
    ListaTarjeta(
        listOf<@Composable () -> Unit>({
            Fila {
                IcoCaja(Iconos.Celular, Tono.CIAN, 38.dp, 19.dp)
                Column(Modifier.weight(1f)) {
                    Text(e.yo.nombre, style = Tipo.T1)
                    Text("Este equipo", style = Tipo.T2)
                }
                if (e.admin) Chip("ADMIN", Tono.CIAN)
            }
        }) + rr.map { r ->
            {
                val icono = if (Regex("comput|lapt|pc", RegexOption.IGNORE_CASE).containsMatchIn(r.nombre)) Iconos.Monitor else Iconos.Celular
                FilaEquipo(r.nombre, icono, Tono.CIAN, alTocar = { vm.abrirDetalle(r.id) }) {
                    val sub = if (!r.demo && ahora - r.visto > 300_000) "Visto ${Formato.hace(r.visto, ahora)}" else "Recibe todas las alertas"
                    Text(sub, style = Tipo.T2)
                }
            }
        },
    )
    SeccionTitulo("Vecinos de confianza", Tono.VIOLETA, vv.size)
    ListaTarjeta(
        if (vv.isEmpty()) {
            emptyList()
        } else {
            listOf {
                FilaEquipo(Formato.listaNombres(vv.map { it.nombre }), Iconos.Personas, Tono.VIOLETA, alTocar = { vm.pestana(Pestana.VECINOS) }) {
                    Text("Solo reciben alertas fuertes", style = Tipo.T2)
                }
            }
        },
        vacio = "Invítalos desde la pestaña Vecinos.",
    )
    Boton("Vincular equipo", { vm.vincular() }, icono = Iconos.Mas, estilo = EstiloBoton.SECUNDARIO)
}

@Composable
private fun FilaEquipo(
    nombre: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    tono: Tono,
    alTocar: () -> Unit,
    sub: @Composable () -> Unit,
) {
    Fila(alTocar = alTocar) {
        IcoCaja(icono, tono, 38.dp, 19.dp)
        Column(Modifier.weight(1f)) {
            Text(nombre, style = Tipo.T1)
            sub()
        }
        Icon(Iconos.Flecha, null, tint = Paleta.Txt2, modifier = Modifier.size(18.dp))
    }
}

// ---------- Horario ----------

@Composable
fun PestanaHorario(e: EstadoApp, ahora: Long, vm: ReceptorViewModel) {
    val c = e.cfg
    val ctx = LocalContext.current
    val horas = ((Formato.aMin(c.fin) - Formato.aMin(c.inicio) + 1440) % 1440) / 60.0
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EtiquetaSeccion("Así queda tu día", modifier = Modifier.weight(1f))
            val h = if (horas % 1.0 == 0.0) horas.toInt().toString() else "%.1f".format(horas)
            Chip("$h h de vigilancia", Tono.AZUL)
        }
        BarraDia(c.carga, c.cargaIni, c.cargaFin, c.auto, c.inicio, c.fin, ahora)
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("12 m.", "6 p. m.", "12 a. m.", "6 a. m.", "12 m.").forEach { Text(it, style = Tipo.T2.copy(fontSize = 10.5.sp)) }
        }
        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Leyenda(Paleta.Verde, "Carga")
            Leyenda(Paleta.Azul, "Vigilancia")
            Leyenda(Paleta.Txt, "Ahora")
        }
    }
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IcoCaja(Iconos.Luna, Tono.AZUL, 34.dp, 17.dp)
            Text("Vigilancia automática", style = Tipo.T1, modifier = Modifier.weight(1f))
            Interruptor(c.auto, { vm.alternar(CampoCfg.AUTO) }, "Vigilancia automática")
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoHora("Empieza", c.inicio, Modifier.weight(1f)) { elegirHora(ctx, c.inicio) { vm.hora(CampoHora.INICIO, it) } }
            CampoHora("Termina", c.fin, Modifier.weight(1f)) { elegirHora(ctx, c.fin) { vm.hora(CampoHora.FIN, it) } }
        }
        val dias = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            "LMMJVSD".forEachIndexed { i, letra ->
                val on = c.dias[i]
                Box(
                    Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (on) Tono.AZUL.fondo else Paleta.Sup2)
                        .clickable(role = Role.Checkbox) { vm.dia(i) }
                        .semantics { contentDescription = dias[i] },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$letra", style = Tipo.T1.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = if (on) Paleta.AzulC else Paleta.Txt2)
                }
            }
        }
    }
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IcoCaja(Iconos.Rayo, Tono.VERDE, 34.dp, 17.dp)
            Column(Modifier.weight(1f)) {
                Text("Carga diaria", style = Tipo.T1)
                Text("${Formato.fmtMin(Formato.aMin(c.cargaIni))} – ${Formato.fmtMin(Formato.aMin(c.cargaFin))}", style = Tipo.T2)
            }
            Interruptor(c.carga, { vm.alternar(CampoCfg.CARGA) }, "Carga diaria")
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoHora("Desde", c.cargaIni, Modifier.weight(1f)) { elegirHora(ctx, c.cargaIni) { vm.hora(CampoHora.CARGA_INI, it) } }
            CampoHora("Hasta", c.cargaFin, Modifier.weight(1f)) { elegirHora(ctx, c.cargaFin) { vm.hora(CampoHora.CARGA_FIN, it) } }
        }
        Box(Modifier.padding(top = 12.dp)) { androidx.compose.material3.HorizontalDivider(color = Paleta.Borde) }
        Fila {
            Text("Avisarme 10 min antes", style = Tipo.Cuerpo, modifier = Modifier.weight(1f))
            Interruptor(c.aviso10, { vm.alternar(CampoCfg.AVISO10) }, "Avisarme 10 minutos antes")
        }
        Fila {
            Text("Batería mínima para vigilar", style = Tipo.Cuerpo, modifier = Modifier.weight(1f))
            IconoBoton(Iconos.Menos, "Menos", { vm.batMin(-5) }, tam = 32.dp)
            Text("${c.batMin} %", style = Tipo.Mono.copy(fontWeight = FontWeight.SemiBold), textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 48.dp))
            IconoBoton(Iconos.Mas, "Más", { vm.batMin(5) }, tam = 32.dp)
        }
        Fila {
            Column(Modifier.weight(1f)) {
                Text("Avisar si pasa de ${c.tempMax} °C", style = Tipo.Cuerpo)
                Text("El Sentinela mide la temperatura de su batería", style = Tipo.T2)
            }
            Interruptor(c.avisoTemp, { vm.alternar(CampoCfg.AVISO_TEMP) }, "Aviso de temperatura")
        }
    }
    Tarjeta(relleno = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IcoCaja(Iconos.Casa, Tono.VIOLETA, 34.dp, 17.dp)
            Column(Modifier.weight(1f)) {
                Text("Modo fuera de casa", style = Tipo.T1)
                Text("Vigila todo el día si la familia sale de viaje", style = Tipo.T2)
            }
            Interruptor(c.fuera, { vm.alternar(CampoCfg.FUERA) }, "Modo fuera de casa")
        }
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Punto(color)
        Text(texto, style = Tipo.T2.copy(fontSize = 12.sp, color = Paleta.Txt))
    }
}

/** Barra de 24 h, de mediodía a mediodía: carga en verde, vigilancia en azul y la hora actual. */
@Composable
private fun BarraDia(carga: Boolean, cargaIni: String, cargaFin: String, auto: Boolean, inicio: String, fin: String, ahora: Long) {
    val zona = java.time.ZoneId.systemDefault()
    val d = java.time.Instant.ofEpochMilli(ahora).atZone(zona)
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Paleta.Sup2),
    ) {
        fun pos(min: Int) = (((min / 60.0 - 12 + 24) % 24) / 24.0).toFloat()
        fun tramo(ini: String, fin2: String, color: Color) {
            val a = Formato.aMin(ini)
            val b = Formato.aMin(fin2)
            val l = pos(a)
            var w = ((b - a + 1440) % 1440) / 1440f
            if (w == 0f) w = 1f
            if (l + w <= 1f) {
                drawRect(color, Offset(l * size.width, 0f), Size(w * size.width, size.height))
            } else {
                drawRect(color, Offset(l * size.width, 0f), Size((1 - l) * size.width, size.height))
                drawRect(color, Offset(0f, 0f), Size((l + w - 1) * size.width, size.height))
            }
        }
        if (carga) tramo(cargaIni, cargaFin, Paleta.Verde)
        if (auto) tramo(inicio, fin, Paleta.Azul)
        val x = pos(d.hour * 60 + d.minute) * size.width
        drawRect(Paleta.Txt, Offset(x - 1.dp.toPx(), 0f), Size(2.dp.toPx(), size.height))
    }
}

@Composable
private fun CampoHora(etiqueta: String, hhmm: String, modifier: Modifier, alTocar: () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, style = Tipo.T2.copy(fontWeight = FontWeight.SemiBold))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Paleta.Sup2)
                .border(1.dp, Paleta.Borde, RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = alTocar)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(Formato.fmtMin(Formato.aMin(hhmm)), style = Tipo.T1.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold))
        }
    }
}

private fun elegirHora(ctx: Context, actual: String, alElegir: (Int) -> Unit) {
    val m = Formato.aMin(actual)
    TimePickerDialog(ctx, android.R.style.Theme_Material_Dialog, { _, h, min -> alElegir(h * 60 + min) }, m / 60, m % 60, false).show()
}

// ---------- Vecinos ----------

@Composable
fun PestanaVecinos(e: EstadoApp, ahora: Long, vm: ReceptorViewModel) {
    val ctx = LocalContext.current
    val vv = e.vecinos
    val elegirContacto = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val uri = r.data?.data ?: return@rememberLauncherForActivityResult
        leerContacto(ctx, uri)?.let { (nombre, tel) -> vm.agregarPendiente(nombre, tel, "") }
    }
    fun contactos() {
        try {
            elegirContacto.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
        } catch (x: ActivityNotFoundException) {
            vm.abrirContacto()
        }
    }

    Text(
        "Reciben la alerta solo cuando alguien intenta entrar. No ven horarios ni ajustes de la casa. Puedes agregar hasta 5.",
        style = Tipo.Sub,
    )
    val filas: List<@Composable () -> Unit> =
        vv.map { v -> @Composable { FilaVecino(v, e.cfg.vecinosOff, ahora, vm) } } +
            e.pendientes.map { p -> @Composable { FilaPendiente(p, vm) } }
    ListaTarjeta(filas, vacio = "Todavía no tienes vecinos de confianza.")
    Boton(
        "Invitar vecino", { vm.vincular(Rol.VECINO) }, icono = Iconos.AgregarPersona, estilo = EstiloBoton.VIOLETA,
        habilitado = vv.size + e.pendientes.size < MAX_VECINOS,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OpcionInvitar("WhatsApp", Iconos.Mensaje, Modifier.weight(1f)) { vm.vincular(Rol.VECINO) }
        OpcionInvitar("Código QR", Iconos.Qr, Modifier.weight(1f)) { vm.vincular(Rol.VECINO) }
        OpcionInvitar("Contactos", Iconos.Personas, Modifier.weight(1f)) { contactos() }
    }
    NumerosEmergencia()
}

@Composable
private fun FilaVecino(v: Equipo, apagados: Set<String>, ahora: Long, vm: ReceptorViewModel) {
    Fila {
        IconoLetra(v.nombre, Tono.VIOLETA)
        Column(Modifier.weight(1f)) {
            Text(v.nombre, style = Tipo.T1)
            val visto = if (!v.demo && ahora - v.visto > 300_000) " · visto ${Formato.hace(v.visto, ahora)}" else ""
            Text(v.ref.ifBlank { "Vecino de confianza" } + visto, style = Tipo.T2)
        }
        Interruptor(v.id !in apagados, { vm.alternarVecino(v.id) }, "Recibe alertas")
    }
}

@Composable
private fun FilaPendiente(p: Pendiente, vm: ReceptorViewModel) {
    Fila {
        IconoLetra(p.nombre, Tono.GRIS)
        Column(Modifier.weight(1f)) {
            Text(p.nombre, style = Tipo.T1)
            Text("Falta que se una con el código", style = Tipo.T2)
        }
        Chip("PENDIENTE", Tono.AMBAR)
        IconoBoton(Iconos.Mensaje, "Invitar", { vm.invitarPendiente(p.id) }, tam = 34.dp)
        IconoBoton(Iconos.Basura, "Quitar", { vm.quitarPendiente(p.id) }, tam = 34.dp)
    }
}

@Composable
private fun OpcionInvitar(texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, alTocar: () -> Unit) {
    Tarjeta(modifier, relleno = PaddingValues(vertical = 10.dp, horizontal = 6.dp), alTocar = alTocar) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icono, null, tint = Paleta.Violeta, modifier = Modifier.size(18.dp))
            Text(texto, style = Tipo.T2.copy(fontSize = 12.sp, color = Paleta.Txt))
        }
    }
}

private fun leerContacto(ctx: Context, uri: Uri): Pair<String, String>? = runCatching {
    ctx.contentResolver.query(
        uri,
        arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
        null, null, null,
    )?.use { cur ->
        if (!cur.moveToFirst()) return@use null
        (cur.getString(0) ?: "Vecino") to (cur.getString(1) ?: "")
    }
}.getOrNull()

/** Policía 118, Bomberos 115, Cruz Roja 128 (Nicaragua). */
@Composable
fun NumerosEmergencia() {
    val ctx = LocalContext.current
    EtiquetaSeccion("Números de emergencia (Nicaragua)", modifier = Modifier.padding(top = 4.dp))
    ListaTarjeta(
        listOf("Policía Nacional" to "118", "Bomberos" to "115", "Cruz Roja" to "128").map { (n, t) ->
            {
                Fila(alTocar = { llamar(ctx, t) }) {
                    Text(n, style = Tipo.Cuerpo, modifier = Modifier.weight(1f))
                    Text(t, style = Tipo.Mono.copy(fontWeight = FontWeight.SemiBold))
                    Box(Modifier.size(34.dp).clip(CircleShape).background(Tono.VERDE.fondo), contentAlignment = Alignment.Center) {
                        Icon(Iconos.Llamar, "Llamar", tint = Paleta.Verde, modifier = Modifier.size(15.dp))
                    }
                }
            }
        },
    )
}

/** Abre el marcador con el número (no llama solo: no hace falta permiso de llamadas). */
fun llamar(ctx: Context, numero: String) {
    runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

// ---------- Alertas ----------

@Composable
fun PestanaAlertas(e: EstadoApp, filtro: Filtro, ahora: Long, vm: ReceptorViewModel) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Filtro.entries.forEach { f ->
            val on = f == filtro
            Box(
                Modifier
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(if (on) Paleta.Azul else Paleta.Sup)
                    .border(1.dp, if (on) Paleta.Azul else Paleta.Borde, CircleShape)
                    .clickable(role = Role.Tab) { vm.filtro(f) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(f.texto, style = Tipo.T1.copy(fontSize = 12.5.sp), color = if (on) Color.White else Paleta.Txt2)
            }
        }
    }
    val lista = e.alertas.filter {
        when (filtro) {
            Filtro.TODAS -> true
            Filtro.INTENTOS -> it.clase == ClaseAlerta.INTENTO
            Filtro.LEVES -> it.clase == ClaseAlerta.LEVE
            Filtro.SISTEMA -> it.clase != ClaseAlerta.INTENTO && it.clase != ClaseAlerta.LEVE
        }
    }
    if (lista.isEmpty()) {
        Tarjeta {
            Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Iconos.EscudoOk, null, tint = Paleta.Txt2, modifier = Modifier.size(28.dp))
                Text(
                    "Todo tranquilo. Aquí aparecerán los intentos, las vibraciones leves y los avisos de batería o conexión.",
                    style = Tipo.Sub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
    val vecinosAvisados = e.vecinosActivos.size
    lista.groupBy { Formato.diaDe(it.t, ahora) }.forEach { (dia, items) ->
        EtiquetaSeccion(dia, modifier = Modifier.padding(top = 2.dp))
        items.forEach { TarjetaAlerta(it, vecinosAvisados, vm) }
    }
    Text(
        "Al responder «¿Qué fue?» el Sentinela ajusta su sensibilidad y da menos falsas alarmas.",
        style = Tipo.T2,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TarjetaAlerta(a: Alerta, vecinosAvisados: Int, vm: ReceptorViewModel) {
    val critica = a.clase == ClaseAlerta.INTENTO
    Tarjeta(
        borde = if (critica) Paleta.Rojo.copy(alpha = 0.45f) else Paleta.Borde,
        fondo = if (critica) Brush.linearGradient(0f to Tono.ROJO.fondo, 0.7f to Paleta.Sup) else Brush.linearGradient(listOf(Paleta.Sup, Paleta.Sup)),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IcoCaja(iconoClase(a.clase), tonoClase(a.clase), 38.dp, 19.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(nombreClase(a.clase), style = Tipo.T1, modifier = Modifier.weight(1f))
                    Text(Formato.fmtHora(a.t), style = Tipo.T2)
                }
                Text(a.nombre + if (a.detalle.isNotBlank()) " · ${a.detalle}" else "", style = Tipo.T2)
                if (critica) {
                    Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (vecinosAvisados > 0) Chip("$vecinosAvisados VECINO(S) AVISADO(S)", Tono.VIOLETA)
                        when {
                            a.que == Etiqueta.FALSA -> Chip("FALSA ALARMA", Tono.GRIS)
                            a.resuelta -> Chip("ATENDIDA", Tono.VERDE)
                        }
                    }
                }
                if (a.clase == ClaseAlerta.LEVE) {
                    Text("¿Qué fue?", style = Tipo.Cuerpo.copy(fontSize = 12.sp), modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(Etiqueta.LLUVIA to "Lluvia", Etiqueta.VIENTO to "Viento", Etiqueta.ANIMAL to "Animal", Etiqueta.YO to "Fui yo")
                            .forEach { (k, t) ->
                                val on = a.que == k
                                Box(
                                    Modifier
                                        .height(30.dp)
                                        .clip(CircleShape)
                                        .background(if (on) Paleta.Ambar else Paleta.Sup2)
                                        .clickable(role = Role.RadioButton) { vm.etiquetar(a.mid, k) }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(t, style = Tipo.T1.copy(fontSize = 12.sp), color = if (on) Paleta.Noche else Paleta.Txt2)
                                }
                            }
                    }
                }
            }
        }
    }
}
