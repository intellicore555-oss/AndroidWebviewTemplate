package com.mulheres

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE = 100
        const val PICK_CONTACT = 1
    }

    var destinoBiometria = 0

    private lateinit var webView: WebView
    private lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.black)
        window.setFlags(1024, 1024)

        window.decorView.systemUiVisibility = 0x1006

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        webView.setBackgroundColor(0xFF000000.toInt())
        webView.visibility = View.INVISIBLE

        locationClient =
            LocationServices.getFusedLocationProviderClient(this)

        if (!temPermissoes()) {
            pedirPermissoes()
        } else {
            carregarWebView2()
        }
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

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
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS,
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

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == PERMISSION_CODE) {

            if (temPermissoes()) {

                carregarWebView2()

            } else {

                Toast.makeText(
                    this,
                    "Aviso: algumas funcionalidades do app nao funcionarao sem permissao, mas sem pressa e nao se preocupe, pode ativar ou nao ativar se quiser!",
                    Toast.LENGTH_LONG
                ).show()

                carregarWebView2()
            }
        }
    }

    private fun configurarWebView() {

        webView.addJavascriptInterface(
            WebAppInterface(this),
            "Android"
        )

        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setGeolocationEnabled(true)
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true

        webView.webChromeClient = object : WebChromeClient() {

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {

                callback?.invoke(origin, true, false)
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val url = request?.url.toString()

                if (url.startsWith("tel:")) {

                    startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse(url)
                        )
                    )

                    return true
                }

                if (url.startsWith("https://wa.me")) {

                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        )
                    )

                    return true
                }

                return false
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {

                super.onPageFinished(view, url)

                val css = """
                    @font-face {
                        font-family: 'MinhaFonte';
                        src: url('file:///android_asset/fonte.ttf');
                    }

                    * {
                        font-family: 'MinhaFonte' !important;
                    }
                """.trimIndent()

                val js = """
                    var style = document.createElement('style');
                    style.innerHTML = `$css`;
                    document.head.appendChild(style);
                """.trimIndent()

                view?.evaluateJavascript(js, null)

                view?.evaluateJavascript(
                    "mostrarConteudo()",
                    null
                )
            }
        }
    }

    private fun carregarWebView() {

        configurarWebView()

        webView.loadUrl(
            "file:///android_asset/user/index.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView1() {

        configurarWebView()

        webView.loadUrl(
            "file:///android_asset/user1/botao.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView2() {

        configurarWebView()

        webView.loadUrl(
            "file:///android_asset/user1/index1.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView3() {

        configurarWebView()

        webView.loadUrl(
            "file:///android_asset/user2/index.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView4() {

        configurarWebView()

        webView.loadUrl(
            "file:///android_asset/user3/index.html"
        )

        webView.visibility = View.VISIBLE
    }

    fun abrirContatos() {

        val intent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        )

        startActivityForResult(intent, PICK_CONTACT)
    }

    fun pegarLocalizacao() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != 0
        ) {
            return
        }

        locationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val lat = location.latitude
                    val lng = location.longitude

                    val js =
                        "javascript:receberLocalizacao($lat,$lng)"

                    webView.evaluateJavascript(js, null)

                } else {

                    webView.post {

                        webView.evaluateJavascript(
                            "alert('Não foi possível obter localização')",
                            null
                        )
                    }
                }
            }
    }

    fun enviarSOS() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != 0
        ) {
            return
        }

        locationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val lat = location.latitude
                    val lng = location.longitude

                    val link =
                        "https://maps.google.com/?q=$lat,$lng"

                    val mensagem =
                        "🚨 SOCORRO! Estou aqui: $link"

                    abrirIntentSMS(mensagem)

                } else {

                    webView.post {

                        webView.evaluateJavascript(
                            "alert('Não foi possível obter localização')",
                            null
                        )
                    }
                }
            }
    }

    private fun abrirIntentSMS(mensagem: String) {

        val prefs: SharedPreferences =
            getSharedPreferences("contatos", MODE_PRIVATE)

        val lista =
            prefs.getString("lista", "") ?: ""

        if (lista.trim().isEmpty()) {

            Toast.makeText(
                this,
                "Nenhum contato cadastrado",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val numeros = lista.split(",")

        val uriBuilder = StringBuilder("smsto:")

        var primeiro = true

        for (numeroRaw in numeros) {

            val numero = numeroRaw.trim()

            if (numero.isNotEmpty()) {

                if (!primeiro) {
                    uriBuilder.append(";")
                }

                uriBuilder.append(numero)

                primeiro = false
            }
        }

        try {

            val intent =
                Intent(Intent.ACTION_SENDTO)

            intent.data =
                Uri.parse(uriBuilder.toString())

            intent.putExtra("sms_body", mensagem)

            if (
                intent.resolveActivity(packageManager)
                != null
            ) {

                startActivity(intent)

            } else {

                Toast.makeText(
                    this,
                    "Nenhum app de SMS encontrado",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Erro ao abrir SMS: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {

        destinoBiometria = 1

        iniciarBiometria()
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {

        destinoBiometria = 2

        iniciarBiometria()
    }

    @JavascriptInterface
    fun iniciarBiometriaAmor() {

        destinoBiometria = 3

        iniciarBiometria()
    }

    @JavascriptInterface
    fun iniciarBiometriaMusica() {

        destinoBiometria = 4

        iniciarBiometria()
    }

    @JavascriptInterface
    fun iniciarBiometria() {

        runOnUiThread {

            val executor: Executor =
                ContextCompat.getMainExecutor(this)

            val biometricPrompt =
                BiometricPrompt(
                    this,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {

                            if (errorCode == 11) {

                                carregarWebView()

                            } else {

                                finish()
                            }
                        }

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {

                            when (destinoBiometria) {

                                1 -> carregarWebView1()

                                2 -> carregarWebView()

                                3 -> carregarWebView3()

                                else -> carregarWebView4()
                            }
                        }
                    }
                )

            val promptInfo =
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Desbloquear")
                    .setSubtitle("Use biometria ou PIN")
                    .setDeviceCredentialAllowed(true)
                    .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun abrirConfiguracoes() {

        val intent = Intent(
            android.provider.Settings
                .ACTION_APPLICATION_DETAILS_SETTINGS
        )

        intent.data = Uri.fromParts(
            "package",
            packageName,
            null
        )

        startActivity(intent)
    }
}