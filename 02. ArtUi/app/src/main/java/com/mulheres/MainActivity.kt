package com.mulheres

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
    // MYCHROME
    // ==========================
    private val myChrome = object : WebChromeClient() {

        private var customView: View? = null
        private var customViewCallback: CustomViewCallback? = null
        private var originalUiFlags: Int = 0

        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?
        ) {
            callback?.invoke(origin ?: "", true, false)
        }

        override fun onShowCustomView(
            view: View,
            callback: CustomViewCallback
        ) {

            if (customView != null) {
                callback.onCustomViewHidden()
                return
            }

            val decor = window.decorView as ViewGroup

            originalUiFlags = decor.systemUiVisibility

            customView = view
            customViewCallback = callback

            decor.addView(
                customView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            decor.systemUiVisibility =
                
    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
    View.SYSTEM_UI_FLAG_FULLSCREEN or
    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        override fun onHideCustomView() {

            val decor = window.decorView as ViewGroup

            customView?.let {
                decor.removeView(it)
            }

            customView = null

            decor.systemUiVisibility = originalUiFlags

            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        }
    }

    // ==========================
    // DOWNLOAD
    // ==========================
    private val myDownloadListener =
        DownloadListener { url, _, _, _, _ ->

            try {

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )

                startActivity(intent)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Não foi possível baixar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

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

        webView.addJavascriptInterface(
            WebAppInterface(this),
            "Android"
        )

        val s = webView.settings

        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true

        s.allowFileAccess = true
        s.allowContentAccess = true

        s.loadsImagesAutomatically = true

        s.useWideViewPort = true
        s.loadWithOverviewMode = true

        s.mediaPlaybackRequiresUserGesture = false

        s.cacheMode = WebSettings.LOAD_DEFAULT

        s.mixedContentMode =
            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        s.setSupportZoom(false)

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        // ==========================
        // CLIENT
        // ==========================
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val url =
                    request?.url.toString()

                // ==========================
                // TEL
                // ==========================
                if (url.startsWith("tel:")) {

                    startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse(url)
                        )
                    )

                    return true
                }

                // ==========================
                // WHATSAPP
                // ==========================
                if (
                    url.startsWith("https://wa.me") ||
                    url.startsWith("https://api.whatsapp.com")
                ) {

                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        )
                    )

                    return true
                }

                // ==========================
                // INTENT
                // ==========================
                if (url.startsWith("intent://")) {

                    try {

                        val intent = Intent.parseUri(
                            url,
                            Intent.URI_INTENT_SCHEME
                        )

                        startActivity(intent)

                    } catch (e: Exception) {

                        Toast.makeText(
                            this@MainActivity,
                            "Intent não suportada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return true
                }

                // ==========================
                // MARKET
                // ==========================
                if (
                    url.startsWith("market://")
                ) {

                    try {

                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                            )
                        )

                    } catch (e: ActivityNotFoundException) {

                        Toast.makeText(
                            this@MainActivity,
                            "Play Store não encontrada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return true
                }

                return false
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {

                super.onPageFinished(view, url)

                view?.evaluateJavascript(
                    "mostrarConteudo()",
                    null
                )
            }
        }

        webView.webChromeClient = myChrome

        webView.setDownloadListener(
            myDownloadListener
        )
    }

    // ==========================
    // WEBVIEWS
    // ==========================
    fun carregarWebView() {

        webView.loadUrl(
            "file:///android_asset/user/index.html"
        )

        webView.visibility = View.VISIBLE
    }

    fun carregarWebView1() {

        webView.loadUrl(
            "file:///android_asset/user1/index1.html"
        )

        webView.visibility = View.VISIBLE
    }

    fun carregarWebView2() {

        webView.loadUrl(
            "file:///android_asset/user/index.html"
        )

        webView.visibility = View.VISIBLE
    }

    fun carregarWebView3() {

        webView.loadUrl(
            "file:///android_asset/index.html"
        )

        webView.visibility = View.VISIBLE
    }

    fun carregarWebView4() {

        webView.loadUrl(
            "file:///android_asset/user1/botao.html"
        )

        webView.visibility = View.VISIBLE
    }

    // ==========================
    // BACK
    // ==========================
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
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

    // ==========================
    // RESULTADO PERMISSÕES
    // ==========================
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

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
    // BIOMETRIA
    // ==========================
    fun iniciarBiometria(tipo: Int) {

        destinoBiometria = tipo

        val biometricManager =
            BiometricManager.from(this)

        val canAuth =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )

        if (
            canAuth != BiometricManager.BIOMETRIC_SUCCESS
        ) {

            carregarWebView4()

            return
        }

        val executor: Executor =
            ContextCompat.getMainExecutor(this)

        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {

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

        val info =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Desbloquear")
                .setDescription(
                    "Use biometria ou senha"
                )
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

        prompt.authenticate(info)
    }
}
