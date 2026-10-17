package ni.uni.nanobots.sentinel.ui.vecino

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import ni.uni.nanobots.sentinel.ui.comp.Anillo
import ni.uni.nanobots.sentinel.ui.comp.Banner
import ni.uni.nanobots.sentinel.ui.comp.Boton
import ni.uni.nanobots.sentinel.ui.comp.Cabecera
import ni.uni.nanobots.sentinel.ui.comp.Chip
import ni.uni.nanobots.sentinel.ui.comp.Confirmacion
import ni.uni.nanobots.sentinel.ui.comp.EstiloBoton
import ni.uni.nanobots.sentinel.ui.comp.EtiquetaSeccion
import ni.uni.nanobots.sentinel.ui.comp.Fila
import ni.uni.nanobots.sentinel.ui.comp.IcoCaja
import ni.uni.nanobots.sentinel.ui.comp.ListaTarjeta
import ni.uni.nanobots.sentinel.ui.comp.Tarjeta
import ni.uni.nanobots.sentinel.ui.comp.TarjetaPermisos
import ni.uni.nanobots.sentinel.ui.comp.resumenEstado
import ni.uni.nanobots.sentinel.ui.fabrica
import ni.uni.nanobots.sentinel.ui.receptor.NumerosEmergencia
import ni.uni.nanobots.sentinel.ui.receptor.ReceptorViewModel
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo
import ni.uni.nanobots.sentinel.ui.tema.Tono

/** El vecino solo ve la casa que cuida, sus intentos de entrada y los números de emergencia. */
@Composable
fun VecinoPantalla(vm: ReceptorViewModel = viewModel(factory = fabrica { ReceptorViewModel(it) })) {
    val e by vm.estado.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    val ahora by vm.ahora.collectAsStateWithLifecycle()
    val est = resumenEstado(e.cfg, ahora)
    val intentos = e.alertas.filter { it.clase == ClaseAlerta.INTENTO }.take(5)

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Cabecera(e.yo.nombre, pre = "Vecino de confianza") { Chip("VECINO", Tono.VIOLETA) }
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TarjetaPermisos(pantallaCompleta = true)
            Tarjeta(
                borde = if (est.estado == Vigilancia.REPOSO) Paleta.Borde else est.tono.color.copy(alpha = 0.35f),
                fondo = Brush.linearGradient(0f to est.tono.fondo, 0.6f to Paleta.Sup),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Anillo(Iconos.Casa, est.tono, 72.dp, latido = false, tamIcono = 24.dp)
                    Column {
                        Text("Cuidas la casa", style = Tipo.T2)
                        Text(e.hogar?.nombre.orEmpty(), style = Tipo.Titulo.copy(fontWeight = FontWeight.ExtraBold))
                        Text(est.titulo, style = Tipo.T1.copy(fontSize = 13.5.sp), color = est.tono.color)
                        Text("${e.sentinelas.size} Centinela(s) conectados", style = Tipo.T2)
                    }
                }
            }
            if (e.yo.id in e.cfg.vecinosOff) Banner("La familia pausó tus alertas.", Iconos.Info, Tono.AMBAR)
            EtiquetaSeccion("Intentos de entrada recientes")
            ListaTarjeta(
                intentos.map { a ->
                    {
                        Fila {
                            IcoCaja(Iconos.PuertaAbierta, Tono.ROJO, 36.dp, 18.dp)
                            Column(Modifier.weight(1f)) {
                                Text(a.nombre, style = Tipo.T1)
                                Text(
                                    "${Formato.diaDe(a.t, ahora)} · ${Formato.fmtHora(a.t)}" + if (a.que == Etiqueta.FALSA) " · falsa alarma" else "",
                                    style = Tipo.T2,
                                )
                            }
                        }
                    }
                },
                vacio = "Ninguno. Si pasa algo, este celular sonará.",
            )
            NumerosEmergencia()
            Text(
                "Centinel te avisa aunque la app esté cerrada.",
                style = Tipo.T2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (ui.confirmar == "salir") {
                Confirmacion("¿Dejar de cuidar «${e.hogar?.nombre.orEmpty()}»?", "Salir", vm::salir, { vm.confirmar(null) })
            } else {
                Boton("Salir", { vm.confirmar("salir") }, estilo = EstiloBoton.SECUNDARIO)
            }
        }
    }
}
