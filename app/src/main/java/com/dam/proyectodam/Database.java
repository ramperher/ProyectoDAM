package com.dam.proyectodam;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * Clase Database.java. Proyecto ARTrack. Diseño de Aplicaciones Móviles. 4º GITT.
 * Clase que conforma la base de datos que se utilizará en la aplicación, con
 * métodos de inserción, modificación, consulta y borrado de puntos.
 *
 * Link del repositorio (GitHub):
 *  https://github.com/ramperher/ProyectoDAM
 *
 * @author Ramón Pérez, Alberto Rodríguez
 * @version 0.1 alfa
 *
 */
public class Database extends SQLiteOpenHelper {

    /* Atributos de la clase = versión de la base de datos, nombre de la base de datos
    y sentencia para crear la tabla en la base de datos. */
    private static final int VERSION_DB = 1;
    private static final String NOMBRE_DB = "recorrido.db";
    private static final String TABLA_PUNTOS ="CREATE TABLE IF NOT EXISTS puntos " +
            " (_id INTEGER PRIMARY KEY, latitud REAL, longitud REAL, posicion REAL, " +
            " velocidad REAL, aceleracion REAL)";

    /**
     * Constructor de la clase Database
     *
     * @param context contexto de la base de datos.
     */
    public Database (Context context) {
        super(context, NOMBRE_DB, null, VERSION_DB);
    }

    /**
     * Método: onCreate
     * Crea la tabla en la base de datos.
     *
     * @param db manejador de la base de datos.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_PUNTOS);
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
        db.execSQL("DROP TABLE IF EXISTS" + TABLA_PUNTOS);
        onCreate(db);
    }

    /**
     * Método: insertarPunto
     * Añade un punto en la base de datos, con sus atributos característicos.
     *
     * @param id identificador único del punto (para la base de datos).
     * @param lat latitud del punto.
     * @param lon longitud del punto.
     * @param dist distancia recorrida entre este punto y el anterior.
     * @param vel velocidad dada entre este punto y el anterior.
     * @param acel aceleración dada entre este punto y el anterior.
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean insertarPunto(int id, double lat, double lon, double dist, double vel, double acel) {
        long salida=0;
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues valores = new ContentValues();

            if (id != 0)
                valores.put("_id", id);

            valores.put("latitud", lat);
            valores.put("longitud", lon);
            valores.put("distancia", dist);
            valores.put("velocidad", vel);
            valores.put("aceleracion", acel);

            salida=db.insert("puntos", null, valores);
        }
        db.close();
        return(salida>0);
    }

    /**
     * Método: modificarPunto
     * Modifica un punto de la base de datos. Sólo ocurrirá cuando lleguemos al límite
     * físico de la tabla, por lo que añadiremos los nuevos puntos a partir de la
     * primera entrada de la tabla, machacando los existentes.
     *
     * @param id identificador único del punto (para la base de datos).
     * @param lat latitud del punto.
     * @param lon longitud del punto.
     * @param dist distancia recorrida entre este punto y el anterior.
     * @param vel velocidad dada entre este punto y el anterior.
     * @param acel aceleración dada entre este punto y el anterior.
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean modificarPunto(int id, double lat, double lon, double dist, double vel, double acel) {
        long salida=0;
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("_id", id);
            valores.put("latitud", lat);
            valores.put("longitud", lon);
            valores.put("distancia", dist);
            valores.put("velocidad", vel);
            valores.put("aceleracion", acel);
            salida=db.update("puntos", valores, "_id=" + id, null);
        }
        db.close();
        return(salida>0);
    }

    /**
     * Método: recuperarPuntos
     * Recupera todos los puntos de la base de datos con formato en base a la
     * clase Point.
     *
     * @return una lista con todos los puntos guardados en la base de datos.
     */
    public ArrayList<Point> recuperarPuntos() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Point> lista_puntos = new ArrayList<Point>();
        String[] valores_recuperar = {"_id", "latitud", "longitud", "distancia", "velocidad", "aceleracion"};
        Cursor c = db.query("puntos", valores_recuperar, null, null, null, null, null, null);
        c.moveToFirst();
        do {
            Point punto = new Point(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getDouble(3), c.getDouble(4), c.getDouble(5));
            lista_puntos.add(punto);
        } while (c.moveToNext());
        db.close();
        c.close();
        return lista_puntos;
    }

    /**
     * Método: borrarPuntos
     * Borra el contenido de la tabla y elimina todos los puntos guardados, para
     * comenzar con un nuevo entrenamiento.
     *
     * @return un booleano que indica si el proceso se ejecutó correctamente o no.
     */
    public boolean borrarPuntos() {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("puntos", null, null);
        }
        db.close();
        return(salida>0);
    }
}
