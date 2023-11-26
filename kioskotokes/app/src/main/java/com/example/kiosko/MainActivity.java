package com.example.kiosko;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
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
        webView.loadUrl("https://panel.gestiondevisitas.es/login");

        // Ocultar la barra de navegación
        hideNavigationBar();

        int[] touchCount = {0};

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Obtén las coordenadas del toque
                float x = event.getX();
                float y = event.getY();

                // Verifica si el toque está en la esquina superior izquierda
                if (event.getAction() == MotionEvent.ACTION_DOWN && x < 100 && y < 100) {
                    touchCount[0]++;

                    // Verifica si se han realizado tres toques
                    if (touchCount[0] == 3) {

                        // Salir del modo kiosco
                        stopLockTask();

                        // Cerrar la aplicación
                        finish();
                    }
                }

                return false;
            }
        });



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