package ni.uni.nanobots.sentinel.ui.inicio

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import ni.uni.nanobots.sentinel.R
import ni.uni.nanobots.sentinel.domain.hogar.EstadoUnion
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.ui.comp.Banner
import ni.uni.nanobots.sentinel.ui.comp.Boton
import ni.uni.nanobots.sentinel.ui.comp.Cabecera
import ni.uni.nanobots.sentinel.ui.comp.Campo
import ni.uni.nanobots.sentinel.ui.comp.Chip
import ni.uni.nanobots.sentinel.ui.comp.EstiloBoton
import ni.uni.nanobots.sentinel.ui.comp.IcoCaja
import ni.uni.nanobots.sentinel.ui.comp.Opcion
import ni.uni.nanobots.sentinel.ui.comp.RadioTarjeta
import ni.uni.nanobots.sentinel.ui.comp.Segmentado
import ni.uni.nanobots.sentinel.ui.fabrica
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo
import ni.uni.nanobots.sentinel.ui.tema.Tono

@Composable
fun InicioPantalla(vm: InicioViewModel = viewModel(factory = fabrica { InicioViewModel(it) })) {
    val f by vm.form.collectAsStateWithLifecycle()
    val union by vm.union.collectAsStateWithLifecycle()
    if (f.enFormulario) {
        BackHandler { vm.volver() }
        Unir(f, union, vm)
    } else {
        Bienvenida(f.rol, vm)
    }
}

/** Esqueleto común: contenido con scroll y botones fijos abajo. */
@Composable
private fun Marco(
    cabecera: (@Composable () -> Unit)? = null,
    abajo: @Composable ColumnScope.() -> Unit,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        cabecera?.invoke()
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = contenido,
        )
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = abajo,
        )
    }
}

@Composable
private fun Bienvenida(rol: Rol, vm: InicioViewModel) {
    Marco(
        abajo = {
            Boton(
                "Configurar como ${if (rol == Rol.RECEPTOR) "Receptor" else "Centinela"}",
                vm::continuar,
                icono = Iconos.Adelante,
            )
            Boton("Ver demostración con datos de ejemplo", vm::demo, icono = Iconos.Play, estilo = EstiloBoton.SECUNDARIO)
            Text(
                "Equipo Nanobots · Rally Latinoamericano de Innovación 2026",
                style = Tipo.T2.copy(fontSize = 12.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.logo_centinel), contentDescription = null, modifier = Modifier.size(52.dp))
            Column {
                Text("Centinel", style = Tipo.Titulo.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp))
                Text("Tu celular viejo cuida tu casa", style = Tipo.T2)
            }
        }
        Column(Modifier.padding(top = 8.dp)) {
            Text("¿Qué será este dispositivo?", style = Tipo.Titulo)
            Text("Elige una opción. Puedes cambiarla después.", style = Tipo.Sub, modifier = Modifier.padding(top = 6.dp))
        }
        TarjetaRol(
            Tono.AZUL, Iconos.EscudoOk, "EMISOR", "Centinela",
            "El celular viejo que va en el soporte de la puerta o ventana. Detecta golpes y aperturas con el acelerómetro y el giroscopio.",
            rol == Rol.SENTINELA,
        ) { vm.elegirRol(Rol.SENTINELA) }
        TarjetaRol(
            Tono.CIAN, Iconos.Campana, "RECEPTOR", "Receptor",
            "Recibe las alertas: tu celular, la tablet o la computadora de la casa.",
            rol == Rol.RECEPTOR,
        ) { vm.elegirRol(Rol.RECEPTOR) }
        Row(
            Modifier.clip(RoundedCornerShape(8.dp)).clickable { vm.elegirRol(Rol.VECINO) }.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Iconos.Personas, null, tint = Paleta.Violeta, modifier = Modifier.size(18.dp))
            Text("Me invitaron como vecino de confianza", style = Tipo.T1.copy(fontSize = 14.sp), color = Paleta.Violeta)
        }
    }
}

@Composable
private fun TarjetaRol(
    tono: Tono,
    icono: ImageVector,
    chip: String,
    titulo: String,
    texto: String,
    elegido: Boolean,
    alTocar: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RadioTarjeta)
            .background(if (elegido) tono.fondo else Paleta.Sup)
            .border(1.5.dp, if (elegido) tono.color else Paleta.Borde, RadioTarjeta)
            .clickable(role = Role.RadioButton, onClick = alTocar)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IcoCaja(icono, tono, 46.dp, 24.dp)
        Column(Modifier.weight(1f)) {
            Chip(chip, tono)
            Text(titulo, style = Tipo.Titulo.copy(fontSize = 18.sp), modifier = Modifier.padding(top = 6.dp))
            Text(texto, style = Tipo.T2.copy(fontSize = 13.sp))
        }
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (elegido) tono.color else Color.Transparent)
                .border(2.dp, if (elegido) tono.color else Paleta.Borde, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (elegido) Icon(Iconos.Check, null, tint = Paleta.Noche, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun Unir(f: FormInicio, union: EstadoUnion, vm: InicioViewModel) {
    val ctx = LocalContext.current
    val esperando = union is EstadoUnion.Esperando
    val error = (union as? EstadoUnion.Error)?.mensaje ?: f.error

    fun escanear() {
        val opciones = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        GmsBarcodeScanning.getClient(ctx, opciones).startScan()
            .addOnSuccessListener { if (!vm.codigoEscaneado(it.rawValue)) vm.avisar("Ese QR no es de Centinel. Escribe el código de 6 números.") }
            .addOnFailureListener { vm.avisar("No se pudo abrir el lector de QR. Escribe el código de 6 números.") }
    }

    val titulo = when (f.rol) {
        Rol.SENTINELA -> "Vincular este Centinela"
        Rol.VECINO -> "Soy vecino de confianza"
        Rol.RECEPTOR -> "Configurar Receptor"
    }
    val crear = f.rol == Rol.RECEPTOR && f.modoReceptor == ModoReceptor.CREAR

    Marco(
        cabecera = { Cabecera(titulo, alVolver = vm::volver) },
        abajo = {
            if (error != null) Text(error, style = Tipo.Cuerpo.copy(fontSize = 13.sp), color = Paleta.Rojo)
            if (esperando) {
                Banner("Conectando con el hogar… deja abierta la pantalla «Vincular» en el celular de la familia.", Iconos.Wifi, Tono.AZUL)
            }
            when {
                crear -> Boton("Crear hogar", vm::crearHogar, icono = Iconos.Casa)
                f.rol == Rol.SENTINELA -> Boton("Unir al hogar", vm::unirse, icono = Iconos.EscudoOk, habilitado = !esperando)
                f.rol == Rol.VECINO -> Boton("Unirme como vecino", vm::unirse, icono = Iconos.Personas, estilo = EstiloBoton.VIOLETA, habilitado = !esperando)
                else -> Boton("Unirme al hogar", vm::unirse, icono = Iconos.Campana, habilitado = !esperando)
            }
        },
    ) {
        if (f.rol == Rol.RECEPTOR) {
            Segmentado(
                listOf(Opcion(ModoReceptor.CREAR, "Crear mi hogar", Iconos.Casa), Opcion(ModoReceptor.UNIR, "Unirme con código", Iconos.Qr)),
                f.modoReceptor,
                vm::modoReceptor,
            )
        }
        if (crear) {
            Text(
                buildAnnotatedString {
                    append("Este celular será el ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Paleta.Txt)) { append("administrador") }
                    append(": vincula los Centinelas, los demás receptores y los vecinos.")
                },
                style = Tipo.Sub,
            )
            Campo("Nombre del hogar", f.casa, vm::casa, "Mi casa")
            Campo("Nombre de este equipo", f.nombre, vm::nombre, "Mi celular")
            Campo("PIN de 4 números (protege los ajustes de los Centinelas)", f.pin, vm::pin, "0000", codigo = true, maximo = 4)
            return@Marco
        }
        Text(
            when (f.rol) {
                Rol.SENTINELA -> "En el celular de la familia abre Centinel → Equipos → +. Escanea ese QR o escribe el código."
                Rol.VECINO -> "Tu vecino te comparte un código desde su app (Vecinos → Invitar vecino). Recibirás una alerta si alguien intenta entrar a su casa."
                Rol.RECEPTOR -> "Para agregar otro celular, tablet o computadora de la familia. Pide el código en el celular administrador (Equipos → +)."
            },
            style = Tipo.Sub,
        )
        Campo("Código de vinculación", f.codigo, vm::codigo, "000000", codigo = true, maximo = 6)
        Boton("Escanear QR", { escanear() }, icono = Iconos.Escanear, estilo = EstiloBoton.SECUNDARIO, chico = true)
        when (f.rol) {
            Rol.SENTINELA -> {
                Campo("Nombre de este Centinela", f.nombre, vm::nombre, "Ventana cocina")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("¿Dónde va el soporte?", style = Tipo.T2.copy(fontWeight = FontWeight.SemiBold))
                    Segmentado(
                        listOf(Opcion(Lugar.PUERTA, "Puerta", Iconos.Puerta), Opcion(Lugar.VENTANA, "Ventana", Iconos.Ventana)),
                        f.lugar,
                        vm::lugar,
                    )
                }
            }
            Rol.VECINO -> {
                Campo("Tu nombre", f.nombre, vm::nombre, "Doña Marta")
                Campo("Referencia (opcional)", f.ref, vm::ref, "Casa de al lado")
            }
            Rol.RECEPTOR -> Campo("Nombre de este equipo", f.nombre, vm::nombre, "Celular de mamá")
        }
    }
}
