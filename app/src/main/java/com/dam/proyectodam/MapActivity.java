package com.dam.proyectodam;

import android.app.Activity;
import android.os.Bundle;

/**
 *
 * Clase MapActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Muestra el recorrido seguido durante el entrenamiento en un mapa.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.1 alfa
 *
 */
public class MapActivity extends Activity {

    /**
     * Método: onCreate
     * Método ejecutado cuando se llama a la actividad.
     *
     * @param savedInstanceState instancia de la aplicación para recuperar datos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }
}
