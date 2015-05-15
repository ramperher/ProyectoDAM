package com.dam.proyectodam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

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
 * @version 1.0 final
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
            "(_id INTEGER PRIMARY key, latitud text, longitud text, distancia text, velocidad text, " +
            "instante text)";

    // Sentencia SQL para borrar la tabla de posiciones del mapa.
    private static final String DROP_LOCALIZACIONES="DROP TABLE IF EXISTS posiciones";

    // Identificador para ir guardando los puntos en orden.
    private static int id = 1;

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
     * @param localizacion location con la información del punto capturado.
     * @param distancia distancia al punto anterior.
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean insertarPosicion(Location localizacion, float distancia) {
        // Valor de comprobación de operaciones con la base de datos.
        long salida=0;

        // Llamamos a la base de datos.
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            // Insertamos los valores.
            ContentValues valores = new ContentValues();
            valores.put("_id", id);

            Log.d("BBDD", String.valueOf(id));
            Log.d("BBDD", "Nuevo punto: Latitud-> " +String.valueOf(localizacion.getLatitude()) + " Longitud->" + String.valueOf(localizacion.getLongitude())+
            " Distancia->" + String.valueOf(distancia) + " Velocidad:" +  String.valueOf(localizacion.getSpeed()) + " Tiempo->" + String.valueOf(localizacion.getTime()));

            valores.put("latitud", String.valueOf(localizacion.getLatitude()));
            valores.put("longitud", String.valueOf(localizacion.getLongitude()));
            valores.put("distancia", String.valueOf(distancia));
            valores.put("velocidad", String.valueOf((localizacion.getSpeed()*3.6)));
            valores.put("instante", String.valueOf((localizacion.getTime()/1000)));

            salida=db.insert("posiciones", null, valores);
            Log.d("BBDD", "Añadimos un valor a la BBDD, posición " + id);

            id++;
            // Cerramos la base de datos y devolvemos el booleano.
            db.close();
            }
        return(salida>0);
    }

    /**
     * Método: borrarPosicion
     * Borra la posición con el id que indiquemos como parámetro de entrada.
     * Da warning porque no se usa el método (se implementa por si es necesario en
     * próximas versiones).
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

            // Cerramos la base de datos.
            db.close();
        }

        // Y devolvemos el booleano.
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
            // Cerramos la base de datos y devolvemos el booleano.
            db.close();
        }
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
        ArrayList<Point> localizaciones = new ArrayList<>();

        // Valores a recuperar de la base de datos.
        String[] valores_recuperar = {"latitud", "longitud", "distancia", "velocidad", "instante"};

        if(db!=null) {
            /* Instanciamos la variable indice, iniciada a 1, para ordenar correctamente la lista
            de Point (que tienen un id como atributo. */
            int indice = 1;

            // Devolvemos todas las filas.
            Cursor c = db.query("posiciones", valores_recuperar, null, null, null, null, null, null);

            /* Y vamos leyendo de principio a fin. Debemos comprobar si el cursor devuelve filas,
            porque puede ocurrir que se cree y no devuelva ninguna fila (no se guardó ningún
            punto). */
            if (c != null && c.getCount() != 0) {
                c.moveToFirst();
                do {
                    localizaciones.add(new Point(indice, Double.valueOf(c.getString(0)), Double.valueOf(c.getString(1)), Float.valueOf(c.getString(2)), Double.valueOf(c.getString(3)), Long.valueOf(c.getString(4))));
                    Log.d("BBDD", "Punto extraido: Latitud-> " + c.getString(0) + " Longitud->" + c.getString(1) + " Distancia->" + c.getString(2) + " Velocidad->" + c.getString(3) + " Tiempo->" + c.getString(4));
                    indice++;
                } while(c.moveToNext());

                c.close();
            }

            Log.d("BBDD", "Puntos devueltos");

            db.close();
        }

        return localizaciones;
    }
}
