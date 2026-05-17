package com.example.androidWebViewOfflineApplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val url = "file:///android_asset/index.html"
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startAppLockService()

        hideSystemUI()

        webView = WebView(this).apply {

            webViewClient = WebViewClient()

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
            }

            setBackgroundColor(0xFF000000.toInt())
        }

        setContentView(webView)

        checkBiometric()
    }

    // 🔐 AppLock service seguro
    private fun startAppLockService() {
        val intent = Intent(this, AppLockService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // 📱 FULLSCREEN REAL
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    // 🔐 BIOMETRIA
    private fun checkBiometric() {

        val manager = BiometricManager.from(this)

        val canAuth = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )

        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometric()
        } else {
            openSite()
        }
    }

    private fun showBiometric() {

        val executor = ContextCompat.getMainExecutor(this)

        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    openSite()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    finish()
                }

                override fun onAuthenticationFailed() {
                    // opcional: feedback leve
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acesso seguro")
            .setSubtitle("Use sua biometria para entrar")
            .setNegativeButtonText("Cancelar")
            .build()

        prompt.authenticate(info)
    }

    // 🌐 ABRIR SITE
    private fun openSite() {
        webView.loadUrl(url)
    }
}
