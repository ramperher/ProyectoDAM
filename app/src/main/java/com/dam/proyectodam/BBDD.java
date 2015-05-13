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
 * @version 0.4 alfa
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
    private static final String TABLA_LOCALIZACION="CREATE TABLE IF NOT EXISTS posiciones " +
            "(_id INTEGER PRIMARY KEY, latitud REAL, longitud REAL, distancia REAL, velocidad REAL, " +
            "instante INTEGER)";

    // Sentencia SQL para borrar la tabla de posiciones del mapa.
    private static final String DROP_LOCALIZACIONES="DROP TABLE IF EXISTS posiciones";

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
        Log.d("BBDD", "Actualización de la BBDD");
    }

    /**
     * Método: insertarPosicion
     * Añade una posición en la base de datos, con sus atributos característicos.
     *
     * @param sobreescribir booleano que indica si se está sobreescribiendo o no la tabla.
     * @param id identificador de la posición en la tabla.
     * @param latitud latitud del punto.
     * @param longitud longitud del punto.
     * @param distancia distancia entre este punto y el anterior.
     * @param velocidad velocidad alcanzada en este punto.
     * @param instante instante de tiempo de captura del punto.
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean insertarPosicion(boolean sobreescribir, int id, double latitud, double longitud,
                                    float distancia, double velocidad, long instante) {
        // Valor de comprobación de operaciones con la base de datos.
        long salida=0;

        // Llamamos a la base de datos.
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            // Insertamos los valores.
            ContentValues valores = new ContentValues();
            valores.put("_id", id);
            valores.put("latitud", latitud);
            valores.put("longitud", longitud);
            valores.put("distancia", distancia);
            valores.put("velocidad", velocidad);
            valores.put("instante", instante);

            // Comprobamos si estamos sobreescribiendo la BBDD por pasarnos del límite marcado.
            if (sobreescribir) {
                // Si se ha reiniciado el puntero de la BBDD, se han de actualizar valores, no introducirlos.
                salida=db.update("posiciones", valores, "_id=" + id, null);
                Log.d("BBDD", "Sobreescritura de la BBDD, posición " + id);
            }
            else {
                // En caso de funcionamiento normal, añadimos la posición a la tabla.
                salida=db.insert("posiciones", null, valores);
                Log.d("BBDD", "Añadimos un valor a la BBDD, posición " + id);
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
            salida=db.delete("posiciones", "_id=" + id, null);
            Log.d("BBDD", "Borramos un valor de la BBDD, posición " + id);
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
            salida=db.delete("posiciones", null, null);
            Log.d("BBDD", "Tabla de la BBDD vaciada");
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
     * @param id última posición en la que se escribió en la base de datos.
     * @param sobreescribir booleano que indica si se está sobreescribiendo o no la tabla.
     * @return una lista con todos los puntos guardados en la base de datos.
     */
    public ArrayList<Point> listarPosiciones(int id, boolean sobreescribir) {
        // Abrimos la base de datos, en modo lectura.
        SQLiteDatabase db = getReadableDatabase();

        // Se crea la lista de objetos Point donde se guardarán los datos de la BBDD.
        ArrayList<Point> localizaciones = new ArrayList<Point>();

        // Valores a recuperar de la base de datos.
        String[] valores_recuperar = {"latitud", "longitud", "distancia", "velocidad", "instante"};

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
                    Log.d("BBDD", "Punto sobreescrito, posición " + (id+indice-1));

                    // Añadimos el punto directamente, puesto que las estadísticas ya están calculadas.
                    localizaciones.add(new Point(indice, c.getDouble(1), c.getDouble(2), c.getFloat(3),
                            c.getDouble(4), c.getLong(5)));

                    // Y actualizamos el índice.
                    indice += 1;
                } while (c.moveToNext());
            }

            /* Aquí, leeremos hasta llegar a la fila id-1, que será la última a leer (última fila de la
            base de datos en caso de no haber sobreescritura, y último valor sobreescrito en caso contrario). */
            for (int puntero = 1; puntero < id; puntero++, indice++) {
                Log.d("BBDD", "Punto no sobreescrito, posición " + puntero);

                // Nos dirigimos a la fila que toque (le restamos 1, porque empieza en 0 el cursor).
                c.moveToPosition(puntero-1);

                // Y añadimos el punto directamente, ya que tenemos los datos correctos.
                localizaciones.add(new Point(indice, c.getDouble(1), c.getDouble(2), c.getFloat(3),
                        c.getDouble(4), c.getLong(5)));
            }
            c.close();
        }
        Log.d("BBDD", "Puntos devueltos");

        db.close();

        return localizaciones;
    }
}
