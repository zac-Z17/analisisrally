package ni.uni.nanobots.sentinel.ui.receptor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Nivel
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.ui.comp.Banner
import ni.uni.nanobots.sentinel.ui.comp.Boton
import ni.uni.nanobots.sentinel.ui.comp.Cabecera
import ni.uni.nanobots.sentinel.ui.comp.Campo
import ni.uni.nanobots.sentinel.ui.comp.Chip
import ni.uni.nanobots.sentinel.ui.comp.CodigoQr
import ni.uni.nanobots.sentinel.ui.comp.Confirmacion
import ni.uni.nanobots.sentinel.ui.comp.Dato
import ni.uni.nanobots.sentinel.ui.comp.EstiloBoton
import ni.uni.nanobots.sentinel.ui.comp.IcoCaja
import ni.uni.nanobots.sentinel.ui.comp.IconoBateria
import ni.uni.nanobots.sentinel.ui.comp.Interruptor
import ni.uni.nanobots.sentinel.ui.comp.Opcion
import ni.uni.nanobots.sentinel.ui.comp.Punto
import ni.uni.nanobots.sentinel.ui.comp.Segmentado
import ni.uni.nanobots.sentinel.ui.comp.Tarjeta
import ni.uni.nanobots.sentinel.ui.comp.estadoEquipo
import ni.uni.nanobots.sentinel.ui.comp.sinConexion
import ni.uni.nanobots.sentinel.ui.tema.Fuentes
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo
import ni.uni.nanobots.sentinel.ui.tema.Tono

@Composable
fun Hojas(hoja: Hoja, e: EstadoApp, ui: UiReceptor, ahora: Long, vm: ReceptorViewModel) {
    when (hoja) {
        Hoja.Vincular -> HojaVincular(e, ahora, vm)
        is Hoja.Detalle -> HojaDetalle(hoja.id, e, ui, ahora, vm)
        Hoja.Ajustes -> HojaAjustes(e, ui, vm)
        Hoja.Contacto -> HojaContacto(vm)
    }
}

@Composable
private fun MarcoHoja(titulo: String, sub: String? = null, alCerrar: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Cabecera(titulo, alVolver = alCerrar)
        }
        if (sub != null) Text(sub, style = Tipo.T2, modifier = Modifier.padding(start = 62.dp).padding(bottom = 6.dp))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = contenido,
        )
    }
}

// ---------- Vincular ----------

@Composable
private fun HojaVincular(e: EstadoApp, ahora: Long, vm: ReceptorViewModel) {
    val v by vm.vinculacion.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    MarcoHoja("Vincular equipo", alCerrar = vm::cerrarHoja) {
        val vinc = v ?: return@MarcoHoja
        Segmentado(
            listOf(
                Opcion(Rol.SENTINELA, "Sentinela", Iconos.Escudo),
                Opcion(Rol.RECEPTOR, "Receptor", Iconos.Campana),
                Opcion(Rol.VECINO, "Vecino", Iconos.Personas),
            ),
            vinc.rol,
            vm::rolVinculo,
        )
        if (e.demo) Banner("En la demostración no se vinculan equipos reales.", Iconos.Info, Tono.AMBAR)
        Tarjeta(relleno = PaddingValues(18.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CodigoQr(vm.enlaceUnir(vinc))
                Text(
                    vinc.codigo.take(3) + " " + vinc.codigo.drop(3),
                    style = TextStyle(fontFamily = Fuentes.Mono, fontWeight = FontWeight.Medium, fontSize = 32.sp, letterSpacing = 3.8.sp, color = Paleta.Txt),
                )
                val resta = ((vinc.vence - ahora).coerceAtLeast(0) / 1000).toInt()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Iconos.Reloj, null, tint = Paleta.Txt2, modifier = Modifier.size(15.dp))
                    Text("El código vence en", style = Tipo.T2.copy(fontSize = 13.sp))
                    Text("${resta / 60}:${(resta % 60).toString().padStart(2, '0')}", style = Tipo.T1.copy(fontSize = 13.sp))
                }
            }
        }
        val pasos = when (vinc.rol) {
            Rol.SENTINELA -> listOf(
                "En el celular viejo, abre Sentinel y elige «Sentinela».",
                "Escanea este QR o escribe los 6 números.",
                "Ponle nombre: «Puerta principal», «Ventana cocina»…",
            )
            Rol.RECEPTOR -> listOf(
                "En el otro celular o tablet abre Sentinel y elige «Receptor» → «Unirme con código».",
                "Escanea este QR o escribe los 6 números.",
                "Ponle nombre: «Celular de mamá»…",
            )
            Rol.VECINO -> listOf(
                "Pídele a tu vecino que instale Sentinel (o envíale el enlace por WhatsApp).",
                "Que elija «Me invitaron como vecino de confianza».",
                "Que escriba los 6 números y su nombre.",
            )
        }
        Tarjeta {
            pasos.forEachIndexed { i, p ->
                Row(Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Chip("${i + 1}", Tono.AZUL)
                    Text(p, style = Tipo.Cuerpo.copy(fontSize = 13.5.sp))
                }
            }
        }
        if (vinc.rol == Rol.VECINO) {
            Boton(
                "Enviar por WhatsApp" + (vinc.para?.let { " a ${it.nombre}" } ?: ""),
                {
                    runCatching {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(vm.enlaceWhatsApp(vinc))))
                    }.onFailure { vm.avisar("No se pudo abrir WhatsApp") }
                },
                icono = Iconos.Mensaje,
                estilo = EstiloBoton.VIOLETA,
            )
        }
        if (vinc.unidos.isNotEmpty()) {
            val nombres = vinc.unidos.map { e.equipos[it]?.nombre ?: "equipo" }
            Banner("Se ${if (nombres.size > 1) "unieron" else "unió"}: ${Formato.listaNombres(nombres)}", Iconos.Check, Tono.VERDE)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                Punto(Paleta.Verde)
                Text("Esperando al equipo nuevo…", style = Tipo.T2.copy(fontSize = 13.sp))
            }
        }
        Boton(if (vinc.unidos.isNotEmpty()) "Listo" else "Cancelar", vm::cerrarHoja, estilo = EstiloBoton.SECUNDARIO)
    }
}

// ---------- Detalle de un equipo ----------

@Composable
private fun HojaDetalle(id: String, e: EstadoApp, ui: UiReceptor, ahora: Long, vm: ReceptorViewModel) {
    val eq = e.equipos[id]
    if (eq == null) {
        MarcoHoja("Equipo", alCerrar = vm::cerrarHoja) {
            Text("Este equipo ya no está vinculado.", style = Tipo.Sub)
        }
        return
    }
    val confirmar: @Composable () -> Unit = {
        if (ui.confirmar == "desv:$id") {
            Confirmacion(
                "¿Desvincular «${eq.nombre}»?", "Desvincular", { vm.desvincular(id) }, { vm.confirmar(null) },
                detalle = "Dejará de vigilar y de recibir alertas de este hogar.",
            )
        } else {
            Boton("Desvincular", { vm.confirmar("desv:$id") }, icono = Iconos.Basura, estilo = EstiloBoton.PELIGRO)
        }
    }
    if (eq.rol != Rol.SENTINELA) {
        MarcoHoja(eq.nombre, if (eq.rol == Rol.VECINO) "Vecino de confianza" else "Receptor", vm::cerrarHoja) {
            Tarjeta {
                Text(
                    if (eq.rol == Rol.VECINO) "Recibe solo las alertas de intento de entrada." else "Recibe todas las alertas y puede cambiar los ajustes.",
                    style = Tipo.T2,
                )
                Text(
                    "Última conexión: ${if (eq.demo) "ahora" else Formato.hace(eq.visto, ahora)}",
                    style = Tipo.T2,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            confirmar()
        }
        return
    }
    val aj = e.cfg.ajusteDe(id)
    val (txt, tono) = estadoEquipo(eq, e.cfg, ahora)
    val desconectado = sinConexion(eq, ahora)
    MarcoHoja(eq.nombre, "Sentinela · ${Formato.lugar(eq.lugar)}", vm::cerrarHoja) {
        Tarjeta {
            Row {
                Dato(eq.bat?.let { "$it %" } ?: "—", if (eq.carga) "Cargando" else "Batería", Modifier.weight(1f)) { IconoBateria(eq.bat) }
                Dato(eq.temp?.let { "$it °C" } ?: "N/D", "Temperatura", Modifier.weight(1f)) {
                    Icon(Iconos.Termometro, null, tint = Paleta.Txt, modifier = Modifier.size(18.dp))
                }
                Dato(if (desconectado) "Sin red" else "Bien", if (eq.demo) "ahora" else Formato.hace(eq.visto, ahora), Modifier.weight(1f)) {
                    Icon(if (desconectado) Iconos.SinWifi else Iconos.Wifi, null, tint = Paleta.Txt, modifier = Modifier.size(18.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                Punto(tono.color)
                Text(txt, style = Tipo.T2.copy(fontSize = 13.sp), color = tono.color)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Sensibilidad", style = Tipo.T2.copy(fontWeight = FontWeight.SemiBold))
            Segmentado(
                listOf(Opcion(Nivel.BAJA, "Baja"), Opcion(Nivel.MEDIA, "Media"), Opcion(Nivel.ALTA, "Alta")),
                aj.nivel,
            ) { vm.nivel(id, it) }
            Text(
                if (aj.ajuste > 1) {
                    "Ajustada por tus respuestas: ${Math.round((aj.ajuste - 1) * 100)} % menos sensible a vibraciones leves."
                } else {
                    "Alta detecta golpes suaves; baja ignora más vibraciones."
                },
                style = Tipo.T2,
            )
        }
        Tarjeta(relleno = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IcoCaja(Iconos.Sirena, Tono.ROJO, 34.dp, 17.dp)
                Column(Modifier.weight(1f)) {
                    Text("Sirena en este equipo", style = Tipo.T1)
                    Text("Suena fuerte al detectar un intento", style = Tipo.T2)
                }
                Interruptor(aj.sirena, { vm.sirena(id) }, "Sirena en este equipo")
            }
        }
        Boton("Probar detección", { vm.probar(id) }, icono = Iconos.Vibrar, estilo = EstiloBoton.SECUNDARIO)
        confirmar()
    }
}

// ---------- Ajustes ----------

@Composable
private fun HojaAjustes(e: EstadoApp, ui: UiReceptor, vm: ReceptorViewModel) {
    var nombre by rememberSaveable { mutableStateOf(e.yo.nombre) }
    var casa by rememberSaveable { mutableStateOf(e.hogar?.nombre.orEmpty()) }
    var pin by rememberSaveable { mutableStateOf(e.hogar?.pin.orEmpty()) }
    var relay by rememberSaveable { mutableStateOf(e.relay) }
    MarcoHoja("Ajustes", alCerrar = vm::cerrarHoja) {
        Campo("Nombre de este equipo", nombre, { nombre = it })
        if (e.admin) {
            Campo("Nombre del hogar", casa, { casa = it })
            Campo("PIN de administrador (4 números)", pin, { pin = it.filter(Char::isDigit) }, numerico = true, mono = true, maximo = 4)
        }
        Boton("Probar el sonido de alarma", vm::probarSonido, icono = Iconos.Volumen, estilo = EstiloBoton.SECUNDARIO)
        Campo("Servidor de mensajes", relay, { relay = it.trim() }, mono = true, maximo = 200, teclado = KeyboardType.Uri)
        Text("Las alertas viajan por el servicio gratuito ntfy. La versión final usará un servidor propio.", style = Tipo.T2)
        Boton("Guardar", { vm.guardarAjustes(nombre, casa, pin, relay) })
        Tarjeta {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Iconos.Info, null, tint = Paleta.Txt2, modifier = Modifier.size(16.dp))
                Text(
                    "Sentinel recibe las alertas aunque la app esté cerrada, gracias a un aviso fijo en la barra de notificaciones. " +
                        "Sentinel no usa cámara ni micrófono.",
                    style = Tipo.T2,
                )
            }
        }
        if (ui.confirmar == "salir") {
            Confirmacion("¿Salir del hogar y borrar los datos de este equipo?", "Salir", vm::salir, { vm.confirmar(null) })
        } else {
            Boton(if (e.demo) "Salir de la demostración" else "Salir del hogar", { vm.confirmar("salir") }, estilo = EstiloBoton.PELIGRO)
        }
    }
}

// ---------- Agregar vecino a mano ----------

@Composable
private fun HojaContacto(vm: ReceptorViewModel) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var tel by rememberSaveable { mutableStateOf("") }
    var ref by rememberSaveable { mutableStateOf("") }
    MarcoHoja("Agregar vecino", alCerrar = vm::cerrarHoja) {
        Campo("Nombre", nombre, { nombre = it }, "Doña Marta")
        Campo("Teléfono (para WhatsApp)", tel, { tel = it }, "+505 8888 8888", mono = true, teclado = KeyboardType.Phone, maximo = 20)
        Campo("Referencia", ref, { ref = it }, "Casa de al lado")
        Boton("Agregar", { vm.agregarPendiente(nombre, tel, ref) }, icono = Iconos.AgregarPersona, estilo = EstiloBoton.VIOLETA)
    }
}
