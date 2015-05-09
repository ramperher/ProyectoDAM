package com.dam.proyectodam;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

/**
 *
 * Clase SplashScreenActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase inicial de la aplicación, que muestra un Splash Screen (o pantalla de inicio de la
 * aplicación) cuando accedemos a ella.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.2 alfa
 *
 */
public class SplashScreenActivity extends Activity {

    // Duración del Splash, en milisegundos.
    private static final long SPLASH_SCREEN_DELAY = 5000;

    /**
     * Método: onCreate
     * Método ejecutado cuando se llama a la actividad.
     *
     * @param savedInstanceState instancia de la aplicación para recuperar datos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establece orientación vertical obligatoria.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Elimina la barra superior.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Fija el layout.
        setContentView(R.layout.splash_screen);
        // Lanza a MainActivity tras pasar el tiempo.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // Marcamos el intent con el lanzamiento de la próxima actividad (MainActivity).
                Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(mainIntent);

                // Cerramos esta actividad para que el usuario no pueda volver a ella pulsando atrás.
                finish();
            }
        };

        // Simulamos el proceso de carga al iniciarse la aplicación.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}
