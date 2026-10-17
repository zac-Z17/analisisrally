package ni.uni.nanobots.sentinel.servicio

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ni.uni.nanobots.sentinel.R
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.ui.AlarmaActivity
import ni.uni.nanobots.sentinel.ui.MainActivity

/** Canales y notificaciones: la fija del servicio, la alarma a pantalla completa y los avisos. */
class Notificador(private val context: Context) {
    private val nm = NotificationManagerCompat.from(context)
    private var siguienteId = ID_AVISOS

    fun crearCanales() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val sistema = context.getSystemService(NotificationManager::class.java)
        sistema.createNotificationChannels(
            listOf(
                NotificationChannel(CANAL_ALARMA, "Intentos de entrada", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Pantalla roja cuando alguien intenta entrar"
                    setSound(null, null)          // la sirena la toca la app por el canal de alarma
                    enableVibration(false)
                    setBypassDnd(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
                NotificationChannel(CANAL_AVISOS, "Avisos", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Vibraciones leves, batería baja y equipos sin conexión"
                },
                NotificationChannel(CANAL_SERVICIO, "Centinel activo", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Aviso fijo mientras Centinel cuida la casa"
                    setShowBadge(false)
                },
            ),
        )
    }

    fun servicio(texto: String): Notification =
        NotificationCompat.Builder(context, CANAL_SERVICIO)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Centinel")
            .setContentText(texto)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(abrirApp())
            .build()

    @SuppressLint("MissingPermission")
    fun actualizarServicio(texto: String) {
        if (puedeNotificar()) nm.notify(ID_SERVICIO, servicio(texto))
    }

    @SuppressLint("MissingPermission")
    fun alarma(a: Alerta) {
        if (!puedeNotificar()) return
        val pantalla = PendingIntent.getActivity(
            context, 1,
            Intent(context, AlarmaActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CANAL_ALARMA)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("¡Intento de entrada!")
            .setContentText("${a.nombre} · ${Formato.fmtHora(a.t)} · ${a.detalle}")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(context, R.color.rojo_alerta))
            .setFullScreenIntent(pantalla, true)
            .setContentIntent(pantalla)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
        nm.notify(ID_ALARMA, n)
    }

    fun cerrarAlarma() = nm.cancel(ID_ALARMA)

    @SuppressLint("MissingPermission")
    fun aviso(texto: String) {
        if (!puedeNotificar()) return
        val n = NotificationCompat.Builder(context, CANAL_AVISOS)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Centinel")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setAutoCancel(true)
            .setContentIntent(abrirApp())
            .build()
        nm.notify(siguienteId++, n)
        if (siguienteId > ID_AVISOS + 50) siguienteId = ID_AVISOS
    }

    fun puedeNotificar(): Boolean =
        (Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            nm.areNotificationsEnabled()

    private fun abrirApp(): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val CANAL_ALARMA = "alarma"
        const val CANAL_AVISOS = "avisos"
        const val CANAL_SERVICIO = "servicio"
        const val ID_SERVICIO = 1
        const val ID_ALARMA = 2
        private const val ID_AVISOS = 100
    }
}
