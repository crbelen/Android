package com.example.kioskogv;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.PowerManager;



public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private final Handler handler = new Handler();

    private static final long ERROR_REFRESH_DELAY = 30 * 1000; // 30 segundos en milisegundos
    private Runnable refreshRunnable;
    private PowerManager.WakeLock wakeLock;

    private CookieManager cookieManager;  // Agregar instancia de CookieManager
    private SharedPreferences sharedPreferences;

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
        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Cargar la página web
        webView.loadUrl("https://panel.gestiondevisitas.es/punto-acceso");

        // Habilitar el acceso a la cámara (necesario para funciones como getUserMedia)
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Puede ser necesario en algunas versiones de Android

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
            float x = event.getX();
            float y = event.getY();

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

        // Inicializar CookieManager
        this.cookieManager = CookieManager.getInstance();
        this.cookieManager.setAcceptCookie(true);

        // Inicializar SharedPreferences
        this.sharedPreferences = getSharedPreferences("MisCookies", Context.MODE_PRIVATE);

        // Cargar las cookies al iniciar la aplicación
        this.loadCookies();
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

        // Guardar las cookies después de la recarga
        this.saveCookies();
    }

    // Método para guardar las cookies en SharedPreferences
    private void saveCookies() {
        String cookies = this.cookieManager.getCookie("https://panel.gestiondevisitas.es/punto-acceso");
        if (cookies != null && !cookies.isEmpty()) {
            // Guardar las cookies en SharedPreferences
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putString("cookies", cookies);
            editor.apply();
        }
    }

    // Método para cargar las cookies desde SharedPreferences
    private void loadCookies() {
        // Cargar las cookies desde SharedPreferences
        String cookies = this.sharedPreferences.getString("cookies", null);

        // Establecer las cookies en CookieManager
        if (cookies != null && !cookies.isEmpty()) {
            this.cookieManager.setCookie("https://panel.gestiondevisitas.es/punto-acceso", cookies);
        }
    }

}

