package com.example.androidWebViewOfflineApplication

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.app.usage.UsageStatsManager

class AppLockService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val myPackage = "com.example.androidWebViewOfflineApplication"

    private val runnable = object : Runnable {
        override fun run() {
            checkApp()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceSafe()
        handler.post(runnable)
    }

    private fun checkApp() {

        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()

        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 5000,
            time
        )

        val top = stats.maxByOrNull { it.lastTimeUsed } ?: return

        if (top.packageName != myPackage) {
            val intent = Intent(this, LockScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun startForegroundServiceSafe() {

        val channelId = "applock"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "AppLock",
                NotificationManager.IMPORTANCE_LOW
            )

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Proteção ativa")
            .setContentText("App Lock rodando")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
