package com.mulheres

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
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

   private lateinit var sensorManager: SensorManager
private var acelerometro: Sensor? = null

private var shakeListener: SensorEventListener? = null

private var protecaoAtiva = false

private var ultimoShake = 0L

// ================= ATIVAR =================


fun ativarProtecao() {
@JavascriptInterface
fun ativarProtecao() {

    protecaoAtiva = true

    iniciarSensor()
}

@JavascriptInterface
fun desativarProtecao() {

    protecaoAtiva = false

    pararSensor()
}

// ================= SENSOR =================

private fun iniciarSensor() {

    sensorManager =
        getSystemService(SENSOR_SERVICE)
                as SensorManager

    acelerometro =
        sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )

    shakeListener =
        object : SensorEventListener {

            override fun onSensorChanged(
                event: SensorEvent
            ) {

                if (!protecaoAtiva) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val aceleracao =
                    Math.sqrt(
                        (x * x + y * y + z * z).toDouble()
                    )

                if (aceleracao > 18) {

                    val agora =
                        System.currentTimeMillis()

                    if (agora - ultimoShake > 4000) {

                        ultimoShake = agora

                        ligarDireto("180")
                    }
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int
            ) {
            }
        }

    acelerometro?.let {

        sensorManager.registerListener(
            shakeListener,
            it,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}
}
// ================= LIGAÇÃO =================

@JavascriptInterface
fun ligarDireto(numero: String) {

    try {

        val intent =
            Intent(Intent.ACTION_CALL)

        intent.data =
            Uri.parse("tel:$numero")

        startActivity(intent)

    } catch (e: Exception) {

        e.printStackTrace()
    }
}

// ================= LIGAÇÃO =================


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

        configurarWebView()

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

            carregarWebView2()

            if (!temPermissoes()) {

                Toast.makeText(
                    this,
                    "Algumas funções podem não funcionar sem permissões.",
                    Toast.LENGTH_LONG
                ).show()
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

        webView.loadUrl(
            "file:///android_asset/user1/botao.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView1() {

        webView.loadUrl(
            "file:///android_asset/user1/botao.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView2() {

        webView.loadUrl(
            "file:///android_asset/user1/index1.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView3() {

        webView.loadUrl(
            "file:///android_asset/user2/index.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView4() {

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

    override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {

    super.onActivityResult(
        requestCode,
        resultCode,
        data
    )

    if (
        requestCode == PICK_CONTACT &&
        resultCode == RESULT_OK
    ) {

        val uri = data?.data ?: return

        val cursor = contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )

        if (
            cursor != null &&
            cursor.moveToFirst()
        ) {

            val numeroIndex =
                cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

            val nomeIndex =
                cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )

            val numero =
                cursor.getString(numeroIndex)
                    ?.replace("\\s".toRegex(), "")
                    ?.replace("-", "")
                    ?: ""

            val nome =
                cursor.getString(nomeIndex)
                    ?: "Contato"

            val prefs =
                getSharedPreferences(
                    "contatos",
                    MODE_PRIVATE
                )

            val listaAtual =
                prefs.getString("lista", "")
                    ?: ""

            val nomesAtual =
                prefs.getString("nomes", "")
                    ?: ""

            val novaLista =
                if (listaAtual.isEmpty()) {
                    numero
                } else {
                    "$listaAtual,$numero"
                }

            val novosNomes =
                if (nomesAtual.isEmpty()) {
                    "$nome - $numero"
                } else {
                    "$nomesAtual\n$nome - $numero"
                }

            prefs.edit()
                .putString("lista", novaLista)
                .putString("nomes", novosNomes)
                .apply()

            webView.post {

                webView.evaluateJavascript(
                    "contatoSalvo('$nome','$numero')",
                    null
                )
            }

            cursor.close()

            Toast.makeText(
                this,
                "Contato salvo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
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

            val biometricManager =
                BiometricManager.from(this)

            val canAuth =
                biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )

            if (
                canAuth ==
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ||
                canAuth ==
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                canAuth ==
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
            ) {

                carregarWebView()

                return@runOnUiThread
            }

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

                            when (errorCode) {

                                BiometricPrompt.ERROR_USER_CANCELED,
                                BiometricPrompt.ERROR_CANCELED -> {

                                    Toast.makeText(
                                        this@MainActivity,
                                        "Autenticação cancelada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {

                                    Toast.makeText(
                                        this@MainActivity,
                                        errString,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
                    .setDescription("Use biometria, PIN ou senha")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
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
