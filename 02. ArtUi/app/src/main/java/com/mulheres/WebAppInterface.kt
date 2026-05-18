package com.mulheres

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class WebAppInterface(
    private val activity: MainActivity
) {

    // ==========================
    // UI THREAD
    // ==========================
    private fun ui(block: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            block()
        }
    }

    // ==========================
    // CARREGAR WEBVIEW
    // ==========================
    @JavascriptInterface
    fun carregarWebView() {
        ui { activity.carregarWebView() }
    }

    @JavascriptInterface
    fun carregarWebView1() {
        ui { activity.carregarWebView1() }
    }

    @JavascriptInterface
    fun carregarWebView2() {
        ui { activity.carregarWebView2() }
    }

    @JavascriptInterface
    fun carregarWebView3() {
        ui { activity.carregarWebView3() }
    }

    @JavascriptInterface
    fun carregarWebView4() {
        ui { activity.carregarWebView4() }
    }

    // ==========================
    // BIOMETRIA BASE
    // ==========================
    @JavascriptInterface
    fun iniciarBiometria(tipo: Int) {
        ui { activity.iniciarBiometria(tipo) }
    }

    // ==========================
    // BIOMETRIA PRÉ-DEFINIDA
    // ==========================
    @JavascriptInterface
    fun iniciarBiometriaAmor() {
        ui { activity.iniciarBiometria(1) }
    }

    @JavascriptInterface
    fun iniciarBiometriaMusica() {
        ui { activity.iniciarBiometria(2) }
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {
        ui { activity.iniciarBiometria(3) }
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {
        ui { activity.iniciarBiometria(4) }
    }
}
