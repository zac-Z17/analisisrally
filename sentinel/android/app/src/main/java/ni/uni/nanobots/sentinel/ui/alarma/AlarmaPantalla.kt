package ni.uni.nanobots.sentinel.ui.alarma

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.di.Contenedor
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.ui.comp.Boton
import ni.uni.nanobots.sentinel.ui.comp.EstiloBoton
import ni.uni.nanobots.sentinel.ui.comp.Pulso
import ni.uni.nanobots.sentinel.ui.fabrica
import ni.uni.nanobots.sentinel.ui.receptor.llamar
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo

class AlarmaViewModel(private val c: Contenedor) : ViewModel() {
    val estado = c.repo.estado
    fun callar() = c.callar()
    fun sirenaRemota() = c.coordinador.sonarSirenaRemota()
    fun falsaAlarma() = c.coordinador.marcarFalsaAlarma()
    fun cerrar() = c.coordinador.cerrarAlarma()
}

/** ¡INTENTO DE ENTRADA! a pantalla completa, en rojo (el único lugar donde se usa este color). */
@Composable
fun AlarmaPantalla(a: Alerta, vm: AlarmaViewModel = viewModel(factory = fabrica { AlarmaViewModel(it) })) {
    val e by vm.estado.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val receptor = e.rol == Rol.RECEPTOR
    val vecinos = e.vecinosActivos.map { it.nombre }
    BackHandler { vm.cerrar() }
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    0f to Color(0xFFF04A4A),
                    0.45f to Paleta.RojoF,
                    1f to Color(0xFF9E1B1B),
                ),
            )
            // Absorbe los toques para que no lleguen a la pantalla de abajo.
            .clickable(remember { MutableInteractionSource() }, indication = null) { },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "ALARMA ACTIVA · ${e.hogar?.nombre.orEmpty().uppercase()}",
                style = Tipo.Etiqueta,
                color = Color.White.copy(alpha = 0.85f),
            )
            Pulso {
                Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(180.dp)) {
                        val r = size.minDimension / 2
                        drawCircle(Color.White.copy(alpha = 0.05f), r * 0.7f)
                        drawCircle(Color.White.copy(alpha = 0.12f), r * 0.55f)
                        drawCircle(Color.White.copy(alpha = 0.28f), r * 0.34f)
                    }
                    Icon(Iconos.Alerta, null, tint = Color.White, modifier = Modifier.size(62.dp))
                }
            }
            Text(
                "¡INTENTO DE\nENTRADA!",
                style = Tipo.Titulo.copy(fontSize = 30.sp, lineHeight = 33.sp, fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(a.nombre.ifBlank { "Centinela" }, style = Tipo.T1.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Text(
                    "${Formato.fmtHora(a.t)} · ${a.detalle}",
                    style = Tipo.Cuerpo.copy(fontSize = 14.sp),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                )
            }
            val pildora = when {
                !receptor -> "Tu vecino necesita ayuda. Si es seguro, enciende luces y llama."
                vecinos.isEmpty() -> "Aún no hay vecinos de confianza vinculados"
                else -> Formato.listaNombres(vecinos) + if (vecinos.size > 1) " ya fueron avisados" else " ya fue avisado(a)"
            }
            Row(
                Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.22f)).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(if (receptor) Iconos.Personas else Iconos.Casa, null, tint = Color.White, modifier = Modifier.size(15.dp))
                Text(pildora, style = Tipo.Cuerpo.copy(fontSize = 13.sp), color = Color.White)
            }
            Spacer(Modifier.height(24.dp))
            Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Boton("Llamar a la Policía · 118", {
                    vm.callar()
                    llamar(ctx, "118")
                }, icono = Iconos.Llamar, estilo = EstiloBoton.BLANCO)
                if (receptor) {
                    Boton("Sonar sirena en el Centinela", vm::sirenaRemota, icono = Iconos.Sirena, estilo = EstiloBoton.OSCURO)
                    Boton("Estoy en casa · fue falsa alarma", vm::falsaAlarma, estilo = EstiloBoton.BORDE)
                }
                Boton("Silenciar y cerrar", vm::cerrar, icono = Iconos.Volumen, estilo = EstiloBoton.TEXTO, chico = true)
            }
        }
    }
}
