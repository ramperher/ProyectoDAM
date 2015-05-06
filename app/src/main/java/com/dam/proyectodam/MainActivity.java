package com.dam.proyectodam;

import android.app.Activity;
import android.os.Bundle;

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
}
