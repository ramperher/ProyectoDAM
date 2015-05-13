package com.dam.proyectodam;

import android.os.Bundle;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 *
 * Clase MapActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Muestra el recorrido seguido durante el entrenamiento en un mapa.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.2 alfa
 *
 */
public class MapActivity extends FragmentActivity {
    // Mapa en el que mostrar las líneas del recorrido.
    private GoogleMap googleMap;

    // Base de datos de la aplicación.
    private BBDD baseDatos;

    // Datos de interés de la base de datos recuperados de CalculationActivity.
    private int puntosGuardados;
    private boolean sobreescribir;

    // Polilínea a mostrar en el mapa.
    PolylineOptions polilinea;

    // Punto de partida, para el zoom.
    LatLng puntoInicial;

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

        // Obtenemos el número de puntos guardados y si se sobreescribió o no la base de datos.
        Bundle bundle = getIntent().getExtras();
        puntosGuardados = bundle.getInt("puntosGuardados");
        sobreescribir = bundle.getBoolean("sobreescribir");

        /* Se inicia la base de datos y se devuelve el listado de puntos, para construir
        la polilínea. */
        baseDatos=new BBDD(getApplicationContext());
        construirPolyline(baseDatos.listarPosiciones(puntosGuardados, sobreescribir));

        // Y construimos el mapa, si no lo estaba ya.
        setUpMapIfNeeded();

        Log.d("Map", "Polilínea y mapa construidos");
    }

    /**
     * Método: construirPolyline
     * Método que construye una polilínea a partir de una lista de Point.
     *
     * @param puntos la lista de puntos del mapa.
     */
    private void construirPolyline(ArrayList<Point> puntos) {
        if (puntos.size() > 0) {
            // Instanciamos la polilínea.
            polilinea = new PolylineOptions();

            // Y vamos añadiendo latitud y longitud, conforme leemos la lista de puntos.
            for (int i = 0; i < puntos.size(); i++) {
                polilinea.add(new LatLng(puntos.get(i).getLatitud(), puntos.get(i).getLongitud()));

                // Si es el primer punto, lo añadimos a puntoInicial, para hacer el zoom en el mapa.
                if (i == 0)
                    puntoInicial = new LatLng(puntos.get(i).getLatitud(), puntos.get(i).getLongitud());
            }
            // Y añadimos otros atributos (grosor, color y marcar como geodésica).
            polilinea = polilinea.width(5);
            polilinea = polilinea.color(Color.BLUE);
            polilinea = polilinea.geodesic(true);

            Log.d("Map", "Polilínea construida");
        }
    }

    /**
     * Método: setUpMapIfNeeded
     * Método que establece el mapa si es necesario (si ya está creado, no se hace nada).
     */
    private void setUpMapIfNeeded() {
        // Comprobamos si ya teníamos el mapa.
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (googleMap != null) {
                // Y si está preparado, añadimos las líneas.
                addLines();
            }
        }
    }

    /**
     * Método: addLines
     * Método que añade las líneas al mapa a partir de los puntos de la base de datos.
     */
    private void addLines() {
        if (polilinea != null && puntoInicial != null) {
            // Añadimos las líneas al mapa.
            googleMap.addPolyline(polilinea);

            // Y con moveCamera, hacemos zoom en el punto inicial del recorrido.
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoInicial, 15));

            Log.d("Map", "Mapa con líneas añadidas");
        }
    }
}