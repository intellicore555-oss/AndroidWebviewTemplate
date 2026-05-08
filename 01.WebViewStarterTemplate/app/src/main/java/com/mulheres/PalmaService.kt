package com.mulheres

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread
import kotlin.math.abs

class PalmaService : Service() {

    private var rodando = true

    private var ultimaPalma = 0L

    private var contadorPalmas = 0

    private var recorder: AudioRecord? = null

    override fun onCreate() {

        super.onCreate()

        criarCanal()

        iniciarForeground()

        iniciarDeteccao()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        return START_STICKY
    }

    override fun onDestroy() {

        rodando = false

        try {

            recorder?.stop()

        } catch (_: Exception) {
        }

        try {

            recorder?.release()

        } catch (_: Exception) {
        }

        recorder = null

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }

    // =========================
    // FOREGROUND
    // =========================

    private fun iniciarForeground() {

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val notification: Notification =
            NotificationCompat.Builder(
                this,
                "palmas"
            )
                .setContentTitle(
                    "Proteção por Palmas"
                )
                .setContentText(
                    "Escuta ativa habilitada"
                )
                .setSmallIcon(
                    android.R.drawable.ic_lock_idle_alarm
                )
                .setContentIntent(
                    pendingIntent
                )
                .setOngoing(true)
                .build()

        startForeground(
            2,
            notification
        )
    }

    // =========================
    // CANAL
    // =========================

    private fun criarCanal() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val canal =
                NotificationChannel(
                    "palmas",
                    "Proteção por Palmas",
                    NotificationManager.IMPORTANCE_LOW
                )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                canal
            )
        }
    }

    // =========================
    // DETECÇÃO
    // =========================

    private fun iniciarDeteccao() {

        thread {

            try {

                if (
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    stopSelf()

                    return@thread
                }

                val taxa = 44100

                val bufferSize =
                    AudioRecord.getMinBufferSize(
                        taxa,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

                if (bufferSize <= 0) {

                    stopSelf()

                    return@thread
                }

                recorder =
                    AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        taxa,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )

                if (
                    recorder?.state !=
                    AudioRecord.STATE_INITIALIZED
                ) {

                    stopSelf()

                    return@thread
                }

                val buffer =
                    ShortArray(bufferSize)

                recorder?.startRecording()

                while (rodando) {

                    val leitura =
                        recorder?.read(
                            buffer,
                            0,
                            buffer.size
                        ) ?: 0

                    if (leitura <= 0) {
                        continue
                    }

                    var pico = 0

                    for (i in 0 until leitura) {

                        pico =
                            maxOf(
                                pico,
                                abs(
                                    buffer[i].toInt()
                                )
                            )
                    }

                    // SENSIBILIDADE

                    if (pico > 14000) {

                        val agora =
                            System.currentTimeMillis()

                        // intervalo entre palmas

                        if (
                            agora - ultimaPalma < 1500
                        ) {

                            contadorPalmas++

                        } else {

                            contadorPalmas = 1
                        }

                        ultimaPalma = agora

                        // 3 PALMAS

                        if (contadorPalmas >= 3) {

                            contadorPalmas = 0

                            acionarEmergencia()
                        }
                    }
                }

            } catch (e: Exception) {

                e.printStackTrace()

            } finally {

                try {

                    recorder?.stop()

                } catch (_: Exception) {
                }

                try {

                    recorder?.release()

                } catch (_: Exception) {
                }

                recorder = null
            }
        }
    }

    // =========================
    // EMERGÊNCIA
    // =========================

    private fun acionarEmergencia() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_CALL
                )

            intent.data =
                Uri.parse(
                    "tel:180"
                )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK

            startActivity(intent)

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}
