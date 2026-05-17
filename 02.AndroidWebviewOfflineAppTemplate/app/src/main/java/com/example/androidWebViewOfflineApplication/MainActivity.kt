package com.example.androidWebViewOfflineApplication

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
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

        hideSystemUI()

        requestPermissionsIfNeeded()

        // 🔥 inicia AppLock service (IMPORTANTE)
        startService(Intent(this, AppLockService::class.java))

        webView = WebView(this).apply {

            setBackgroundColor(Color.BLACK)

            webViewClient = WebViewClient()

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
        }

        setContentView(webView)

        checkBiometricAndAuthenticate()
    }

    // 🔥 FULLSCREEN (status + nav bar escondidos)
    private fun hideSystemUI() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.insetsController?.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )

        } else {

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun requestPermissionsIfNeeded() {

        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
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

    // 🔥 BIOMETRIA
    private fun checkBiometricAndAuthenticate() {

        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {

            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }

            else -> {
                Toast.makeText(
                    this,
                    "Sem biometria - carregando offline",
                    Toast.LENGTH_SHORT
                ).show()

                loadOffline()
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

                    loadOffline()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticação")
            .setSubtitle("Use sua digital")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // 🔥 OFFLINE MODE + FONT TTF
    private fun loadOffline() {

        webView.loadUrl(applicationUrl)

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // aplica fonte local assets/fonts/font.ttf
                webView.evaluateJavascript(
                    """
                    var newStyle = document.createElement('style');
                    newStyle.innerHTML = `
                        @font-face {
                            font-family: 'CustomFont';
                            src: url('file:///android_asset/fonts/font.ttf');
                        }
                        body {
                            font-family: 'CustomFont';
                            background-color: black;
                            color: white;
                        }
                    `;
                    document.head.appendChild(newStyle);
                    """.trimIndent(),
                    null
                )
            }
        }
    }
}
