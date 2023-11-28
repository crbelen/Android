package com.example.kiosko;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;



/*
*permite personalizar el comportamiento del webView en relacion con eventos de carga
* y errores de la página.
*/
public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Handler handler = new Handler();
    private static final long NORMAL_REFRESH_DELAY = 2 * 60 * 1000; // 2 minutos en milisegundos
    private static final long ERROR_REFRESH_DELAY = 30 * 1000; // 30 segundos en milisegundos
    private Runnable refreshRunnable;


    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar para modo kiosko
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        // Habilitar JavaScript y otras configuraciones necesarias
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Cargar la página web
        webView.loadUrl("https://panel.gestiondevisitas.es/punto-acceso");

        // Ocultar la barra de navegación
        hideNavigationBar();

            startLockTask();


        int[] touchCount = {0};

        webView.setOnTouchListener((v, event) -> {
            float x = event.getX();
            float y = event.getY();

            if (MotionEvent.ACTION_DOWN == event.getAction() && x < 100 && y < 100) {
                touchCount[0]++;
                resetRefreshTimer(); // Reiniciar el temporizador después de la interacción
                if (3 == touchCount[0]) {
                    stopLockTask();
                    finish();
                }
            }

            return false;
        });

        // Iniciar el temporizador al cargar la aplicación
        resetRefreshTimer();

        // Configurar CustomWebViewClient para manejar eventos de carga y errores
        CustomWebViewClient customWebViewClient = new CustomWebViewClient(this);
        webView.setWebViewClient(customWebViewClient);
    }


    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

    public void resetRefreshTimer() {
        if (refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }

        refreshRunnable = () -> {
            webView.reload();
            resetRefreshTimer();
        };

        handler.postDelayed(refreshRunnable, NORMAL_REFRESH_DELAY);
    }
    public void handleErrorAndRefresh() {
        // Lógica para manejar el error y programar una recarga más frecuente
        handler.postDelayed(refreshRunnable, ERROR_REFRESH_DELAY);
        // Log para indicar que comienza recarga frecuente
        Log.e("MainActivity", "handleErrorAndRefresh: ");
    }


}

