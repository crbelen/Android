package com.example.kiosko;

import android.annotation.SuppressLint;
import android.content.Context;

import android.os.Bundle;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private PowerManager.WakeLock wakeLock;

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar la barra de título
        getSupportActionBar().hide();

        // Configurar para modo kiosko
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        // Habilitar JavaScript y otras configuraciones necesarias
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //cookies

        // Cargar la página web
        webView.loadUrl("https://panel.gestiondevisitas.es/punto-acceso");

        // Ocultar la barra de navegación
        hideNavigationBar();

        startLockTask();

        // Obtener el PowerManager para controlar el estado de la pantalla
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (null != powerManager) {
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MiApp::MiWakeLockTag");
        }

        //toques para salir de app
        int[] touchCount = {0};

        webView.setOnTouchListener((v, event) -> {
            float x = event.getX();
            float y = event.getY();

            if (MotionEvent.ACTION_DOWN == event.getAction() && 100 > x && 100 > y) {
                touchCount[0]++;
                if (3 == touchCount[0]) {
                    stopLockTask();
                    finish();
                }
            }

            return false;
        });

    }

    protected void onResume() {
        super.onResume();
        // Mantener la pantalla encendida mientras la actividad esté en primer plano
        if (null != wakeLock && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Liberar el WakeLock cuando la actividad pierda el foco
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
        }
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

}

