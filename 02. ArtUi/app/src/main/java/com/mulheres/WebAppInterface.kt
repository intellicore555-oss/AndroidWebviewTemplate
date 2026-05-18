package com.mulheres

import android.webkit.JavascriptInterface

class WebAppInterface(
    private val activity: MainActivity
) {

    @JavascriptInterface
    fun carregarWebView() {
        activity.runOnUiThread { activity.carregarWebView() }
    }

    @JavascriptInterface
    fun carregarWebView1() {
        activity.runOnUiThread { activity.carregarWebView1() }
    }

    @JavascriptInterface
    fun carregarWebView2() {
        activity.runOnUiThread { activity.carregarWebView2() }
    }

    @JavascriptInterface
    fun carregarWebView3() {
        activity.runOnUiThread { activity.carregarWebView3() }
    }

    @JavascriptInterface
    fun carregarWebView4() {
        activity.runOnUiThread { activity.carregarWebView4() }
    }

    @JavascriptInterface
    fun iniciarBiometria(tipo: Int) {
        activity.runOnUiThread { activity.iniciarBiometria(tipo) }
    }

    @JavascriptInterface
    fun iniciarBiometriaAmor() {
        activity.runOnUiThread { activity.iniciarBiometria(1) }
    }

    @JavascriptInterface
    fun iniciarBiometriaMusica() {
        activity.runOnUiThread { activity.iniciarBiometria(2) }
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {
        activity.runOnUiThread { activity.iniciarBiometria(3) }
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {
        activity.runOnUiThread { activity.iniciarBiometria(4) }
    }
}
