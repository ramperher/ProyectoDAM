package com.dam.proyectodam;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.NumberPicker;

/**
 *
 * Clase MainActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase principal de servicio, que sirve de punto de partida para la toma de datos del GPS.
 * Esta actividad tiene en cuenta la orientación del terminal (horizontal o vertical), y
 * tiene un layout para cada caso. El NumberPicker está adaptado para ambos casos, y no
 * hace falta dos ficheros layout.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.5 alfa
 *
 */
public class MainActivity extends ActionBarActivity {

    /* Identificador del intent lanzado al comenzar el entrenamiento, para hacer las acciones
    pertinentes al volver a esta actividad. */
    private final static int ENTRENAMIENTO = 0;

    // Tiempos mínimo y máximo de actualización del GPS (límites que tendremos para elegir).
    private final static int MIN_T_ACT = 1;
    private final static int MAX_T_ACT = 60;

    /* Tiempo de actualización del GPS, que puede ser modificado por el usuario. Será de 10
    segundos por defecto (está en milisegundos). */
    private int tiempo_actualizacion = 1000*10;

    /**
     * Método: onCreate
     * Método ejecutado cuando se llama a la actividad.
     *
     * @param savedInstanceState instancia de la actividad para recuperar datos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restauramos el estado de tiempo_actualizacion.
        if (savedInstanceState != null){
            tiempo_actualizacion = savedInstanceState.getInt("tiempo_actualizacion");

            Log.d("Main", "Restaurado tiempo de actualización de " + tiempo_actualizacion);
        }

        // Mostramos el layout de la actividad.
        setContentView(R.layout.activity_main);

        Log.d("Main", "Actividad principal desplegada");
    }

    /**
     * Método: onCreateOptionsMenu
     * Método que lanza la barra de menú en la parte superior de la pantalla.
     *
     * @param menu menú a lanzar
     * @return true, ya que funciona correctamente.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Simplemente, lanzamos la barra de menú.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Log.d("Main", "Mostramos el menú");

        return true;
    }

    /**
     * Método: onSaveInstanceState
     * Método que guarda el atributo de tiempo de actualización, para casos de rotación
     * del terminal.
     *
     * @param state estado a guardar.
     */
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Y guardamos la variable.
        state.putInt("tiempo_actualizacion", tiempo_actualizacion);

        Log.d("Main", "Guardado tiempo de actualización de " + tiempo_actualizacion);
    }

    /**
     * Método: onOptionsItemSelected
     * Método que realiza una acción u otra en función de la opción seleccionada
     * en el menú superior. En este caso, sólo actúa con la única opción implementada,
     * que es la modificación del tiempo de actualización.
     *
     * @param item opción seleccionada del menú.
     * @return true en caso de ir bien, false en caso contrario.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean completado = false; // De partida, suponemos que no se realiza la acción.

        /* Se comprueba si hemos pulsado tiempo, por si se añaden más opciones en el futuro
        (se extendería a un switch). */

        if (item.getItemId() == R.id.tiempo) {
            Log.d("Main", "Pulsamos el botón de tiempo");

            // Modificamos el tiempo de actualización (dentro de mostrarNumberPicker).
            mostrarNumberPicker();

            // Fue bien: actualizamos a true.
            completado = true;
        }

        return completado;
    }

    /**
     * Método: mostrarNumberPicker
     * Método que pide al usuario cambiar el tiempo de actualización del GPS a través
     * de un NumberPicker.
     */
    public void mostrarNumberPicker()
    {
        // Generamos el Dialog, y fijamos título y layout.
        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle("Fijar tiempo (en segundos)");
        d.setContentView(R.layout.number_picker);

        // Añadimos los dos botones del Dialog.
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);

        // Creamos el NumberPicker, con sus límites.
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMinValue(MIN_T_ACT);
        np.setMaxValue(MAX_T_ACT);
        np.setWrapSelectorWheel(false);

        /* Fijamos los listener para los dos botones:
            b1 (Fijar) sobreescribe el tiempo de actualización.
            b2 (No fijar) no hace nada, y mantiene el tiempo que estaba.
        */
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                tiempo_actualizacion = 1000 * np.getValue();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        // Mostramos el Dialog.
        d.show();

        Log.d("Main", "Mostramos el NumberPicker");
    }

    /**
     * Método: onBackPressed
     * Método ejecutado cuando se pulsa el botón atrás del terminal, preguntando
     * si desea salir de la aplicación o no, en vez de sacarlo directamente.
     */
    @Override
    public void onBackPressed() {
        Log.d("Main", "Pulsamos el botón de ir atrás");

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
     * Método ejecutado cuando se pulsa el botón de esta actividad, para pasar a
     * la siguiente.
     *
     * @param view vista actual.
     */
    public void comenzarEntrenamiento(View view) {
        /* Comprobamos si está activado el GPS. De no estarlo, no dejaremos acceder a la siguiente
        actividad, y redirigiremos al usuario al menú de ajustes del GPS para que lo active. */
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("Main", "GPS no activo");

            // Llamamos a la función que nos pasa al menú del GPS.
            avisarGPSNoActivo();
        }
        else {
            Log.d("Main", "Pasamos a CalculationActivity");

            // Marcamos el intent con el lanzamiento de la próxima actividad (CalculationActivity).
            Intent calculationIntent = new Intent().setClass(
                    MainActivity.this, CalculationActivity.class);

            // Añadimos la variable tiempo_actualización, que se pasará como parámetro.
            calculationIntent.putExtra("t_act", tiempo_actualizacion);

            // Y comenzamos la siguiente actividad.
            startActivityForResult(calculationIntent, ENTRENAMIENTO);
        }
    }

    /**
     * Método: avisarGPSNoActivo
     * Indica al usuario que el GPS no está activo, y le pregunta si desea activarlo.
     */
    public void avisarGPSNoActivo() {
        // Construimos la alerta con el texto a mostrar.
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        /* En caso de pulsar sí, sólo nos redirigirá al menú del GPS para que lo activemos manualmente
        (ya que no se recomienda que una aplicación active el GPS por sí sola). En caso de pulsar no,
        no se modificará nada. Notar que debemos tener activado el GPS para pasar a la siguiente
        actividad, que captura los datos leídos del GPS. */
        builder.setMessage("Tiene el GPS desactivado. ¿Quiere activarlo?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });

        // Lo creamos y lo mostramos.
        final AlertDialog alert = builder.create();
        alert.show();

        Log.d("Main", "Pasamos a los ajustes del GPS");
    }

    /**
     * Método: onActivityResult
     * Método ejecutado cuando se vuelve a esta actividad. Su misión es borrar la base
     * de datos para empezar un nuevo entrenamiento.
     *
     * @param requestCode código que identifica a la actividad por la que se vuelve.
     * @param resultCode código que indica el resultado final.
     * @param data datos obtenidos como resultado.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Comprobamos el único caso posible y definido (podría extenderse de meter más opciones)
        if (requestCode == ENTRENAMIENTO) {
            Log.d("Main", "Hemos vuelto de la actividad lanzada");

             // Se reinicia la base de datos.
             BBDD baseDatos = new BBDD(getApplicationContext());
             baseDatos.borrarPosiciones();
             Log.d("Main", "Tabla en BBDD borrada al volver de actividad");
        }
    }
}
