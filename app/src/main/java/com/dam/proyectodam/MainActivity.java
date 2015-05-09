package com.dam.proyectodam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 *
 * Clase MainActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase principal de servicio, que sirve de punto de partida para la toma de datos del GPS.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.2 alfa
 *
 */
public class MainActivity extends Activity {

    private final static int CALCULOS = 0;

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
     * Método: onBackPressed
     * Método ejecutado cuando se pulsa el botón atrás del terminal.
     *
     */
    @Override
    public void onBackPressed() {
        // Construimos la alerta que saldrá al pulsar el botón.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¿Quieres salir de la aplicación?");

        // Y establecemos los métodos de sí/no.
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Si se pulsa sí, salimos de la aplicación.
                finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Si se pulsa no, se continúa dentro de la aplicación.
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    /**
     * Método: comenzarEntrenamiento
     * Método ejecutado cuando se pulsa el botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void comenzarEntrenamiento(View view) {
        // Marcamos el intent con el lanzamiento de la próxima actividad (CalculationActivity).
        Intent calculationIntent = new Intent().setClass(
                MainActivity.this, CalculationActivity.class);
        startActivityForResult(calculationIntent, CALCULOS);
    }

    /**
     * Método: onActivityResult
     * Método ejecutado cuando se vuelve a esta actividad.
     *
     * @param requestCode código que identifica a la actividad por la que se vuelve.
     * @param resultCode código que indica el resultado final.
     * @param data datos obtenidos como resultado.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CALCULOS) {
            if (resultCode == RESULT_OK) {
                // Código cuando el resultado es correcto.
                /* Se debería reiniciar la base de datos, o almacenar los datos
                para la nueva medida si vamos a guardar varios entrenamientos. */
            }
            if (resultCode == RESULT_CANCELED) {
                // Código cuando no hay resultado.
            }
        }
    }
}
