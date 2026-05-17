package com.example.androidWebViewOfflineApplication

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class LockScreenActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private var isShowingBiometric = false

    private val myPackage by lazy {
        packageName
    }

    private val checkRunnable = object : Runnable {

        override fun run() {

            checkForegroundApp()

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler.post(checkRunnable)
    }

    private fun checkForegroundApp() {

        val usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE)
                    as UsageStatsManager

        val time = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10,
            time
        )

        if (stats.isNullOrEmpty()) {
            return
        }

        val recentApp = stats.maxByOrNull {
            it.lastTimeUsed
        }

        val packageName = recentApp?.packageName ?: return

        // Ignora seu próprio app
        if (packageName == myPackage) {
            return
        }

        // Qualquer app aberto
        if (!isShowingBiometric) {

            isShowingBiometric = true

            showBiometricPrompt()
        }
    }

    private fun showBiometricPrompt() {

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    isShowingBiometric = false
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    isShowingBiometric = false
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("App Lock")
            .setSubtitle("Desbloqueie o aplicativo")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {

        fun startLock(context: Context) {

            val intent = Intent(
                context,
                LockScreenActivity::class.java
            )

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(checkRunnable)
    }
}
