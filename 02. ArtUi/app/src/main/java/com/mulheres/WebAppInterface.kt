package com.mulheres

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class WebAppInterface(
    private val activity: Activity
) {

    // ==========================
    // MAIN ACTIVITY
    // ==========================
    private fun main(): MainActivity {

        return activity as MainActivity
    }

    // ==========================
    // UI THREAD
    // ==========================
    private fun ui(
        block: () -> Unit
    ) {

        Handler(
            Looper.getMainLooper()
        ).post {

            block()
        }
    }

    // ==========================
    // CARREGAR WEBVIEW
    // ==========================
    @JavascriptInterface
    fun carregarWebView() {

        ui {

            main().carregarWebView()
        }
    }

    @JavascriptInterface
    fun carregarWebView1() {

        ui {

            main().carregarWebView1()
        }
    }

    @JavascriptInterface
    fun carregarWebView2() {

        ui {

            main().carregarWebView2()
        }
    }

    @JavascriptInterface
    fun carregarWebView3() {

        ui {

            main().carregarWebView3()
        }
    }

    @JavascriptInterface
    fun carregarWebView4() {

        ui {

            main().carregarWebView4()
        }
    }

    // ==========================
    // BIOMETRIA BASE
    // ==========================
    @JavascriptInterface
    fun iniciarBiometria(
        tipo: Int
    ) {

        ui {

            main().iniciarBiometria(
                tipo
            )
        }
    }

    // ==========================
    // BIOMETRIA AMOR
    // ==========================
    @JavascriptInterface
    fun iniciarBiometriaAmor() {

        ui {

            main().iniciarBiometria(
                1
            )
        }
    }

    // ==========================
    // BIOMETRIA MUSICA
    // ==========================
    @JavascriptInterface
    fun iniciarBiometriaMusica() {

        ui {

            main().iniciarBiometria(
                2
            )
        }
    }

    // ==========================
    // BIOMETRIA PRINCESA
    // ==========================
    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {

        ui {

            main().iniciarBiometria(
                3
            )
        }
    }

    // ==========================
    // BIOMETRIA PRINCIPE
    // ==========================
    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {

        ui {

            main().iniciarBiometria(
                4
            )
        }
    }
}
