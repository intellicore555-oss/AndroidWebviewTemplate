package com.example.androidWebViewOfflineApplication

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private val applicationUrl = "file:///android_asset/index.html"

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionsIfNeeded()

        LockScreenActivity.startLock(this)

        webView = WebView(this).apply {

            webViewClient = WebViewClient()

            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                domStorageEnabled = true
            }
        }

        setContentView(webView)

        checkBiometricAndAuthenticate()
    }

    private fun requestPermissionsIfNeeded() {

        if (!Settings.canDrawOverlays(this)) {

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            startActivity(intent)
        }

        if (!hasUsageStatsPermission()) {

            val intent = Intent(
                Settings.ACTION_USAGE_ACCESS_SETTINGS
            )

            startActivity(intent)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )

        } else {

            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }

        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun checkBiometricAndAuthenticate() {

        val biometricManager = BiometricManager.from(this)

        when (
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
        ) {

            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }

            else -> {
                Toast.makeText(
                    this,
                    "Biometria não disponível",
                    Toast.LENGTH_SHORT
                ).show()

                // SEM biometria → abre o site direto
                webView.loadUrl(applicationUrl)
            }
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

                    webView.loadUrl(applicationUrl)
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    Toast.makeText(
                        this@MainActivity,
                        "Autenticação cancelada",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Toast.makeText(
                        this@MainActivity,
                        "Biometria inválida",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticação Biométrica")
            .setSubtitle("Use sua digital para entrar")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
