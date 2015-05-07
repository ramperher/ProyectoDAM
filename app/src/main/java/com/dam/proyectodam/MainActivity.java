package com.dam.proyectodam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 *
 * Clase MainActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase principal de servicio, que sirve de punto de partida para la toma de datos del GPS.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.1 alfa
 *
 */
public class MainActivity extends Activity {

    /**
     * Método: onCreate
     * Método ejecutado cuando se llama a la actividad.
     *
     * @param savedInstanceState instancia de la aplicación para recuperar datos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Por ahora, sólo muestra el Hola mundo en el layout.
        setContentView(R.layout.activity_main);
    }

    /**
     * Método: comenzarEntrenamiento
     * Método ejecutado cuando se pulsa el botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void comenzarEntrenamiento(View view) {
        // Prueba de que funciona.
        Toast.makeText(getApplicationContext(),"Funciona", Toast.LENGTH_LONG).show();
    }
}
