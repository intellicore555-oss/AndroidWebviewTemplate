package com.mulheres

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.media.MediaPlayer
import android.os.Build
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executor
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE = 100
        const val PICK_CONTACT = 1
    }

    var destinoBiometria = 0

    private lateinit var webView: WebView
    private lateinit var locationClient: FusedLocationProviderClient

    // SENSOR
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var shakeListener: SensorEventListener? = null
    private var protecaoAtiva = false
    private var ultimoShake = 0L

    // ==========================
    // PROTEÇÃO
    // ==========================
private fun semInternet(): Boolean {

    val connectivityManager =
        getSystemService(CONNECTIVITY_SERVICE)
                as ConnectivityManager

    val network =
        connectivityManager.activeNetwork
            ?: return true

    val capabilities =
        connectivityManager.getNetworkCapabilities(network)
            ?: return true

    return !capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    )
}

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

@JavascriptInterface
fun ativarPalmas() {

    val intent =
        Intent(
            this,
            PalmaService::class.java
        )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        startForegroundService(intent)

    } else {

        startService(intent)
    }

    Toast.makeText(
        this,
        "Proteção por palmas ativada",
        Toast.LENGTH_SHORT
    ).show()
}

@JavascriptInterface
fun desativarPalmas() {

    stopService(
        Intent(
            this,
            PalmaService::class.java
        )
    )

    Toast.makeText(
        this,
        "Proteção por palmas desativada",
        Toast.LENGTH_SHORT
    ).show()
}

    // ==========================
    // SENSOR
    // ==========================

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
                        sqrt(
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

    private fun pararSensor() {

        shakeListener?.let {

            sensorManager.unregisterListener(it)
        }
    }

    // ==========================
    // LIGAÇÃO
    // ==========================

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

    // ==========================
    // ON CREATE
    // ==========================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(
            android.R.color.black
        )

        window.setFlags(1024, 1024)

        window.decorView.systemUiVisibility = 0x1006

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        webView.setBackgroundColor(
            0xFF000000.toInt()
        )

        webView.visibility = View.INVISIBLE

        locationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

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
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.SEND_SMS,
    Manifest.permission.CALL_PHONE,
    Manifest.permission.RECORD_AUDIO
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

            carregarWebView3()

            if (!temPermissoes()) {

                Toast.makeText(
                    this,
                    "Algumas permissões não foram concedidas.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ==========================
    // WEBVIEW
    // ==========================

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

        webView.webChromeClient =
            object : WebChromeClient() {

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {

                    callback?.invoke(
                        origin,
                        true,
                        false
                    )
                }
            }

        
    
    
                webView.webViewClient =
    object : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {

            val url =
                request?.url.toString()

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

            super.onPageFinished(
                view,
                url
            )

            if (semInternet()) {

                val js = """
                    (function() {

                        var style =
                            document.createElement('style');

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

                view?.evaluateJavascript(
                    js,
                    null
                )
            }

            view?.evaluateJavascript(
                "mostrarConteudo()",
                null
            )
        }
    }
} 
    // ==========================
    // CARREGAR WEBVIEW
    // ==========================

    private fun carregarWebView() {

        webView.loadUrl(
            "file:///android_asset/user1/index1.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView1() {

        webView.loadUrl(
            "file:///android_asset/user1/index1.html"
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
            "file:///android_asset/user1/index1.html"
        )

        webView.visibility = View.VISIBLE
    }

    private fun carregarWebView4() {

        webView.loadUrl(
            "file:///android_asset/user1/botao.html"
        )

        webView.visibility = View.VISIBLE
    }



    // ==========================
    // CONTATOS
    // ==========================

    fun abrirContatos() {

        val intent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        )

        startActivityForResult(
            intent,
            PICK_CONTACT
        )
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

            val cursor =
                contentResolver.query(
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
                    prefs.getString(
                        "lista",
                        ""
                    ) ?: ""

                val nomesAtual =
                    prefs.getString(
                        "nomes",
                        ""
                    ) ?: ""

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
                    .putString(
                        "lista",
                        novaLista
                    )
                    .putString(
                        "nomes",
                        novosNomes
                    )
                    .apply()

                cursor.close()

                Toast.makeText(
                    this,
                    "Contato salvo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ==========================
    // LOCALIZAÇÃO
    // ==========================

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

                    webView.evaluateJavascript(
                        js,
                        null
                    )
                }
            }
    }

    // ==========================
    // SOS
    // ==========================

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
                }
            }
    }

    private fun abrirIntentSMS(
        mensagem: String
    ) {

        val prefs =
            getSharedPreferences(
                "contatos",
                MODE_PRIVATE
            )

        val lista =
            prefs.getString(
                "lista",
                ""
            ) ?: ""

        if (lista.trim().isEmpty()) {

            Toast.makeText(
                this,
                "Nenhum contato cadastrado",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(Intent.ACTION_SENDTO)

        intent.data =
            Uri.parse("smsto:")

        intent.putExtra(
            "address",
            lista
        )

        intent.putExtra(
            "sms_body",
            mensagem
        )

        startActivity(intent)
    }
// ==========================
// BIOMETRIA
// ==========================

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
    canAuth !=
    BiometricManager.BIOMETRIC_SUCCESS
) {

    Toast.makeText(
        this,
        "Biometria indisponível, mas abriremos o serviço pra você!",
        Toast.LENGTH_SHORT
    ).show()

    carregarWebView4()

    return@runOnUiThread
        }

        val executor: Executor =
            ContextCompat.getMainExecutor(this)

        val biometricPrompt =
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

                            val afd =
                                assets.openFd("unlock.mp3")

                            val mediaPlayer =
                                MediaPlayer()

                            mediaPlayer.setDataSource(
                                afd.fileDescriptor,
                                afd.startOffset,
                                afd.length
                            )

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

    Toast.makeText(
        this@MainActivity,
        "Biometria não disponível, recomendo ativar a biometria no seu aparelho, porém mesmo assim abriremos os seus acessos!",
        Toast.LENGTH_SHORT
    ).show()

    carregarWebView4()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Desbloquear")
                .setDescription(
                    "Use biometria, PIN ou senha"
                )
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }
}

private fun abrirConfiguracoes() {

    val intent =
        Intent(
            android.provider.Settings
                .ACTION_APPLICATION_DETAILS_SETTINGS
        )

    intent.data =
        Uri.fromParts(
            "package",
            packageName,
            null
        )

    startActivity(intent)
}}
