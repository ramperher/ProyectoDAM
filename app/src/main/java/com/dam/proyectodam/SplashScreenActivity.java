package com.dam.proyectodam;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class SplashScreenActivity extends Activity {

    // Duración del Splash, en milisegundos.
    private static final long SPLASH_SCREEN_DELAY = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establece orientación vertical.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Elimina la barra superior.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Fija el layout.
        setContentView(R.layout.splash_screen);
        // Lanza MainActivity tras pasar el tiempo.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // Comienza la próxima actividad.
                Intent mainIntent = new Intent().setClass(
                        SplashScreenActivity.this, MainActivity.class);
                startActivity(mainIntent);

                // Cerramos la actividad para que el usuario no pueda volver a ella pulsando atrás.
                finish();
            }
        };

        // Simulamos el proceso de carga al iniciarse la aplicación.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

}