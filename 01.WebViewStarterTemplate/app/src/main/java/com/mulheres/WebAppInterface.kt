package com.mulheres

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface

class WebAppInterface(
    private val activity: Activity
) {

    @JavascriptInterface
    fun enviarSOS() {

        (activity as MainActivity).enviarSOS()
    }
@JavascriptInterface
fun abrirContatos() {

    activity.runOnUiThread {

        (activity as MainActivity)
            .abrirContatos()
    }
}

@JavascriptInterface
public void abrirGravador() {
    Intent intent = new Intent(activity, GravarActivity.class);
    activity.startActivity(intent);
}

    @JavascriptInterface
    fun iniciarBiometria() {

        (activity as MainActivity).iniciarBiometria()
    }

    @JavascriptInterface
    fun iniciarBiometriaAmor() {

        (activity as MainActivity)
            .iniciarBiometriaAmor()
    }

    @JavascriptInterface
    fun iniciarBiometriaMusica() {

        (activity as MainActivity)
            .iniciarBiometriaMusica()
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincesa() {

        (activity as MainActivity)
            .iniciarBiometriaPrincesa()
    }

    @JavascriptInterface
    fun iniciarBiometriaPrincipe() {

        (activity as MainActivity)
            .iniciarBiometriaPrincipe()
    }

    @JavascriptInterface
    fun ligarDireto(numero: String) {

        val intent = Intent(Intent.ACTION_CALL)

        intent.data = Uri.parse("tel:$numero")

        activity.startActivity(intent)
    }

    @JavascriptInterface
    fun pegarLocalizacao() {

        (activity as MainActivity)
            .pegarLocalizacao()
    }

    @JavascriptInterface
    fun salvarContatos(lista: String) {

        val prefs = activity.getSharedPreferences(
            "contatos",
            Activity.MODE_PRIVATE
        )

        prefs.edit()
            .putString("lista", lista)
            .apply()
    }

    @JavascriptInterface
fun ativarPalmas() {

    (activity as MainActivity)
        .ativarPalmas()
}

@JavascriptInterface
fun desativarPalmas() {

    (activity as MainActivity)
        .desativarPalmas()
}

 @JavascriptInterface
fun ativarProtecao() {

    (activity as MainActivity)
        .ativarProtecao()
}

@JavascriptInterface
fun desativarProtecao() {

    (activity as MainActivity)
        .desativarProtecao()
}

    @JavascriptInterface
    fun selecionarContato() {

        (activity as MainActivity)
            .abrirContatos()
    }
}
