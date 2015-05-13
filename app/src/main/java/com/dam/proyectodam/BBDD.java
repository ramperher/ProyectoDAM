package com.dam.proyectodam;

import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

/**
 *
 * Clase BBDD.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase que conforma la base de datos que se utilizará en la aplicación, con
 * métodos de inserción, modificación, consulta y borrado de puntos.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.3 alfa
 *
 */
public class BBDD extends SQLiteOpenHelper {

    // Versión de la base de datos.
    private static final int VERSION_BBDD = 1;

    // Nombre de la base de datos.
    private static final String NOMBRE_BBDD = "gps.db";

    /* Sentencia SQL para crear la tabla de posiciones del mapa.
    En la base de datos, se guardará:
    -Identificador, para clave primaria.
    -Latitud y longitud del punto.
    -Velocidad alcanzada en ese punto.
    -Instante de captura de la posición.
    La distancia de cada intervalo se calcula a partir de estos datos. */
    private static final String TABLA_LOCALIZACION="CREATE TABLE IF NOT EXIST posiciones " +
            "(_id INTEGER PRIMARY KEY, latitud TEXT, longitud TEXT, velocidad TEXT, tiempo TEXT)";

    // Sentencia SQL para borrar la tabla de posiciones del mapa.
    private static final String DROP_LOCALIZACIONES="DROP TABLE IF EXIST posiciones";

    /* Número de puntos con los que trabajará la aplicación como máximo (poniéndole un
    límite a la base de datos). */
    private static final int indiceMaximo = 100;

    // Usado cuando no se dispone de la velocidad de movimiento.
    private static final String sinVelocidad="0";

    // Indica que se ha superado el número de puntos máximos en la base de datos.
    private boolean sobreescribir = false;

    // Índice de la base de datos (partimos de 1 y se va modificando).
    private int id = 1;

    /**
     * Constructor de la clase BBDD
     *
     * @param context contexto de la base de datos.
     */
    public BBDD (Context context){
        super(context, NOMBRE_BBDD, null, VERSION_BBDD);
    }

    /**
     * Método: onCreate
     * Crea la tabla en la base de datos.
     *
     * @param db manejador de la base de datos.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_LOCALIZACION);
        Log.d("BBDD", "Creación de la BBDD");
    }

    /**
     * Método: onUpgrade
     * Actualiza la tabla en la base de datos, borrando y creándola de nuevo.
     *
     * @param db manejador de la base de datos.
     * @param oldVersion antigua versión.
     * @param newVersion nueva versión.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_LOCALIZACIONES);
        db.execSQL(TABLA_LOCALIZACION);
    }

    /**
     * Método: insertarPosicion
     * Añade una posición en la base de datos, con sus atributos característicos.
     *
     * @param posicion objeto Location con la información de la posición a introducir.
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean insertarPosicion(Location posicion) {
        // Valor de comprobación de operaciones con la base de datos.
        long salida=0;

        // Llamamos a la base de datos.
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            // Insertamos los valores a partir del Location.
            ContentValues valores = new ContentValues();
            valores.put("_id", id);

            // Convertimos latitud, longitud y tiempo (pasando de ms a s) en String.
            valores.put("latitud", posicion.convert(posicion.getLatitude(),Location.FORMAT_DEGREES));
            valores.put("longitud", posicion.convert(posicion.getLongitude(), Location.FORMAT_DEGREES));
            valores.put("tiempo", posicion.convert(posicion.getTime()*1000, Location.FORMAT_SECONDS));

            /* Comprobamos si podemos obtener datos de la velocidad para este punto (si no, ponemos 0).
            Se convierte, también, a km/h. */
            if (posicion.hasSpeed())
                valores.put("velocidad", String.valueOf(posicion.getSpeed()*3.6));
            else
                valores.put("velocidad", sinVelocidad);

            // Comprobamos si estamos sobreescribiendo la BBDD por pasarnos del límite marcado.
            if (sobreescribir) {
                // Si se ha reiniciado el puntero de la BBDD, se han de actualizar valores, no introducirlos.
                salida=db.update("posiciones", valores, "_id=" + id, null);
            }
            else {
                // En caso de funcionamiento normal, añadimos la posición a la tabla.
                salida=db.insert("posiciones", null, valores);
            }

            // Y comprobamos si nos salimos del índice máximo de la tabla.
            if (id < indiceMaximo) {
                // De no salirnos, incrementamos el índice.
                id += 1;
            }
            else
            {
                // Activamos sobreescritura.
                id = 1;
                sobreescribir = true;
            }
        }
        // Cerramos la base de datos y devolvemos el booleano.
        db.close();
        return(salida>0);
    }

    /**
     * Método: borrarPosicion
     * Borra la posición con el id que indiquemos como parámetro de entrada.
     *
     * @param id identificador de la posición en la tabla.
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean borrarPosicion(int id) {
        // Abrimos la base de datos.
        SQLiteDatabase db = getWritableDatabase();

        // Valor a devolver, tras hacer el delete.
        long salida=0;
        if (db != null) {
            // Borramos la entrada de la tabla.
            salida=db.delete("contactos", "_id=" + id, null);
        }
        // Cerramos la base de datos y devolvemos el booleano.
        db.close();
        return(salida>0);
    }

    /**
     * Método: borrarPosiciones
     * Borra el contenido de la tabla y elimina todas las posiciones guardadas, para
     * comenzar con un nuevo entrenamiento.
     *
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean borrarPosiciones() {
        // Abrimos la base de datos.
        SQLiteDatabase db = getWritableDatabase();

        // Valor a devolver, tras hacer el delete.
        long salida=0;
        if (db != null) {
            // Vaciamos la tabla.
            salida=db.delete("contactos", null, null);
        }
        // Cerramos la base de datos y devolvemos el booleano.
        db.close();
        return(salida>0);
    }

    /**
     * Método: listarPosiciones
     * Recupera todos los puntos de la base de datos con formato en base a la
     * clase Point.
     *
     * @return una lista con todos los puntos guardados en la base de datos.
     */
    public ArrayList<Point> listarPosiciones() {
        // Abrimos la base de datos, en modo lectura.
        SQLiteDatabase db = getReadableDatabase();

        // Se crea la lista de objetos Point donde se guardarán los datos de la BBDD.
        ArrayList<Point> localizaciones = new ArrayList<Point>();

        // Valores a recuperar de la base de datos.
        String[] valores_recuperar = {"latitud", "longitud", "velocidad", "tiempo"};

        if(db!=null) {
            /* Instanciamos la variable indice, iniciada a 1, para ordenar correctamente la lista
            de Point (que tienen un id como atributo. */
            int indice = 1;

            // Devolvemos todas las filas.
            Cursor c = db.query("posiciones", valores_recuperar, null, null, null, null, null, null);

            /* Si estamos en sobreescritura, tomamos como primer punto la fila referenciada con el índice
            id (que es el siguiente a sobreescribir, luego es el primer punto para nosotros). */
            if (sobreescribir) {
                /* Nos colocamos en la posición marcada por id (debe ser id-1, porque el primer índice
                del cursor es el 0, no el 1. */
                c.moveToPosition(id-1);

                // Leemos todas las filas que quedan, hasta el final.
                do {
                    // Declaramos el punto.
                    Point punto;

                    // Creamos el objeto Location, y lo construimos con los valores guardados en la BBDD.
                    Location localizacion = new Location("punto");
                    localizacion.setLatitude(Location.convert(c.getString(1)));
                    localizacion.setLongitude(Location.convert(c.getString(2)));
                    localizacion.setSpeed(Float.parseFloat(c.getString(3)));
                    localizacion.setTime(Long.parseLong(c.getString(4)));

                    /* De ser el primer punto, no podemos sacar una distancia recorrida, ya que se basa
                    en el punto anterior. Por eso, lo fijamos a 0, pero guardando el resto de valores en el
                    punto antes declarado. */
                    if (indice == 1) {
                        punto = new Point(indice, localizacion.getLatitude(), localizacion.getLongitude(), 0,
                                localizacion.getSpeed());
                    }
                    else {
                        /* Si no es el primer punto, necesitamos acceder al punto anterior para conseguir
                        la distancia. */
                        c.moveToPrevious();
                        Location localizacion_ant = new Location("punto_ant");
                        localizacion_ant.setLatitude(Location.convert(c.getString(1)));
                        localizacion_ant.setLongitude(Location.convert(c.getString(2)));
                        localizacion_ant.setSpeed(Float.parseFloat(c.getString(3)));
                        localizacion_ant.setTime(Long.parseLong(c.getString(4)));

                        // Y calculamos la distancia con distanceTo
                        punto = new Point(indice, localizacion.getLatitude(), localizacion.getLongitude(),
                                localizacion.distanceTo(localizacion_ant), localizacion.getSpeed());

                        // Por último, volvemos a poner el cursor donde estaba.
                        c.moveToNext();
                    }
                    // Actualizamos el índice.
                    indice += 1;

                    // Y añadimos el punto a la lista.
                    localizaciones.add(punto);

                } while (c.moveToNext());
            }

            /* Aquí, leeremos hasta llegar a la fila id-1, que será la última a leer (última fila de la
            base de datos en caso de no haber sobreescritura, y último valor sobreescrito en caso contrario). */
            for (int puntero = 1; puntero < id; puntero++) {
                // Nos dirigimos a la fila que toque (le restamos 1, porque empieza en 0 el cursor).
                c.moveToPosition(puntero-1);

                // Declaramos el punto.
                Point punto;

                // Y realizamos el mismo tratamiento que antes: tomamos el punto y lo guardamos en un Location.
                Location localizacion = new Location("punto");
                localizacion.setLatitude(Location.convert(c.getString(1)));
                localizacion.setLongitude(Location.convert(c.getString(2)));
                localizacion.setSpeed(Float.parseFloat(c.getString(3)));
                localizacion.setTime(Long.parseLong(c.getString(4)));

                /* Si es la primera posición, debemos comprobar si este punto es producto o no de una
                sobreescritura. */
                if (puntero == 1) {
                    if (sobreescribir) {
                        /* Caso de sobreescritura. Para obtener la distancia, tomamos los
                        datos del punto anterior, que está en la última fila. */
                        c.moveToLast();
                        Location localizacion_ant=new Location("punto_ant");
                        localizacion_ant.setLatitude(Location.convert(c.getString(1)));
                        localizacion_ant.setLongitude(Location.convert(c.getString(2)));
                        localizacion_ant.setSpeed(Float.parseFloat(c.getString(3)));
                        localizacion_ant.setTime(Long.parseLong(c.getString(4)));
                        punto = new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),
                                localizacion.distanceTo(localizacion_ant),localizacion.getSpeed());
                    }
                    else {
                        // Caso de no sobreescritura: es el primer punto, luego la distancia vale 0.
                        punto = new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),0,localizacion.getSpeed());
                    }
                }
                else {
                    // No es la primera posición: leemos el punto anterior.
                    c.moveToPrevious();
                    Location localizacion_ant=new Location("punto_ant");
                    localizacion_ant.setLatitude(Location.convert(c.getString(1)));
                    localizacion_ant.setLongitude(Location.convert(c.getString(2)));
                    localizacion_ant.setSpeed(Float.parseFloat(c.getString(3)));
                    localizacion_ant.setTime(Long.parseLong(c.getString(4)));

                    punto=new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),
                            localizacion.distanceTo(localizacion_ant),localizacion.getSpeed());
                }
                // Al final, añadimos el punto a la lista.
                localizaciones.add(punto);
            }
            c.close();
        }
        db.close();

        return localizaciones;
    }
}
