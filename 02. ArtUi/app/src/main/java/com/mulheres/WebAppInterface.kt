package com.mulheres

import android.app.Activity
import android.webkit.JavascriptInterface

class WebAppInterface(
    private val activity: Activity
) {

    private fun main(): MainActivity {
        return activity as MainActivity
    }

    // ==========================
    // CARREGAR WEBVIEWS
    // ==========================
    @JavascriptInterface
    fun carregarWebView() =
        activity.runOnUiThread { main().carregarWebView() }

    @JavascriptInterface
    fun carregarWebView1() =
        activity.runOnUiThread { main().carregarWebView1() }

    @JavascriptInterface
    fun carregarWebView2() =
        activity.runOnUiThread { main().carregarWebView2() }

    @JavascriptInterface
    fun carregarWebView3() =
        activity.runOnUiThread { main().carregarWebView3() }

    @JavascriptInterface
    fun carregarWebView4() =
        activity.runOnUiThread { main().carregarWebView4() }

    // ==========================
    // BIOMETRIA (CORRETO)
    // ==========================
    @JavascriptInterface
    fun iniciarBiometria(tipo: Int) =
        activity.runOnUiThread { main().iniciarBiometria(tipo) }

    @JavascriptInterface
    fun iniciarBiometriaAmor() =
        activity.runOnUiThread { main().iniciarBiometria(1) }

    @JavascriptInterface
    fun iniciarBiometriaMusica() =
        activity.runOnUiThread { main().iniciarBiometria(2) }

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() =
        activity.runOnUiThread { main().iniciarBiometria(3) }

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() =
        activity.runOnUiThread { main().iniciarBiometria(4) }
}
