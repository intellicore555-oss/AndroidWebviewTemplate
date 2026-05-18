package com.mulheres

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
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
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private var destinoBiometria = 0

    // ==========================
    // MYCHROME
    // ==========================
    private val myChrome =
        object : WebChromeClient() {

            private var customView: View? = null

            private var customViewCallback:
                CustomViewCallback? = null

            private var originalUiFlags = 0

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {

                callback?.invoke(
                    origin ?: "",
                    true,
                    false
                )
            }

            override fun onShowCustomView(
                view: View,
                callback: CustomViewCallback
            ) {

                if (customView != null) {

                    callback.onCustomViewHidden()
                    return
                }

                val decor =
                    window.decorView as ViewGroup

                originalUiFlags =
                    decor.systemUiVisibility

                customView = view

                customViewCallback =
                    callback

                decor.addView(
                    customView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                esconderSistema()
            }

            override fun onHideCustomView() {

                val decor =
                    window.decorView as ViewGroup

                customView?.let {

                    decor.removeView(it)
                }

                customView = null

                decor.systemUiVisibility =
                    originalUiFlags

                customViewCallback
                    ?.onCustomViewHidden()

                customViewCallback = null

                esconderSistema()
            }
        }

        

    // ==========================
    // DOWNLOAD
    // ==========================
    private val myDownloadListener =
    DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->

        try {

            val request = android.app.DownloadManager.Request(Uri.parse(url))

            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Baixando arquivo...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(
                android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )

            request.setDestinationInExternalPublicDir(
                android.os.Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType)
            )

            val dm = getSystemService(DOWNLOAD_SERVICE) as android.app.DownloadManager
            dm.enqueue(request)

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao baixar QR", Toast.LENGTH_SHORT).show()
        }
    }
    // ==========================
    // ON CREATE
    // ==========================
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.decorView.setBackgroundColor(
            Color.BLACK
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.statusBarColor =
                Color.BLACK

            window.navigationBarColor =
                Color.BLACK
        }

        esconderSistema()

        setContentView(
            R.layout.activity_main
        )

        webView =
            findViewById(R.id.webview)

        webView.setBackgroundColor(
            Color.BLACK
        )

        configurarWebView()

        carregarWebView()
    }

    // ==========================
    // ESCONDER SISTEMA
    // ==========================
    private fun esconderSistema() {

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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

        // ==========================
        // JAVASCRIPT
        // ==========================
        s.javaScriptEnabled = true

        s.domStorageEnabled = true

        s.databaseEnabled = true

        // ==========================
        // ARQUIVOS
        // ==========================
        s.allowFileAccess = true

        s.allowContentAccess = true

        s.allowFileAccessFromFileURLs = true

        s.allowUniversalAccessFromFileURLs = true

        // ==========================
        // LAYOUT
        // ==========================
        s.loadsImagesAutomatically = true

        s.useWideViewPort = true

        s.loadWithOverviewMode = true

        s.layoutAlgorithm =
            WebSettings.LayoutAlgorithm.NORMAL

        // ==========================
        // VIDEO
        // ==========================
        s.mediaPlaybackRequiresUserGesture =
            false

        // ==========================
        // CACHE
        // ==========================
        s.cacheMode =
            WebSettings.LOAD_DEFAULT

        // ==========================
        // MIXED CONTENT
        // ==========================
        s.mixedContentMode =
            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // ==========================
        // ZOOM
        // ==========================
        s.setSupportZoom(false)

        s.builtInZoomControls = false

        s.displayZoomControls = false

        // ==========================
        // FONTE
        // ==========================
        s.standardFontFamily =
            "Fonte"

        s.fixedFontFamily =
            "Fonte"

        s.serifFontFamily =
            "Fonte"

        s.sansSerifFontFamily =
            "Fonte"

        // ==========================
        // SCROLL
        // ==========================
        webView.isVerticalScrollBarEnabled =
            false

        webView.isHorizontalScrollBarEnabled =
            false

        webView.overScrollMode =
            View.OVER_SCROLL_NEVER

        // ==========================
        // CHROME
        // ==========================
        webView.webChromeClient =
            myChrome

        // ==========================
        // DOWNLOAD
        // ==========================
        webView.setDownloadListener(
            myDownloadListener
        )

        // ==========================
        // CLIENT
        // ==========================
        webView.webViewClient =
            object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val url =
                        request?.url.toString()

                    // ==========================
                    // TEL
                    // ==========================
                    if (
                        url.startsWith("tel:")
                    ) {

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
                    if (
                        url.startsWith("intent://")
                    ) {

                        try {

                            val intent =
                                Intent.parseUri(
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
                    // PLAY STORE
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

                        } catch (
                            e: ActivityNotFoundException
                        ) {

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

    esconderSistema()

    // ==========================
    // INJETAR FONTE (sempre)
    // ==========================
    val jsFonte = """
        (function() {
            var style = document.createElement('style');
            style.innerHTML = `
                @font-face {
                    font-family: 'MinhaFonte';
                    src: url('file:///android_asset/fonte.ttf');
                }

                * {
                    font-family: 'MinhaFonte' !important;
                }
            `;
            document.head.appendChild(style);
        })();
    """.trimIndent()

    view?.evaluateJavascript(jsFonte, null)

    // ==========================
    // CHAMAR JS DA PÁGINA
    // ==========================
    view?.evaluateJavascript(
        """
        (function() {
            if (typeof mostrarConteudo === 'function') {
                mostrarConteudo();
            }
        })();
        """.trimIndent(),
        null
    )
                }
    }

    // ==========================
    // WEBVIEWS
    // ==========================
    fun carregarWebView() {

        webView.loadUrl(
            "file:///android_asset/index.html"
        )

        webView.visibility =
            View.VISIBLE
    }

    fun carregarWebView1() {

        webView.loadUrl(
            "file:///android_asset/user1/index1.html"
        )

        webView.visibility =
            View.VISIBLE
    }

    fun carregarWebView2() {

        webView.loadUrl(
            "file:///android_asset/user/index.html"
        )

        webView.visibility =
            View.VISIBLE
    }

    fun carregarWebView3() {

        webView.loadUrl(
            "file:///android_asset/index.html"
        )

        webView.visibility =
            View.VISIBLE
    }

    fun carregarWebView4() {

        webView.loadUrl(
            "file:///android_asset/user/indes.html"
        )

        webView.visibility =
            View.VISIBLE
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
    // RESUME
    // ==========================
    override fun onResume() {

        super.onResume()

        esconderSistema()
    }

    // ==========================
    // BIOMETRIA
    // ==========================
    fun iniciarBiometria(
        tipo: Int
    ) {

        destinoBiometria = tipo

        val biometricManager =
            BiometricManager.from(this)

        val canAuth =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )

        if (
            canAuth !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {

            carregarWebView4()
            return
        }

        val executor: Executor =
            ContextCompat.getMainExecutor(this)

        val prompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
    result: BiometricPrompt.AuthenticationResult
) {

    super.onAuthenticationSucceeded(result)

    try {

        val afd = assets.openFd("unlock.mp3")

        val mediaPlayer = MediaPlayer()

        mediaPlayer.setDataSource(
            afd.fileDescriptor,
            afd.startOffset,
            afd.length
        )

        afd.close()

        mediaPlayer.isLooping = false
        mediaPlayer.setVolume(1f, 1f)

        mediaPlayer.setOnCompletionListener {
            it.release()
        }

        mediaPlayer.prepare()
        mediaPlayer.start()

    } catch (e: Exception) {
        e.printStackTrace()
    }

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
                .setTitle(
                    "Desbloquear"
                )
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
