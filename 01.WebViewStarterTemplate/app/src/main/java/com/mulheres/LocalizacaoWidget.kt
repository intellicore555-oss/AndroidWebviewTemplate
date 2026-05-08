package com.mulheres

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalizacaoWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for (appWidgetId in appWidgetIds) {

            val intent = Intent(context, LocalizacaoWidget::class.java).apply {
                action = "ENVIAR_LOCALIZACAO"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_localizacao)

            views.setOnClickPendingIntent(
                R.id.localizacao,
                pendingIntent
            )

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "ENVIAR_LOCALIZACAO") {

            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context)

            try {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->

                        val lat = location?.latitude ?: 0.0
                        val lon = location?.longitude ?: 0.0

                        val horario = SimpleDateFormat(
                            "dd/MM/yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())

                        val mensagem = """
🚨 EMERGÊNCIA 🚨

Uma solicitação de ajuda foi acionada pelo aplicativo Mulher Amparada.

📍 Localização atual:
Latitude: $lat
Longitude: $lon

📱 Dispositivo:
${Build.MANUFACTURER} ${Build.MODEL}

🕒 Horário:
$horario

⚠️ Caso necessário, tente entrar em contato imediatamente.
""".trimIndent()

                        val uri = Uri.parse(
                            "https://wa.me/?text=" + Uri.encode(mensagem)
                        )

                        val whatsappIntent = Intent(
                            Intent.ACTION_VIEW,
                            uri
                        )

                        whatsappIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        context.startActivity(whatsappIntent)
                    }

            } catch (_: Exception) {
            }
        }
    }
}
