package com.dam.proyectodam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 *
 * Clase CalculationActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase donde se leen los datos del GPS, se almacenan en la base de datos, y se muestran
 * los resultados del entrenamiento por cada intervalo de tiempo muestreado.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.1 alfa
 *
 */
public class CalculationActivity extends Activity {

    /**
     * Método: onCreate
     * Método ejecutado cuando se llama a la actividad.
     *
     * @param savedInstanceState instancia de la aplicación para recuperar datos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation);
    }
    /**
     * Método: onBackPressed
     * Método ejecutado cuando se pulsa el botón atrás del terminal.
     *
     */
    @Override
    public void onBackPressed() {
        /* Lo único que hacemos es mostrar un Toast diciendo que no se
        puede volver atrás. */
        Toast.makeText(getApplicationContext(),
                "GPS activo: no se puede retroceder.", Toast.LENGTH_LONG).show();

    }

    /**
     * Método: finalizarEntrenamiento
     * Método ejecutado cuando se pulsa el botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void finalizarEntrenamiento(View view) {
        // Marcamos el intent con el lanzamiento de la próxima actividad (ResultActivity).
        Intent resultIntent = new Intent(CalculationActivity.this, ResultActivity.class);
        startActivity(resultIntent);

        // Y cerramos la actividad para no volver a ella al pulsar atrás.
        finish();
    }

}
