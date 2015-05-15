package com.dam.proyectodam;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 *
 * Clase CalculationActivity.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase donde se leen los datos del GPS, se almacenan en la base de datos, y se muestran
 * los resultados del entrenamiento por cada intervalo de tiempo muestreado.
 * Adaptada con 2 layout distintos para orientación vertical y horizontal, guardando valores
 * de interés y recuperándolos al crearse de nuevo la actividad.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 1.0 final
 *
 */
public class CalculationActivity extends FragmentActivity {

    /* Distancia necesaria para actualizar los datos del GPS. Como no se va a usar, se
    fija a 0. */
    private static final int DISTANCIA_ACTUALIZACION = 0;

    /* Número de puntos con los que trabajará la aplicación como máximo (poniéndole un
    límite a la base de datos). El número equivale al número de segundos que hay en
    un año (luego, si actualizamos cada segundo, el entrenamiento duraría un año). */
    private static final int LIMITE_BBDD = 31536000;

    // Base de datos de la aplicación.
    private BBDD baseDatos;

    // Tiempo de actualización del GPS, que se modifica en onCreate (por eso no es final).
    private int tiempo_actualizacion = 0;

    // Última localización capturada, para cálculos de estadísticas.
    private Location ultima_localizacion;

    // Listener y Manager para gestionar la localización.
    private LocationListener locationListener;
    private LocationManager locationManager;

    // Número de puntos guardados en la base de datos.
    int puntosGuardados = 0;

    // Mapa en el que se muestra la posición actual.
    private GoogleMap mapa;

    // TextView en los que mostrar los resultados por cada intervalo.
    private TextView dist;
    private TextView vel;
    private TextView acel;

    // Booleano para saber si ya hemos añadido algún punto a la base de datos.
    private boolean BBDDusada = false;

    /* Atributos auxiliares de latitud, longitud, distancia, velocidad y estado
    de aceleración, para mostrarlos en caso de giro de pantalla y otros motivos
    de nueva creación de la actividad. */
    private double latitud;
    private double longitud;
    private String distancia = "-";
    private String velocidad = "-";
    private String est_aceleracion = "-";

    /**
     * Método: onCreate
     * Método ejecutado cuando se llama a la actividad.
     *
     * @param savedInstanceState instancia de la aplicación para recuperar datos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mostramos el layout de la actividad.
        setContentView(R.layout.activity_calculation);

        // Fijamos el tiempo de actualización con el valor pasado por MainActivity.
        Bundle bundle = getIntent().getExtras();
        tiempo_actualizacion = bundle.getInt("t_act");

        // Asociamos los TextView.
        dist = (TextView) findViewById(R.id.textorelleno1);
        vel = (TextView) findViewById(R.id.textorelleno2);
        acel = (TextView) findViewById(R.id.textorelleno3);

        // Establecemos el mapa.
        mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapCalc)).getMap();

        /* Ponemos los datos guardados en el mapa, los TextView y variables auxiliares.
        Único problema: se volverá a buscar una localización provisional, a configurar el listener
        para cambios, y a comenzar un entrenamiento con el primer punto con distancia 0, pero tendrá
        el resto de datos en la base de datos, y se mostrarán estos atributos. Tenerlo en cuenta. */
        if (savedInstanceState != null) {
            // Primero, restauramos los atributos.
            latitud = savedInstanceState.getDouble("latitud");
            longitud = savedInstanceState.getDouble("longitud");
            distancia = savedInstanceState.getString("distancia");
            velocidad = savedInstanceState.getString("velocidad");
            est_aceleracion = savedInstanceState.getString("est_aceleracion");
            tiempo_actualizacion = savedInstanceState.getInt("tiempo_actualizacion");
            puntosGuardados = savedInstanceState.getInt("puntosGuardados");

            if (puntosGuardados > 0) {
                // Sólo añadimos el punto si tenemos más de un punto guardado.
                mapa.clear();
                mapa.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud)).title("Posición actual"));
            }

            dist.setText(distancia);
            vel.setText(velocidad);
            acel.setText(est_aceleracion);

            Log.d("Calculation", "Atributos recuperados");
        }

        // También se inicia la base de datos.
        baseDatos=new BBDD(getApplicationContext());

        // Sólo borramos si entramos por primera vez.
        if (savedInstanceState == null) {
            baseDatos.borrarPosiciones();
            Log.d("Calculation", "Puntos borrados de la base de datos");
        }

        // E iniciamos la captura de la localización.
        iniciarGPS();

        Log.d("Calculation", "Actividad preparada");
    }

    /**
     * Método: onSaveInstanceState
     * Método que guarda los datos del último punto capturado, para mostrarlo por
     * pantalla en caso de que se vuelva a iniciar la actividad (por giro de pantalla,
     * por ejemplo).
     *
     * @param state estado a guardar.
     */
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Y guardamos los datos.
        state.putDouble("latitud", latitud);
        state.putDouble("longitud", longitud);
        state.putString("distancia", distancia);
        state.putString("velocidad", velocidad);
        state.putString("est_aceleracion", est_aceleracion);

        // Datos extra, aparte de la localización.
        state.putInt("tiempo_actualizacion", tiempo_actualizacion);
        state.putInt("puntosGuardados", puntosGuardados);

        Log.d("Main", "Guardados los valores de la última localización");
    }

    /**
     * Método: onDestroy
     * Método llamado antes de destruirse la actividad. Lo usamos para desactivar
     * la actualización de la localización, ya que puede ocurrir que pasemos a
     * ResultActivity, o que cambie la orientación o se reinicie la actividad. Si
     * no desactivamos la localización, el listener seguirá activo, y seguirá
     * guardando puntos. De esta forma, en caso de destrucción de la actividad,
     * desactivamos el listener, y que se vuelva a programar en caso de volver
     * a crearse la actividad.
     */
    @Override
    protected void onDestroy() {
        // Desactivamos la actualización de la localización.
        locationManager.removeUpdates(locationListener);
        Log.d("Calculation", "La actividad muere");

        super.onDestroy();
    }

    /**
     * Método: onBackPressed
     * Método ejecutado cuando se pulsa el botón atrás del terminal.
     *
     */
    @Override
    public void onBackPressed() {
        /* Lo único que hacemos es mostrar un Toast diciendo que no se
        puede volver atrás (vamos a impedir salir de esta actividad, a no
        ser que se pulse el botón). */
        Toast.makeText(getApplicationContext(),
                "GPS activo: no se puede retroceder.", Toast.LENGTH_LONG).show();

        Log.d("Calculation", "Pulsado botón de ir atrás");
    }

    /**
     * Método: finalizarEntrenamiento
     * Método ejecutado cuando se pulsa el botón de esta actividad.
     *
     * @param view vista actual.
     */
    public void finalizarEntrenamiento(View view) {
        // Lanzo un toast en caso de habernos pasado del límite de la BBDD (view será null).
        if (view == null) {
            Toast.makeText(getApplicationContext(),
                    "¿Cansado? Ha llegado al límite de la base de datos. Contacte con nosotros para " +
                            "recoger su medalla...", Toast.LENGTH_LONG).show();
        }

        Log.d("Calculation", "Paramos actualización de localización");
        Log.d("Calculation", "Pasamos a ResultActivity");

        // Marcamos el intent con el lanzamiento de la próxima actividad (ResultActivity).
        Intent resultIntent = new Intent(CalculationActivity.this, ResultActivity.class);
        startActivity(resultIntent);

        // Y cerramos la actividad para no volver a ella al pulsar atrás.
        finish();
    }

    /**
     * Método: iniciarGPS
     * Método que inicia la captura de la localización vía GPS.
     */
    public void iniciarGPS() {
        /* Instanciamos la referencia al gestor de localización del sistema. Tal y como se declara,
         el móvil usará tanto el GPS como las antenas de telefonía para posicionarse. */
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Última posición conocida, mientras se busca la actual (ésta no se guarda en la base de datos).
        ultima_localizacion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Objeto LocationListener, que actuará sólo cuando cambie la posición.
        locationListener = new LocationListener() {
            // Sólo actuaremos cuando cambie la posición.
            @Override
            public void onLocationChanged(Location location) {
                // Sólo trataremos con localizaciones no nulas.
                if (location != null) {
                /* Registraremos el cambio actualizando la posición en el mapa y guardándola en la
                base de datos, sólo si merece la pena actualizar la posición. Si no, se ignora y
                se espera a la siguiente. */
                    Log.d("Calculation", "Cambio en la localización");

                    if (isBetterLocation(location, ultima_localizacion)) {
                        Log.d("Calculation", "Cambio significativo en la localización");

                        // Si es el primer punto, mostramos un Toast diciendo que empieza el entrenamiento.
                        if (puntosGuardados == 0) {
                            Toast.makeText(getApplicationContext(),
                                    "Comienza el entrenamiento.", Toast.LENGTH_LONG).show();
                        }

                        // Obtenemos latitud y longitud, pasando a LatLng.
                        latitud = location.getLatitude();
                        longitud = location.getLongitude();
                        LatLng latLng = new LatLng(latitud, longitud);

                        // Añadimos un marcador, limpiando el que pusimos antes.
                        mapa.clear();
                        mapa.addMarker(new MarkerOptions().position(latLng).title("Posición actual"));

                        // Y fijamos el centro del mapa, añadiendo animaciones.
                        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        mapa.animateCamera(CameraUpdateFactory.zoomIn());
                        mapa.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

                        // Indicamos los cambios en los TextView, primero con la velocidad.
                        velocidad = Double.toString(location.getSpeed() * 3.6);
                        vel.setText(velocidad);

                    /* El estado de la aceleración puede ser acelerando, decelerando o velocidad constante,
                    dependiendo del resultado de la aceleración (que es vf-vo/tf-to). Aquí obviaremos el
                    tiempo, y nos limitaremos a la diferencia de velocidades (ya que tf-to siempre será >=0). */
                        if ((location.getSpeed() - ultima_localizacion.getSpeed()) > 0)
                            est_aceleracion = "Acelerando";
                        else if ((location.getSpeed() - ultima_localizacion.getSpeed()) < 0)
                            est_aceleracion = "Decelerando";
                        else
                            est_aceleracion = "Velocidad constante";

                        acel.setText(est_aceleracion);

                        /* La posición depende de si es el primer punto tomado o no.
                        Cuidado, porque puede ocurrir que se gire el terminal y se vuelva a entrar
                        aquí, registrando distancia 0 (cuando puede que no sea así). No se va a
                        guardar el atributo BBDDusada en caso de cambios, porque la distancia
                        se compararía con la última posición conocida (que se hace fuera del listener),
                        y no con la última posición con cambios significativos. Por eso, acumulamos
                        0 en la distancia. El error será menor conforme menos tarde el GPS en volver a
                        capturar posiciones correctas tras destruirse y crearse de nuevo la
                        actividad, y también conforme más puntos se capturen. */
                        if (!BBDDusada) {
                            // Distancia a 0.
                            distancia = "0";
                            dist.setText(distancia);

                            // Guardamos la posición en la base de datos con distancia 0.
                            baseDatos.insertarPosicion(location, 0);

                            // Y actualizamos a true, para que no vuelva a entrar aquí.
                            BBDDusada = true;
                        } else {
                            // Fijamos la distancia a su valor normal.
                            distancia = Float.toString(location.distanceTo(ultima_localizacion));

                            // Con esto, se guarda la posición en la base de datos.
                            baseDatos.insertarPosicion(location, location.distanceTo(ultima_localizacion));
                        }
                        dist.setText(distancia);

                        Log.d("Activity", "Punto mandado a guardar");

                    /* Aumentamos el número de puntos y comprobamos que no se ha llegado al límite
                    de puntos marcado. */
                        puntosGuardados++;
                        if (puntosGuardados == LIMITE_BBDD) {
                            // Hemos llegado al límite: debemos acabar el entrenamiento.
                            finalizarEntrenamiento(null);
                        }

                        // Y actualizamos la última posición capturada.
                        ultima_localizacion = location;
                    }
                }
            }

            // No hacemos nada en el resto de métodos.
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Calculation", "Estado cambiado.");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Calculation", "Proveedor activado.");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Calculation", "Proveedor desactivado.");
            }
        };
        /* Actualizamos la posición usando GPS (que llamaría a onLocationChanged). Se actualiza sólo
        con el tiempo (tiempo_actualizacion), no actuando la distancia. */
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tiempo_actualizacion,
                DISTANCIA_ACTUALIZACION, locationListener);
    }

    /**
     * Método: isBetterLocation
     * Método que determina si es conveniente usar la nueva localización o esperarse a otra.
     * Se basa en el tiempo de actualización. Se escribe el código tal y como se obtuvo.
     *
     * @param location la nueva localización que se quiere evaluar.
     * @param currentBestLocation la localización actual con la que se quiere comparar la nueva.
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // Una localización nueva es siempre mejor que no estar localizado.
            return true;
        }

        // Comprueba si la nueva localización es nueva o vieja.
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > tiempo_actualizacion;
        boolean isSignificantlyOlder = timeDelta < -tiempo_actualizacion;
        boolean isNewer = timeDelta > 0;

        /* Si han pasado más de 2 minutos desde la posición actual, se usa la nueva localización
        porque el usuario debe haberse movido. */
        if (isSignificantlyNewer) {
            return true;
            // Si la nueva posición es más antigua de 2 minutos, debe ser peor.
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Comprobamos si la nueva posición es más o menos exacta.
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Comprobamos si ambas localizaciones vienen del mismo proveedor.
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determinamos la calidad de la posición usando una combinación de oportunidad y precisión.
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Método: isSameProvider
     * Comprueba si los dos proveedores pasados como parámetros son iguales.
     * Se utiliza en el método isBetterLocation.
     *
     * @param provider1 primero de los proveedores.
     * @param provider2 segundo de los proveedores.
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }

        // Comprobamos si los dos proveedores son iguales.
        return provider1.equals(provider2);
    }
}
