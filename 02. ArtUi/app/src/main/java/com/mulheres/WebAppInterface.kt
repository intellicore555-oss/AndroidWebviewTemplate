package com.mulheres

import android.webkit.JavascriptInterface

class WebAppInterface(
    private val activity: MainActivity
) {

    private fun ui(block: () -> Unit) {
        activity.runOnUiThread {
            block()
        }
    }

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

    @JavascriptInterface
    fun iniciarBiometria(tipo: Int) {
        ui { activity.iniciarBiometria(tipo) }
    }

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
