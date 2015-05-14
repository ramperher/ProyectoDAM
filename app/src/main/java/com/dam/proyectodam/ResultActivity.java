package com.dam.proyectodam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
 * @version 0.3 alfa
 *
 */
public class ResultActivity extends Activity {

    // TextView en los que mostrar los resultados finales.
    private TextView dist_rec;
    private TextView vel_media;
    private TextView duracion;

    // Base de datos de la aplicación.
    private BBDD baseDatos;

    // Datos de interés de la base de datos recuperados de CalculationActivity.
    private int puntosGuardados;
    private boolean sobreescribir;

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

        dist_rec = (TextView) findViewById(R.id.textorelleno4);
        vel_media = (TextView) findViewById(R.id.textorelleno5);
        duracion = (TextView) findViewById(R.id.textorelleno6);

        // Obtenemos el número de puntos guardados y si se sobreescribió o no la base de datos.
        Bundle bundle = getIntent().getExtras();
        //puntosGuardados = bundle.getInt("puntosGuardados");
        //sobreescribir = bundle.getBoolean("sobreescribir");

        /* Se inicia la base de datos y se devuelve el listado de puntos, que se
        pasa a mostrarDatos. */
        baseDatos=new BBDD(getApplicationContext());
        mostrarDatos(baseDatos.listarPosiciones());

        Log.d("Result", "Actividad preparada y datos mostrados");
    }

    /**
     * Método: mostrarDatos
     * Actualiza los valores de distancia recorrida y velocidad media mostrados por pantalla.
     *
     * @param listado array de objetos Point a partir del cual se realizan los cálculos.
     */
    public void mostrarDatos(ArrayList<Point> listado){
        // Declaramos los acumuladores.
        double d_recorrida = 0;
        double v_acumulada = 0;

        // También la velocidad media y duración del entrenamiento.
        double v_media;
        long duracion = 0;

        // Mostraremos resultados si hay algún punto en la lista.
        if (listado.size()>0) {
            for (int i = 0; i < listado.size(); i++) {
                d_recorrida += listado.get(i).getDistancia();
                v_acumulada += listado.get(i).getVelocidad();

                // Cálculo del tiempo: instante final-inicial.
                if (i == 0)
                    duracion = -listado.get(i).getInstante();
                if (i == listado.size()-1)
                    duracion += listado.get(i).getInstante();

            }
            /* La distancia sí es la suma, pero la velocidad es media; luego dividimos
            entre el número de puntos. */
            v_media = v_acumulada/listado.size();

            // Y los mostramos en los TextView.
            dist_rec.setText(Double.toString(d_recorrida));
            vel_media.setText(Double.toString(v_media));
            this.duracion.setText(Long.toString(duracion));
        }

        Log.d("Result", "Datos calculados y mostrados");
    }

    /**
     * Método: mostrarMapa
     * Método ejecutado cuando se pulsa el primer botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void mostrarMapa(View view) {
        Log.d("Result", "Pasamos a MapActivity");

        // Marcamos el intent con el lanzamiento de la próxima actividad (MapActivity).
        Intent mapIntent = new Intent(ResultActivity.this, MapActivity.class);

        // Añadimos el número de puntos leídos y sobreescribir para pasarlo a ResultActivity.
        mapIntent.putExtra("puntosGuardados", puntosGuardados);
        mapIntent.putExtra("sobreescribir", sobreescribir);

        startActivity(mapIntent);
    }

    /**
     * Método: volverMain
     * Método ejecutado cuando se pulsa el segundo botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void volverMain(View view) {
        Log.d("Result", "Volviendo a MainActivity");

        // Simplemente, cerramos la actividad, para volver al Main.
        finish();
    }
}