package ni.uni.nanobots.sentinel.domain.hogar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.comun.Ids
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Comando
import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Contenido
import ni.uni.nanobots.sentinel.domain.modelo.Equipo
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.Hogar
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.MAX_ALERTAS
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Nivel
import ni.uni.nanobots.sentinel.domain.modelo.Pendiente
import ni.uni.nanobots.sentinel.domain.modelo.Perfil
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Temas
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import ni.uni.nanobots.sentinel.domain.puertos.Efectos
import ni.uni.nanobots.sentinel.domain.puertos.Mensajeria
import ni.uni.nanobots.sentinel.domain.puertos.Reloj
import ni.uni.nanobots.sentinel.domain.reglas.Horario
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Sesión del hogar: escucha el canal, responde a cada tipo de mensaje, manda los
 * latidos y aplica las acciones del usuario. Es el mismo protocolo del prototipo web,
 * así que la app y la página se entienden entre sí.
 */
class Coordinador(
    private val repo: RepositorioEstado,
    private val red: Mensajeria,
    private val efectos: Efectos,
    private val reloj: Reloj,
    private val scope: CoroutineScope,
) {
    private val e: EstadoApp get() = repo.estado.value

    val conectado: StateFlow<Boolean?> = red.conectado

    private val _avisos = MutableSharedFlow<Aviso>(extraBufferCapacity = 16)
    val avisos: SharedFlow<Aviso> = _avisos.asSharedFlow()

    private val _alarma = MutableStateFlow<Alerta?>(null)
    val alarma: StateFlow<Alerta?> = _alarma.asStateFlow()

    private val _pruebaHasta = MutableStateFlow(0L)
    val pruebaHasta: StateFlow<Long> = _pruebaHasta.asStateFlow()

    private val _vigilancia = MutableStateFlow(Vigilancia.REPOSO)
    val vigilancia: StateFlow<Vigilancia> = _vigilancia.asStateFlow()

    private val _local = MutableStateFlow(DatosLocales())
    val local: StateFlow<DatosLocales> = _local.asStateFlow()

    private val vistos = VistosRecientes(1_000)
    private val respondidos = ConcurrentHashMap<String, Long>()
    private var sesion: Job? = null

    @Volatile
    private var ultimaVigEnviada: Vigilancia? = null

    val activo: Boolean get() = sesion?.isActive == true

    // ---------- sesión ----------

    fun arrancar() {
        sesion?.cancel()
        val inicial = e
        val hogar = inicial.hogar ?: return
        val esSentinela = inicial.rol == Rol.SENTINELA
        ultimaVigEnviada = null
        _vigilancia.value = Horario.estado(inicial.cfg, reloj.ahora())
        sesion = scope.launch {
            if (!inicial.demo) {
                launch {
                    escuchar(Temas.hogar(hogar.id), if (esSentinela) null else "3m").collect { alMensaje(it) }
                }
                withTimeoutOrNull(4_000) { red.conectado.first { it == true } }
                enviarHogar(Contenido.Hola(inicial.rol, e.cfg.v))
                latido()
                launch {
                    while (isActive) {
                        delay(if (esSentinela) LATIDO_SENTINELA_MS else LATIDO_RECEPTOR_MS)
                        latido()
                    }
                }
            }
            ciclo()
        }
    }

    fun asegurarSesion() {
        if (!activo) arrancar()
    }

    fun detener() {
        sesion?.cancel()
        sesion = null
    }

    /** Revisión cada segundo: estado de vigilancia y, en los receptores, avisos cada 15 s. */
    private suspend fun ciclo() {
        var n = 1
        while (currentCoroutineContext().isActive) {
            val est = e
            val v = Horario.estado(est.cfg, reloj.ahora())
            _vigilancia.value = v
            if (est.rol == Rol.SENTINELA && v != ultimaVigEnviada) {
                ultimaVigEnviada = v
                enviarEstado()
            }
            if (est.rol != Rol.SENTINELA && n % 15 == 0) revisiones()
            n++
            delay(1_000)
        }
    }

    // ---------- envío y recepción ----------

    internal fun enviar(tema: String, c: Contenido): Mensaje {
        val est = e
        val m = Mensaje(Ids.nuevo(10), est.yo.id, reloj.ahora(), c)
        vistos.agregar(m.mid)
        if (!est.demo) scope.launch { red.enviar(tema, m) }
        return m
    }

    private fun enviarHogar(c: Contenido): Mensaje? = e.hogar?.let { enviar(Temas.hogar(it.id), c) }

    /** Mensajes de otros equipos, sin repetidos. */
    internal fun escuchar(tema: String, desde: String?): Flow<Mensaje> =
        red.escuchar(tema, desde).filter { it.de != e.yo.id && vistos.agregar(it.mid) }

    fun avisar(texto: String, tipo: TipoAviso = TipoAviso.INFO, notificar: Boolean = false) {
        _avisos.tryEmit(Aviso(texto, tipo))
        if (notificar) efectos.notificar(texto)
    }

    private fun latido() {
        val est = e
        if (est.hogar == null || est.demo) return
        if (est.rol == Rol.SENTINELA) {
            enviarEstado()
        } else {
            val rol = est.rol ?: return
            enviarHogar(Contenido.Presencia(rol, est.yo.nombre, est.yo.ref, est.admin, est.cfg.v))
        }
    }

    fun enviarEstado() {
        val est = e
        if (est.hogar == null || est.rol != Rol.SENTINELA) return
        val l = _local.value
        enviarHogar(
            Contenido.Estado(
                nombre = est.yo.nombre,
                lugar = est.yo.lugar,
                vig = Horario.estado(est.cfg, reloj.ahora()),
                bat = l.bat,
                carga = l.carga,
                sensoresOk = l.sensoresOk,
                cfgv = est.cfg.v,
                temp = l.temp,
            ),
        )
    }

    internal fun alMensaje(m: Mensaje) {
        val viejo = reloj.ahora() - m.t > ORDEN_VIEJA_MS
        when (val c = m.contenido) {
            is Contenido.Estado -> if (e.rol != Rol.SENTINELA) {
                actualizarEquipo(
                    m, Rol.SENTINELA, c.nombre, lugar = c.lugar, ref = null, admin = null, vig = c.vig,
                    bat = c.bat, carga = c.carga, sensoresOk = c.sensoresOk, temp = c.temp, cfgv = c.cfgv,
                )
            }
            is Contenido.Presencia -> if (e.rol != Rol.SENTINELA) {
                actualizarEquipo(
                    m, c.rol, c.nombre, lugar = null, ref = c.ref, admin = c.admin, vig = null,
                    bat = null, carga = false, sensoresOk = true, temp = null, cfgv = c.cfgv,
                )
            }
            is Contenido.Hola -> {
                scope.launch {
                    delay(300 + Random.nextLong(1_500))
                    latido()
                }
                if (e.rol == Rol.RECEPTOR && c.cfgv < e.cfg.v) enviarCfgA(m.de)
            }
            is Contenido.Cfg -> if (c.cfg.v > e.cfg.v) {
                repo.actualizar { it.copy(cfg = c.cfg) }
                alCambiarCfg()
            }
            is Contenido.Evento -> alEvento(m, c, viejo)
            is Contenido.Orden -> if (!viejo) alComando(c)
            is Contenido.Marca -> repo.actualizar { est ->
                est.copy(alertas = est.alertas.map { if (it.mid == c.alerta) it.copy(que = c.que) else it })
            }
            is Contenido.Solicitud, is Contenido.Bienvenida -> Unit // solo en el canal de vinculación
        }
    }

    private fun enviarCfgA(id: String) {
        val v = e.cfg.v
        if (respondidos[id] == v) return
        respondidos[id] = v
        scope.launch {
            delay(Random.nextLong(1_200))
            enviarHogar(Contenido.Cfg(e.cfg))
        }
    }

    private fun actualizarEquipo(
        m: Mensaje,
        rol: Rol,
        nombre: String,
        lugar: Lugar?,
        ref: String?,
        admin: Boolean?,
        vig: Vigilancia?,
        bat: Int?,
        carga: Boolean,
        sensoresOk: Boolean,
        temp: Int?,
        cfgv: Long,
    ) {
        var volvio = false
        var nuevoNombre = nombre
        repo.actualizar { est ->
            val prev = est.equipos[m.de]
            volvio = prev?.offAvisado == true
            nuevoNombre = nombre.ifBlank { prev?.nombre ?: "Equipo" }
            val eq = Equipo(
                id = m.de,
                rol = rol,
                nombre = nuevoNombre,
                lugar = lugar ?: prev?.lugar ?: Lugar.PUERTA,
                ref = ref ?: prev?.ref.orEmpty(),
                admin = admin ?: prev?.admin ?: false,
                vig = vig ?: prev?.vig,
                bat = bat ?: if (rol == Rol.SENTINELA) null else prev?.bat,
                carga = carga,
                sensoresOk = sensoresOk,
                temp = temp ?: if (rol == Rol.SENTINELA) null else prev?.temp,
                visto = maxOf(m.t, prev?.visto ?: 0),
                offAvisado = false,
            )
            est.copy(equipos = est.equipos + (m.de to eq))
        }
        if (e.rol == Rol.RECEPTOR) {
            if (volvio) avisar("$nuevoNombre volvió a conectarse", TipoAviso.SIN_RED)
            if (rol == Rol.SENTINELA && cfgv < e.cfg.v) enviarCfgA(m.de)
        }
    }

    private fun alEvento(m: Mensaje, c: Contenido.Evento, viejo: Boolean) {
        val est = e
        if (est.rol == Rol.SENTINELA) return
        val nombre = c.equipoNombre.ifBlank { "Centinela" }
        val a = Alerta(m.mid, c.clase, m.de, nombre, m.t, c.detalle)
        if (c.clase == ClaseAlerta.PRUEBA) {
            if (est.rol == Rol.RECEPTOR && !viejo) {
                avisar("$nombre: ${a.detalle}", TipoAviso.CHECK)
                efectos.bip(1200, 100, 0.2f)
            }
            return
        }
        if (est.rol == Rol.VECINO && c.clase != ClaseAlerta.INTENTO) return
        if (!registrarAlerta(a) || viejo) return
        when (c.clase) {
            ClaseAlerta.INTENTO -> {
                if (est.rol == Rol.VECINO && est.yo.id in est.cfg.vecinosOff) return
                mostrarAlarma(a)
            }
            ClaseAlerta.LEVE -> {
                avisar("Vibración leve en $nombre", TipoAviso.VIBRACION, notificar = true)
                efectos.bip(660, 150, 0.2f)
                efectos.vibrar(120)
            }
            else -> avisar("$nombre: ${a.detalle}", TipoAviso.ALERTA, notificar = true)
        }
    }

    private fun alComando(c: Contenido.Orden) {
        val est = e
        if (c.para != null && c.para != est.yo.id) return
        val esSentinela = est.rol == Rol.SENTINELA
        when (c.cmd) {
            Comando.SIRENA -> if (esSentinela) {
                efectos.sirena(20)
                avisar("Sirena activada desde ${c.desde.ifBlank { "un receptor" }}", TipoAviso.SIRENA)
            }
            Comando.CALLAR -> efectos.callar()
            Comando.PROBAR -> if (esSentinela) {
                _pruebaHasta.value = reloj.ahora() + PRUEBA_MS
                efectos.bip(990, 120, 0.25f)
                avisar("Prueba: golpea o mueve la ${Formato.lugar(est.yo.lugar)}", TipoAviso.VIBRACION)
                scope.launch {
                    delay(200)
                    val detalle = if (_local.value.sensoresOk) {
                        "Recibió la orden. Golpéalo o muévelo para probar."
                    } else {
                        "Recibió la orden, pero no tiene datos de sensores"
                    }
                    enviarHogar(Contenido.Evento(ClaseAlerta.PRUEBA, detalle, null, est.yo.id, est.yo.nombre))
                }
            }
            Comando.RESUELTA -> {
                repo.actualizar { s ->
                    s.copy(alertas = s.alertas.map {
                        if (it.mid == c.alerta) it.copy(que = if (c.falsa) Etiqueta.FALSA else it.que, resuelta = true) else it
                    })
                }
                if (_alarma.value?.mid == c.alerta) cerrarAlarma()
                if (esSentinela) efectos.callar()
                val quien = c.desde.ifBlank { "La familia" }
                avisar(quien + if (c.falsa) " marcó la alerta como falsa alarma" else " atendió la alerta", TipoAviso.CHECK)
            }
            Comando.DESVINCULAR -> if (c.para == est.yo.id) {
                salir()
                avisar("Este equipo fue desvinculado del hogar")
            }
        }
    }

    private fun registrarAlerta(a: Alerta): Boolean {
        var nueva = false
        repo.actualizar { est ->
            if (est.alertas.any { it.mid == a.mid }) {
                est
            } else {
                nueva = true
                est.copy(alertas = (listOf(a) + est.alertas).take(MAX_ALERTAS))
            }
        }
        return nueva
    }

    /** Avisos de los receptores: Centinela sin conexión, batería baja y hora de cargar. */
    private fun revisiones() {
        val est = e
        val t = reloj.ahora()
        if (est.rol == Rol.RECEPTOR) {
            for (s in est.sentinelas) {
                if (s.demo) continue
                if (t - s.visto > SIN_CONEXION_MS && !s.offAvisado) {
                    repo.actualizar { it.copy(equipos = it.equipos + (s.id to s.copy(offAvisado = true))) }
                    registrarAlerta(
                        Alerta("off${s.id}${t / 60_000}", ClaseAlerta.CONEXION, s.id, s.nombre, t, "Dejó de reportarse hace más de 2 min"),
                    )
                    avisar("${s.nombre} está sin conexión", TipoAviso.SIN_RED, notificar = true)
                }
                val clave = "bat${s.id}"
                if (s.bat != null && s.bat <= 20 && !s.carga && t - (est.avisos[clave] ?: 0) > AVISO_BATERIA_MS) {
                    repo.actualizar { it.copy(avisos = it.avisos + (clave to t)) }
                    registrarAlerta(
                        Alerta("bat${s.id}${t / 60_000}", ClaseAlerta.BATERIA, s.id, s.nombre, t, "Batería baja (${s.bat} %)"),
                    )
                    avisar("Carga ${s.nombre}: ${s.bat} %", TipoAviso.BATERIA, notificar = true)
                }
            }
            val c = est.cfg
            if (c.carga && c.aviso10) {
                val ahora = java.time.Instant.ofEpochMilli(t).atZone(java.time.ZoneId.systemDefault())
                val m = ahora.hour * 60 + ahora.minute
                val clave = "carga" + ahora.toLocalDate()
                if (m == (Formato.aMin(c.cargaIni) - 10 + 1440) % 1440 && clave !in est.avisos) {
                    repo.actualizar { it.copy(avisos = it.avisos + (clave to t)) }
                    avisar(
                        "En 10 min: hora de cargar los Centinelas (${Formato.fmtMin(Formato.aMin(c.cargaIni))})",
                        TipoAviso.RAYO,
                        notificar = true,
                    )
                    efectos.bip(880, 200, 0.25f)
                }
            }
        }
    }

    // ---------- Centinela ----------

    /** El Centinela detectó algo: avisa al hogar y reacciona en el propio equipo. */
    fun emitirEvento(clase: ClaseAlerta, detalle: String, pico: Double?) {
        val est = e
        if (est.hogar == null) return
        val p = pico?.takeIf { it != 0.0 }?.let(Formato::redondear2)
        val m = enviarHogar(Contenido.Evento(clase, detalle, p, est.yo.id, est.yo.nombre)) ?: return
        if (clase != ClaseAlerta.PRUEBA) registrarAlerta(Alerta(m.mid, clase, est.yo.id, est.yo.nombre, m.t, detalle))
        when (clase) {
            ClaseAlerta.INTENTO -> {
                if (est.cfg.ajusteDe(est.yo.id).sirena) efectos.sirena(15)
                efectos.vibrar(300, 100, 300)
            }
            ClaseAlerta.PRUEBA -> {
                efectos.bip(1200, 120, 0.25f)
                scope.launch {
                    delay(160)
                    efectos.bip(1500, 120, 0.25f)
                }
                avisar("Detectado: $detalle", TipoAviso.CHECK)
            }
            else -> efectos.bip(660, 120, 0.15f)
        }
    }

    fun sensoresConDatos() {
        if (!_local.value.sensoresOk) _local.update { it.copy(sensoresOk = true) }
    }

    /** Batería y temperatura de este equipo. Avisa si se calienta demasiado. */
    fun actualizarBateria(nivel: Int?, carga: Boolean, temp: Int?) {
        _local.update { it.copy(bat = nivel, carga = carga, temp = temp) }
        val est = e
        if (est.rol != Rol.SENTINELA || temp == null) return
        val t = reloj.ahora()
        if (est.cfg.avisoTemp && temp > est.cfg.tempMax && t - (est.avisos[CLAVE_CALOR] ?: 0) > AVISO_CALOR_MS) {
            repo.actualizar { it.copy(avisos = it.avisos + (CLAVE_CALOR to t)) }
            emitirEvento(ClaseAlerta.CALOR, "Muy caliente · $temp °C. Desconecta el cargador", null)
        }
    }

    fun probarLocal() {
        _pruebaHasta.value = reloj.ahora() + PRUEBA_MS
        avisar("Prueba por 15 s: golpea o mueve la ${Formato.lugar(e.yo.lugar)}", TipoAviso.VIBRACION)
    }

    fun guardarCalibracion(cal: ni.uni.nanobots.sentinel.domain.modelo.Calibracion) {
        repo.actualizar { it.copy(cal = cal) }
        enviarEstado()
    }

    fun recalibrar() {
        repo.actualizar { it.copy(cal = null) }
    }

    fun cambiarLugar(lugar: Lugar) {
        repo.actualizar { it.copy(yo = it.yo.copy(lugar = lugar)) }
        enviarEstado()
    }

    fun pinCorrecto(pin: String): Boolean = e.hogar?.pin?.let { it.isNotEmpty() && it == pin } == true

    // ---------- configuración compartida ----------

    fun cambiarCfg(cambio: (Config) -> Config) {
        val t = reloj.ahora()
        repo.actualizar { est ->
            val nueva = cambio(est.cfg)
            est.copy(cfg = nueva.copy(v = maxOf(t, est.cfg.v + 1)))
        }
        enviarHogar(Contenido.Cfg(e.cfg))
        alCambiarCfg()
    }

    private fun alCambiarCfg() {
        val v = Horario.estado(e.cfg, reloj.ahora())
        _vigilancia.value = v
        if (e.rol == Rol.SENTINELA && v != ultimaVigEnviada) {
            ultimaVigEnviada = v
            enviarEstado()
        }
    }

    fun pausar() {
        cambiarCfg { it.copy(pausaHasta = reloj.ahora() + PAUSA_MS) }
        avisar("Vigilancia en pausa por 10 minutos", TipoAviso.PAUSA)
    }

    fun reanudar() = cambiarCfg { it.copy(pausaHasta = 0) }

    fun detenerVigilancia() {
        cambiarCfg { it.copy(modo = Modo.OFF, modoHasta = Horario.proximoInicio(it, reloj.ahora()), pausaHasta = 0) }
        avisar("Vigilancia detenida")
    }

    fun activarVigilancia() {
        cambiarCfg { it.copy(modo = Modo.ON, modoHasta = 0, pausaHasta = 0) }
        avisar("Vigilancia activada", TipoAviso.ESCUDO)
    }

    fun volverAlHorario() = cambiarCfg { it.copy(modo = Modo.AUTO, modoHasta = 0) }

    fun cambiarNivel(id: String, nivel: Nivel) =
        cambiarCfg { it.copy(equipos = it.equipos + (id to it.ajusteDe(id).copy(nivel = nivel))) }

    fun alternarSirena(id: String) =
        cambiarCfg { c -> c.ajusteDe(id).let { a -> c.copy(equipos = c.equipos + (id to a.copy(sirena = !a.sirena))) } }

    fun alternarVecino(id: String) =
        cambiarCfg { it.copy(vecinosOff = if (id in it.vecinosOff) it.vecinosOff - id else it.vecinosOff + id) }

    // ---------- receptor ----------

    fun etiquetar(mid: String, que: Etiqueta) {
        val a = e.alertas.firstOrNull { it.mid == mid } ?: return
        repo.actualizar { est -> est.copy(alertas = est.alertas.map { if (it.mid == mid) it.copy(que = que) else it }) }
        enviarHogar(Contenido.Marca(mid, que))
        if (que.ajustaSensibilidad) {
            cambiarCfg { c ->
                val aj = c.ajusteDe(a.equipoId)
                val nuevo = Formato.redondear2((aj.ajuste * 1.15).coerceIn(1.0, 2.5))
                c.copy(equipos = c.equipos + (a.equipoId to aj.copy(ajuste = nuevo)))
            }
            val causa = when (que) {
                Etiqueta.LLUVIA -> "la lluvia"
                Etiqueta.VIENTO -> "el viento"
                else -> "los animales"
            }
            avisar("Listo: ${a.nombre} será un poco menos sensible a $causa", TipoAviso.CHECK)
        }
    }

    fun probarEquipo(id: String) {
        if (e.demo) return avisar("En la demostración no hay equipos reales")
        enviarHogar(Contenido.Orden(Comando.PROBAR, para = id, desde = e.yo.nombre))
        avisar("Orden de prueba enviada", TipoAviso.VIBRACION)
    }

    fun desvincularEquipo(id: String) {
        enviarHogar(Contenido.Orden(Comando.DESVINCULAR, para = id))
        repo.actualizar { it.copy(equipos = it.equipos - id, cfg = it.cfg.copy(equipos = it.cfg.equipos - id)) }
        avisar("Equipo desvinculado")
    }

    fun agregarPendiente(nombre: String, tel: String, ref: String) {
        repo.actualizar { it.copy(pendientes = it.pendientes + Pendiente(Ids.nuevo(6), nombre, tel, ref)) }
        avisar("Vecino agregado. Envíale la invitación por WhatsApp.", TipoAviso.USUARIO)
    }

    fun quitarPendiente(id: String) = repo.actualizar { est -> est.copy(pendientes = est.pendientes.filterNot { it.id == id }) }

    fun guardarAjustes(nombre: String, casa: String, pin: String, relay: String) {
        val antes = e
        val pinLimpio = pin.filter(Char::isDigit)
        val relayNuevo = relay.trim().takeIf { Regex("^https?://\\S+$").matches(it) && it != antes.relay }
        repo.actualizar { est ->
            var s = est
            if (nombre.isNotBlank()) s = s.copy(yo = s.yo.copy(nombre = nombre.trim()))
            if (s.admin && s.hogar != null) {
                var h = s.hogar!!
                if (casa.isNotBlank()) h = h.copy(nombre = casa.trim())
                if (pinLimpio.length == 4) h = h.copy(pin = pinLimpio)
                s = s.copy(hogar = h)
            }
            if (relayNuevo != null) s = s.copy(relay = relayNuevo)
            s
        }
        if (relayNuevo != null) arrancar() else latido()
        avisar("Ajustes guardados", TipoAviso.CHECK)
    }

    // ---------- alarma ----------

    private fun mostrarAlarma(a: Alerta) {
        _alarma.value = a
        efectos.mostrarAlarma(a)
    }

    fun cerrarAlarma() {
        efectos.cerrarAlarma()
        _alarma.value = null
    }

    fun sonarSirenaRemota() {
        val a = _alarma.value
        enviarHogar(Contenido.Orden(Comando.SIRENA, para = a?.equipoId, desde = e.yo.nombre))
        avisar("Sirena activada en ${a?.nombre ?: "el Centinela"}", TipoAviso.SIRENA)
    }

    fun marcarFalsaAlarma() {
        val a = _alarma.value ?: return
        repo.actualizar { est ->
            est.copy(alertas = est.alertas.map { if (it.mid == a.mid) it.copy(que = Etiqueta.FALSA, resuelta = true) else it })
        }
        enviarHogar(Contenido.Orden(Comando.RESUELTA, alerta = a.mid, falsa = true, desde = e.yo.nombre))
        cerrarAlarma()
        avisar("Avisamos a todos que fue falsa alarma", TipoAviso.CHECK)
    }

    fun simularIntento() {
        val s = e.sentinelas.firstOrNull()
        val a = Alerta(
            Ids.nuevo(10), ClaseAlerta.INTENTO, s?.id ?: "x", s?.nombre ?: "Puerta principal",
            reloj.ahora(), "Golpes fuertes y apertura",
        )
        registrarAlerta(a)
        mostrarAlarma(a)
    }

    // ---------- salir y demostración ----------

    /** Borra los datos de este equipo (conserva el servidor de mensajes). */
    fun salir() {
        detener()
        efectos.callar()
        cerrarAlarma()
        _pruebaHasta.value = 0
        repo.reemplazar(EstadoApp(relay = e.relay))
    }

    fun cargarDemo() {
        detener()
        val t = reloj.ahora()
        fun eq(id: String, rol: Rol, nombre: String, lugar: Lugar = Lugar.PUERTA, bat: Int? = null, ref: String = "") =
            Equipo(
                id = id, rol = rol, nombre = nombre, lugar = lugar, ref = ref, bat = bat, visto = t, demo = true,
                vig = if (rol == Rol.SENTINELA) Vigilancia.VIGILANDO else null,
                temp = if (rol == Rol.SENTINELA) 31 else null,
            )
        val equipos = listOf(
            eq("d1", Rol.SENTINELA, "Puerta principal", Lugar.PUERTA, 82),
            eq("d2", Rol.SENTINELA, "Ventana cocina", Lugar.VENTANA, 64),
            eq("d3", Rol.SENTINELA, "Ventana cuarto", Lugar.VENTANA, 28),
            eq("r1", Rol.RECEPTOR, "Celular de mamá"),
            eq("r2", Rol.RECEPTOR, "Computadora de la sala"),
            eq("v1", Rol.VECINO, "Doña Marta", ref = "Casa de al lado"),
            eq("v2", Rol.VECINO, "Don José", ref = "Enfrente"),
        ).associateBy { it.id }
        repo.reemplazar(
            EstadoApp(
                yo = Perfil(Ids.nuevo(8), "Mi celular", Rol.RECEPTOR),
                hogar = Hogar("demo" + Ids.nuevo(6), "Mi casa", "1234"),
                admin = true,
                cfg = Config(modo = Modo.ON),
                equipos = equipos,
                pendientes = listOf(Pendiente("p1", "Karla")),
                alertas = listOf(
                    Alerta("a1", ClaseAlerta.LEVE, "d2", "Ventana cocina", t - 50 * 60_000, "Vibración rara en la ventana", que = Etiqueta.LLUVIA),
                    Alerta("a2", ClaseAlerta.BATERIA, "d3", "Ventana cuarto", t - 26 * 3_600_000, "Batería baja (28 %)"),
                    Alerta("a3", ClaseAlerta.CONEXION, "d3", "Ventana cuarto", t - 26 * 3_600_000 - 1_800_000, "Sin conexión por 3 min"),
                ),
                relay = e.relay,
                demo = true,
            ),
        )
        arrancar()
        avisar("Demostración con datos de ejemplo", TipoAviso.PLAY)
    }

    private companion object {
        const val LATIDO_SENTINELA_MS = 60_000L
        const val LATIDO_RECEPTOR_MS = 120_000L
        const val ORDEN_VIEJA_MS = 90_000L
        const val SIN_CONEXION_MS = 150_000L
        const val AVISO_BATERIA_MS = 30 * 60_000L
        const val AVISO_CALOR_MS = 30 * 60_000L
        const val PRUEBA_MS = 15_000L
        const val PAUSA_MS = 10 * 60_000L
        const val CLAVE_CALOR = "calor"
    }
}
