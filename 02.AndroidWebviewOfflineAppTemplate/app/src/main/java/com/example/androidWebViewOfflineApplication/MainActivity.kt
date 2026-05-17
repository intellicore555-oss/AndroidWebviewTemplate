package com.example.androidWebViewOfflineApplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val url = "file:///android_asset/index.html"
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(Intent(this, AppLockService::class.java))

        hideSystemUI()

        webView = WebView(this).apply {

            webViewClient = WebViewClient()

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            setBackgroundColor(0xFF000000.toInt())
        }

        setContentView(webView)

        checkBiometric()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun checkBiometric() {

        val manager = BiometricManager.from(this)

        if (manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) == BiometricManager.BIOMETRIC_SUCCESS
        ) {
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

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    openSite()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    finish()
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acesso seguro")
            .setSubtitle("Use sua biometria")
            .setNegativeButtonText("Cancelar")
            .build()

        prompt.authenticate(info)
    }

    private fun openSite() {
        webView.loadUrl(url)
    }
}
