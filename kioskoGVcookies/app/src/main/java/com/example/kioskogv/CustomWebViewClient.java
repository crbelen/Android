package com.example.kioskogv;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/*
 *comportamiento del webView en relacion con eventos de carga
 * y errores de la página.
 */
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

        // Verifica la URL de la página de inicio de sesión
        if (url.equals("https://panel.gestiondevisitas.es/punto-acceso")) {
            // Extraer credenciales del formulario de inicio de sesión
            view.evaluateJavascript(
                    "(function() { return { username: document.getElementById('username').value, password: document.getElementById('password').value }; })();",
                    result -> {
                        // 'result' contiene las credenciales en formato JSON
                        try {
                            JSONObject jsonResult = new JSONObject(result);
                            String username = jsonResult.getString("username");
                            String password = jsonResult.getString("password");

                            // Guardar las credenciales en SharedPreferences
                            SharedPreferences.Editor editor = mainActivity.sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            );
        }
    }
    /*
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        // Verifica la URL de la página de inicio de sesión
        if (url.equals("https://panel.gestiondevisitas.es/punto-acceso")) {

            // Log para indicar que la carga de la página ha finalizado
            Log.d("WebViewClient", "onPageFinished: " + url);
        }
    }

    */

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        // Puedes manejar errores de carga de la página aquí
        if (error.getErrorCode() == ERROR_TIMEOUT || error.getErrorCode() == ERROR_CONNECT) {
            // Si hay un problema de conexión, intentar recargar cada 30 segundos
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mainActivity.handleErrorAndRefresh();
            }

            // Log para indicar un error de carga de la página
            Log.e("WebViewClient", "onReceivedError: " + "errorCode: " + error.getErrorCode() + ", " +
                    "description: " + error.getDescription() + ", " +
                    "failingUrl: " + request.getUrl());

        } else {
            // mostrar un mensaje al usuario si es necesario
            Toast.makeText(mainActivity, "Error al cargar la página", Toast.LENGTH_SHORT).show();
        }
    }
}
