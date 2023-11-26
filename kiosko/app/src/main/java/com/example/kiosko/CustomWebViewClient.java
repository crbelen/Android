package com.example.kiosko;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class CustomWebViewClient extends WebViewClient {

    private MainActivity mainActivity;

    public  CustomWebViewClient(MainActivity mainActivity){
        this.mainActivity =mainActivity;
    }
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        // Puedes realizar acciones específicas cuando la carga de la página ha comenzado

        // Log para indicar el inicio de la carga de la página
        Log.d("WebViewClient", "onPageStarted: " + url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        // Puedes realizar acciones específicas cuando la carga de la página ha finalizado

        // Log para indicar que la carga de la página ha finalizado
        Log.d("WebViewClient", "onPageFinished: " + url);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        // Puedes manejar errores de carga de la página aquí
        if (error.getErrorCode() == ERROR_TIMEOUT || error.getErrorCode() == ERROR_CONNECT) {
            // Si hay un problema de conexión, intentar recargar cada 30 segundos
            mainActivity.handleErrorAndRefresh();

            // Log para indicar un error de carga de la página
            Log.e("WebViewClient", "onReceivedError: " + error.toString());

        } else {
            // mostrar un mensaje al usuario si es necesario
            Toast.makeText(mainActivity, "Error al cargar la página", Toast.LENGTH_SHORT).show();
        }
    }
}

