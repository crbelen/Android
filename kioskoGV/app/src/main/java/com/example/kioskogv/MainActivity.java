package com.example.kioskogv;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.PowerManager;


/*
 *comportamiento del webView en relacion con eventos de carga
 * y errores de la página.
 */
public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private final Handler handler = new Handler();
    private static final long NORMAL_REFRESH_DELAY = 2 * 60 * 1000; // 2 minutos en milisegundos
    private static final long ERROR_REFRESH_DELAY = 30 * 1000; // 30 segundos en milisegundos
    private Runnable refreshRunnable;
    private PowerManager.WakeLock wakeLock;

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // PROVOCA ERROR:Ocultar la barra de título
        //getSupportActionBar().hide();

        // Configurar para modo kiosko
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        this.setContentView(R.layout.activity_main);

        this.webView = this.findViewById(R.id.webView);

        // Habilitar JavaScript y otras configuraciones necesarias
        final WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Cargar la página web
        webView.loadUrl("https://panel.gestiondevisitas.es/punto-acceso");

        // Ocultar la barra de navegación
        this.hideNavigationBar();

        this.startLockTask();

        // Obtener el PowerManager para controlar el estado de la pantalla
        final PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (null != powerManager) {
            this.wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MiApp::MiWakeLockTag");
        }

        //toques para salir de app
        final int[] touchCount = {0};

        this.webView.setOnTouchListener((v, event) -> {
            final float x = event.getX();
            final float y = event.getY();

            if (MotionEvent.ACTION_DOWN == event.getAction() && 100 > x && 100 > y) {
                touchCount[0]++;
                if (3 == touchCount[0]) {
                    this.stopLockTask();
                    this.finish();
                }
            }

            return false;
        });



        // Configurar CustomWebViewClient para manejar eventos de carga y errores
        final CustomWebViewClient customWebViewClient = new CustomWebViewClient(this);
        this.webView.setWebViewClient(customWebViewClient);
    }

    protected void onResume() {
        super.onResume();
        // Mantener la pantalla encendida mientras la actividad esté en primer plano
        if (null != wakeLock && !this.wakeLock.isHeld()) {
            this.wakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Liberar el WakeLock cuando la actividad pierda el foco
        if (null != wakeLock && this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
    }



    private void hideNavigationBar() {
        final View decorView = this.getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.hideNavigationBar();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void handleErrorAndRefresh() {
        // Lógica para manejar el error y programar una recarga más frecuente

        if (handler.hasCallbacks(this.refreshRunnable)) {
            return;
        }

        this.refreshRunnable = () -> {
            this.webView.reload();
            this.handleErrorAndRefresh(); // Programar la siguiente recarga en caso de error persistente
        };

        this.handler.postDelayed(this.refreshRunnable, MainActivity.ERROR_REFRESH_DELAY);
        // Log para indicar que comienza recarga frecuente
        Log.e("MainActivity", "handleErrorAndRefresh: ");
    }


}

