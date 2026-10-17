package ni.uni.nanobots.sentinel.ui.receptor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.ui.comp.Banner
import ni.uni.nanobots.sentinel.ui.comp.Cabecera
import ni.uni.nanobots.sentinel.ui.comp.Chip
import ni.uni.nanobots.sentinel.ui.comp.IconoBoton
import ni.uni.nanobots.sentinel.ui.fabrica
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo
import ni.uni.nanobots.sentinel.ui.tema.Tono

@Composable
fun ReceptorPantalla(vm: ReceptorViewModel = viewModel(factory = fabrica { ReceptorViewModel(it) })) {
    val e by vm.estado.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    val ahora by vm.ahora.collectAsStateWithLifecycle()
    val conectado by vm.conectado.collectAsStateWithLifecycle()

    val hoja = ui.hoja
    if (hoja != null) {
        BackHandler { vm.cerrarHoja() }
        Hojas(hoja, e, ui, ahora, vm)
        return
    }
    if (ui.pestana != Pestana.INICIO) BackHandler { vm.pestana(Pestana.INICIO) }

    val titulo = when (ui.pestana) {
        Pestana.INICIO -> e.hogar?.nombre.orEmpty()
        Pestana.VECINOS -> "Vecinos de confianza"
        else -> ui.pestana.titulo
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).statusBarsPadding()) {
            Cabecera(titulo, pre = if (ui.pestana == Pestana.INICIO) "Hogar" else null) {
                if (e.demo) Chip("DEMO", Tono.AMBAR)
                if (ui.pestana == Pestana.EQUIPOS) IconoBoton(Iconos.Mas, "Vincular equipo", { vm.vincular() }, principal = true)
                IconoBoton(Iconos.Controles, "Ajustes", vm::abrirAjustes)
            }
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (conectado == false && !e.demo) {
                    Banner("Sin conexión con el servicio de alertas. Revisa el internet.", Iconos.SinWifi, Tono.AMBAR)
                }
                when (ui.pestana) {
                    Pestana.INICIO -> PestanaInicio(e, ahora, vm)
                    Pestana.EQUIPOS -> PestanaEquipos(e, ahora, vm)
                    Pestana.HORARIO -> PestanaHorario(e, ahora, vm)
                    Pestana.VECINOS -> PestanaVecinos(e, ahora, vm)
                    Pestana.ALERTAS -> PestanaAlertas(e, ui.filtro, ahora, vm)
                }
            }
        }
        BarraInferior(ui.pestana, nuevasAlertas(e, ahora), vm::pestana)
    }
}

private fun nuevasAlertas(e: EstadoApp, ahora: Long) =
    e.alertas.count { it.clase == ClaseAlerta.INTENTO && !it.resuelta && ahora - it.t < 12 * 3_600_000 }

@Composable
private fun BarraInferior(actual: Pestana, nuevas: Int, alElegir: (Pestana) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Paleta.Nav)) {
        HorizontalDivider(color = Paleta.Borde, thickness = 1.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 12.dp),
        ) {
            Pestana.entries.forEach { p ->
                val icono: ImageVector = when (p) {
                    Pestana.INICIO -> Iconos.Casa
                    Pestana.EQUIPOS -> Iconos.Celular
                    Pestana.HORARIO -> Iconos.Reloj
                    Pestana.VECINOS -> Iconos.Personas
                    Pestana.ALERTAS -> Iconos.Campana
                }
                val activa = p == actual
                val color = if (activa) Paleta.AzulC else Paleta.Txt2
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(role = Role.Tab) { alElegir(p) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box {
                        Box(
                            Modifier
                                .size(56.dp, 30.dp)
                                .clip(CircleShape)
                                .background(if (activa) Tono.AZUL.fondo else Color.Transparent),
                            contentAlignment = Alignment.Center,
                        ) { Icon(icono, p.titulo, tint = color, modifier = Modifier.size(21.dp)) }
                        if (p == Pestana.ALERTAS && nuevas > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = (-3).dp)
                                    .widthIn(min = 18.dp)
                                    .clip(CircleShape)
                                    .background(Paleta.RojoF)
                                    .padding(horizontal = 5.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("$nuevas", style = Tipo.Chip.copy(fontSize = 10.5.sp), color = Color.White)
                            }
                        }
                    }
                    Text(
                        p.titulo,
                        style = Tipo.Cuerpo.copy(fontSize = 11.sp, fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium),
                        color = color,
                    )
                }
            }
        }
    }
}
