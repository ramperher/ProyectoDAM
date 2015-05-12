package com.dam.proyectodam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 *
 * Clase ResultActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Muestra los resultados finales del entrenamiento, y da la opción de volver a MainActivity
 * para un nuevo entrenamiento o mostrar el recorrido en un mapa.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.2 alfa
 *
 */
public class ResultActivity extends Activity {

    private EditText editVMedia;
    private EditText editDRecorrida;

    /**
     * Método: onCreate
     * Método ejecutado cuando se llama a la actividad.
     *
     * @param savedInstanceState instancia de la aplicación para recuperar datos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        editVMedia   = (EditText) findViewById(R.id.vel_media);
        editDRecorrida = (EditText) findViewById(R.id.dist_rec);
    }

    /**
     * Método: mostrarMapa
     * Método ejecutado cuando se pulsa el primer botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void mostrarMapa(View view) {
        // Marcamos el intent con el lanzamiento de la próxima actividad (MapActivity).
        Intent mapIntent = new Intent(ResultActivity.this, MapActivity.class);
        startActivity(mapIntent);
    }

    /**
     * Método: mostrarDatos
     * Actualiza los valores de distancia recorrida y velocidad media mostrados por pantalla.
     *
     * @param listado array de objetos Point a partir del cual se realizan los cálculos.
     */
    public void mostrarDatos(ArrayList<Point> listado){
        Point punto;
        double d_recorrida=0;
        double v_acumulada=0;
        double v_media=0;
        if(listado.size()>0) {
            for (int i = 0; i < listado.size(); i++) {
                d_recorrida = d_recorrida + listado.get(i).getDistancia();
                v_acumulada = v_acumulada + listado.get(i).getVelocidad();
            }
            v_media = v_acumulada / listado.size();
            editVMedia.setText(String.valueOf(v_media));
            editDRecorrida.setText(String.valueOf(d_recorrida));
        }
    }


    /**
     * Método: volverMain
     * Método ejecutado cuando se pulsa el segundo botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void volverMain(View view) {
        // Simplemente, cerramos la actividad, para volver al Main.
        finish();
    }
}