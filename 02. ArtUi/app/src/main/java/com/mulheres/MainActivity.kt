package com.mulheres

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE = 100
    }

    private lateinit var webView: WebView
    private var destinoBiometria = 0

    // ==========================
    // ON CREATE
    // ==========================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        configurarWebView()

        if (!temPermissoes()) {
            pedirPermissoes()
        } else {
            carregarWebView()
        }
    }

    // ==========================
    // WEBVIEW CONFIG
    // ==========================
    private fun configurarWebView() {

    webView.addJavascriptInterface(WebAppInterface(this), "Android")
        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.allowFileAccess = true

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val url = request?.url.toString()

                if (url.startsWith("tel:")) {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    return true
                }

                if (url.startsWith("https://wa.me")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }

                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript("mostrarConteudo()", null)
            }
        }
    }

    // ==========================
    // CARREGAR WEBVIEW (TODOS)
    // ==========================

    private fun carregarWebView() {
        webView.loadUrl("file:///android_asset/user1/index1.html")
        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView1() {
        webView.loadUrl("file:///android_asset/user1/index1.html")
        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView2() {
        webView.loadUrl("file:///android_asset/user1/index1.html")
        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView3() {
        webView.loadUrl("file:///android_asset/user1/index1.html")
        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView4() {
        webView.loadUrl("file:///android_asset/user1/botao.html")
        webView.visibility = View.VISIBLE
    }

    // ==========================
    // BACK
    // ==========================
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else finish()
    }

    // ==========================
    // PERMISSÕES
    // ==========================
    private fun temPermissoes(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == 0 &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == 0
    }

    private fun pedirPermissoes() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE
            ),
            PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {
            carregarWebView()

            if (!temPermissoes()) {
                Toast.makeText(
                    this,
                    "Permissões não concedidas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ==========================
    // BIOMETRIA (BÁSICA)
    // ==========================
    @JavascriptInterface
    fun iniciarBiometria(tipo: Int) {

        destinoBiometria = tipo

        val biometricManager = BiometricManager.from(this)

        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            carregarWebView4()
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(this)

        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    when (destinoBiometria) {
                        1 -> carregarWebView1()
                        2 -> carregarWebView2()
                        3 -> carregarWebView3()
                        else -> carregarWebView4()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    carregarWebView4()
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear")
            .setDescription("Use biometria ou senha")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(info)
    }
}
