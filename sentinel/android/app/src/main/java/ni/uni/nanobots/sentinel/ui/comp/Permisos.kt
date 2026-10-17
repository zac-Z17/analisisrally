package ni.uni.nanobots.sentinel.ui.comp

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo

private fun notificacionesOk(c: Context) = Build.VERSION.SDK_INT < 33 ||
    ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun bateriaOk(c: Context) =
    (c.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(c.packageName)

private fun pantallaCompletaOk(c: Context) = Build.VERSION.SDK_INT < 34 ||
    c.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

/**
 * Pide, con una explicación corta, los permisos que hacen falta para que la alarma
 * llegue con la app cerrada. Solo aparece si falta alguno.
 */
@SuppressLint("BatteryLife")
@Composable
fun TarjetaPermisos(pantallaCompleta: Boolean) {
    val ctx = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        revision++
        onPauseOrDispose { }
    }
    val pedirNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { revision++ }
    LaunchedEffect(Unit) {
        if (!notificacionesOk(ctx) && Build.VERSION.SDK_INT >= 33) pedirNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Se vuelve a revisar al regresar de los ajustes del sistema.
    val faltaNotif = remember(revision) { !notificacionesOk(ctx) }
    val faltaBateria = remember(revision) { !bateriaOk(ctx) }
    val faltaPantalla = remember(revision) { pantallaCompleta && !pantallaCompletaOk(ctx) }
    if (!faltaNotif && !faltaBateria && !faltaPantalla) return

    fun abrir(accion: String) {
        runCatching { ctx.startActivity(Intent(accion, Uri.parse("package:${ctx.packageName}"))) }
            .onFailure { runCatching { ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))) } }
    }

    Tarjeta(borde = Paleta.Ambar.copy(alpha = 0.35f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Iconos.Info, null, tint = Paleta.Ambar, modifier = Modifier.size(18.dp))
            Text("Para que la alarma llegue con la app cerrada", style = Tipo.T1)
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (faltaNotif) {
                Boton("Permitir notificaciones", {
                    if (Build.VERSION.SDK_INT >= 33) pedirNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else abrir(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                }, icono = Iconos.Campana, estilo = EstiloBoton.SECUNDARIO, chico = true)
            }
            if (faltaBateria) {
                Boton("No optimizar la batería", { abrir(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) }, icono = Iconos.Bateria, estilo = EstiloBoton.SECUNDARIO, chico = true)
            }
            if (faltaPantalla && Build.VERSION.SDK_INT >= 34) {
                Boton("Mostrar alertas a pantalla completa", { abrir(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT) }, icono = Iconos.Alerta, estilo = EstiloBoton.SECUNDARIO, chico = true)
            }
        }
    }
}
