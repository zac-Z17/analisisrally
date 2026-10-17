package ni.uni.nanobots.sentinel.ui.comp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import ni.uni.nanobots.sentinel.domain.hogar.Aviso
import ni.uni.nanobots.sentinel.domain.hogar.TipoAviso
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo

fun iconoAviso(t: TipoAviso): ImageVector = when (t) {
    TipoAviso.INFO -> Iconos.Info
    TipoAviso.CHECK -> Iconos.Check
    TipoAviso.ALERTA -> Iconos.Alerta
    TipoAviso.VIBRACION -> Iconos.Vibrar
    TipoAviso.SIRENA -> Iconos.Sirena
    TipoAviso.BATERIA -> Iconos.Bateria
    TipoAviso.SIN_RED -> Iconos.SinWifi
    TipoAviso.RAYO -> Iconos.Rayo
    TipoAviso.PAUSA -> Iconos.Pausa
    TipoAviso.ESCUDO -> Iconos.EscudoOk
    TipoAviso.CANDADO -> Iconos.Candado
    TipoAviso.PLAY -> Iconos.Play
    TipoAviso.USUARIO -> Iconos.AgregarPersona
}

/** Muestra cada aviso 4,2 s, como el «toast» del prototipo. */
@Composable
fun AvisoHost(avisos: Flow<Aviso>, margenInferior: Dp) {
    var actual by remember { mutableStateOf<Aviso?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(avisos) {
        avisos.collect {
            actual = it
            visible = true
        }
    }
    LaunchedEffect(actual) {
        if (actual != null) {
            delay(4_200)
            visible = false
        }
    }
    Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = margenInferior), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
            val a = actual ?: return@AnimatedVisibility
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 440.dp)
                    .clip(RadioBoton)
                    .background(Paleta.AvisoFondo)
                    .border(1.dp, Paleta.Borde, RadioBoton)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(iconoAviso(a.tipo), null, tint = Paleta.Txt, modifier = Modifier.size(18.dp))
                Text(a.texto, style = Tipo.Cuerpo.copy(fontSize = Tipo.Cuerpo.fontSize * 0.93f))
            }
        }
    }
}
