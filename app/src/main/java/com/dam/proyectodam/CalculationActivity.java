package com.dam.proyectodam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

    //Interesa que esten aqui porque van a ser accedidas desde diferentes metodos.
    private BBDD baseDatos;
    private int TIEMPO_ACTUALIZACION = 0;                   // Se modifica en onCreate.
    private static final int DISTANCIA_ACTUALIZACION= 0;    // 0 metros (no afecta).
    Location ultima_localizacion;
    LocationListener listener;
    LocationManager locationManager;



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

        Bundle bundle = getIntent().getExtras();
        TIEMPO_ACTUALIZACION = bundle.getInt("t_act");
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
        locationManager.removeUpdates(listener);
        Intent resultIntent = new Intent(CalculationActivity.this, ResultActivity.class);
        startActivity(resultIntent);

        // Y cerramos la actividad para no volver a ella al pulsar atrás.
        finish();
    }

    //Metido qe inicia el GPS (y con el la BBDD).
    public void iniciarGPS(){
        // Referencia al gestor de localizacion del sistema.
        //Usando eso el movil usara tanto el GPS como las antenas de telefonia para posicionarse.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Tambien se inicia la base de datos.
        baseDatos=new BBDD(getApplicationContext());

        //Para no hacer esperar demasiado al usuario se le muestra la ultima localizacion conocida mientras se busca la actual.
        ultima_localizacion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //Este punto no veo necesario guardarlo en la bd.
        //INSERTAR AQUI PARA ENVIARLO AL MAPA.

        //Clase Locationlistener, llevar los metodos que son llamados al iniciar apagaga y actualizar los datos de posicion.
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Esto es lo principal, registrar cambios, actualizar posicion en el mapa y guardarla en la base de datos.
                //La linea siguiente deberia centrar el mapa en la posicion adquirica. Sustituir "mapa" por la referencia al mapa.
                // mapa.setCenter(new GeoPoint((int)location.getLatitude(),(int)location.getLongitude()))   //Cuando se añada la API del Maps


                //Comprobamos que merece la pena actualizar la posicion. Si no, se ignora y se espera a la siguiente.
                if(isBetterLocation(location, ultima_localizacion)){
                    //Con esto se guarda la posicion en la BD
                    baseDatos.insertarPosicion(location);
                }

                //Si se quiere usar la posicion sin pasar por la base de datos, hacerlo desde aqui.
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }


            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        //Actualizar usando GPS (lanzar LocationCahnged). Minimo cada 5 segundos o 10 metros.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIEMPO_ACTUALIZACION, DISTANCIA_ACTUALIZACION, listener );
    }


    //Logica molona que determina si es conveniente usar la nueva localizacion o esperarse a otra.
    //Mejorar para que tenga tambien en cuenta la distancia.

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIEMPO_ACTUALIZACION;
        boolean isSignificantlyOlder = timeDelta < -TIEMPO_ACTUALIZACION;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


}
