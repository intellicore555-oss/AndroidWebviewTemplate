package com.mulheres

import android.app.Activity
import android.webkit.JavascriptInterface

class WebAppInterface(
    private val activity: Activity
) {

    private fun main(): MainActivity =
        activity as MainActivity

    // ==========================
    // CARREGAR WEBVIEW
    // ==========================
    @JavascriptInterface
    fun carregarWebView() {
        main().carregarWebView()
    }

    @JavascriptInterface
    fun carregarWebView1() {
        main().carregarWebView1()
    }

    @JavascriptInterface
    fun carregarWebView2() {
        main().carregarWebView2()
    }

    @JavascriptInterface
    fun carregarWebView3() {
        main().carregarWebView3()
    }

    @JavascriptInterface
    fun carregarWebView4() {
        main().carregarWebView4()
    }

    // ==========================
    // BIOMETRIA BASE
    // ==========================
    @JavascriptInterface
    fun iniciarBiometria(tipo: Int) {
        main().iniciarBiometria(tipo)
    }

    @JavascriptInterface
    fun iniciarBiometriaAmor() {
        main().iniciarBiometria(1)
    }

    @JavascriptInterface
    fun iniciarBiometriaMusica() {
        main().iniciarBiometria(2)
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {
        main().iniciarBiometria(3)
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {
        main().iniciarBiometria(4)
    }
}
