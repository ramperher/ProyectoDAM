package com.dam.proyectodam;

import java.util.ArrayList;
import java.util.List;

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
 * @version 0.1 alfa
 *
 */
public class BBDD extends SQLiteOpenHelper {

    private static final int VERSION_BBDD=1;
    private static final String NOMBRE_BBDD="gps.db";
    private static final String TABLA_LOCALIZACION="CREATE TABLE IF NOT EXIST posiciones " +
            "(id INTEGER PRIMARY KEY, latitud TEXT, longitud TEXT, velocidad TEXT, tiempo TEXT)";
    private static final String DROP_LOCALIZACIONES="drop table if exist posiciones";
    private static int indiceMaximo=100;         //Numero de puntos con lo que trabajara la aplicacion.
    private static boolean sobreescribir=false;  //Indica que se ha superado el numero de pustos maximos en la BD.
    private static int id=0;                     //Indice de la base de datos.
    private static String sinVelocidad="0";      //Usado cuando no se dispone de la velocidad de movimiento.

    public BBDD (Context context){
        super(context, NOMBRE_BBDD, null, VERSION_BBDD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_LOCALIZACION);
        Log.d("BBDD", "Creacion de la BBDD()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_LOCALIZACIONES);
        db.execSQL(TABLA_LOCALIZACION);
    }

    public boolean insertarPosicion(Location posicion) {
        long salida=0;
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("_id", id);
            valores.put("latitud", posicion.convert(posicion.getLatitude(),Location.FORMAT_DEGREES));    //Como la BD no acepta Double, se guardaran como TEXT.
            valores.put("longitud", posicion.convert(posicion.getLongitude(), Location.FORMAT_DEGREES));
            valores.put("tiempo", posicion.convert(posicion.getTime(), Location.FORMAT_SECONDS));
            if(posicion.hasSpeed()){
                valores.put("velocidad", String.valueOf(posicion.getSpeed()));
            } else {
                valores.put("velocidad", sinVelocidad);
            }
            //BBDD nueva rellenandose.
            if(sobreescribir){
                //Si se ha reiniciado el puntero de la BD, se han de actualizar valores, no introducirlos.
                salida=db.update("posiciones", valores, "id=" + id,null);
            } else{
                salida=db.insert("posiciones", null, valores);
            }
            if(id<indiceMaximo){
                id=id+1;            //Actualizamos al indice para en siguiente dato.
            } else {
                id=0;
                sobreescribir=true;
            }
        }
        db.close();
        return(salida>0);
    }

    public boolean  borrarLocalizacion(int id) {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("contactos", "_id=" + id, null);
        }
        db.close();
        return(salida>0);
    }

    public ArrayList<Point> listaPosiciones(){
        SQLiteDatabase db = getReadableDatabase();
        //Se leen todas las posiciones existentes en la BBDD, y se crean los objetos point donde devolverlas.
        ArrayList<Point> localizaciones= new ArrayList<Point>();
        String[] valores_recuperar = {"latitud", "longitud", "velocidad", "tiempo"};
        int puntero=id;
        int indice=0;
        if(db!=null){
            Cursor c=db.query("posiciones", valores_recuperar, "id=" + id, null, null, null, null, null);
            c.move(puntero);
            do{
                Point punto;
                Location localizacion=new Location("puntoA");
                localizacion.setLatitude(Location.convert(c.getString(0)));
                localizacion.setLongitude(Location.convert(c.getString(2)));
                localizacion.setSpeed(Float.parseFloat(c.getString(3)));
                localizacion.setTime(Long.parseLong(c.getString(4)));
                if(indice==0){
                    //Si el el primer punto, no puede sacarse una distancia recorrida ni una aceleracion.
                    punto=new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),0,localizacion.getSpeed(),0);
                } else {
                    //Se necesita acceder al punto anterior para conseguir la distancia y la aceleracion.
                    c.moveToPrevious();
                    Location localizacion2=new Location("puntoB");
                    localizacion2.setLatitude(Location.convert(c.getString(0)));
                    localizacion2.setLongitude(Location.convert(c.getString(2)));
                    localizacion2.setSpeed(Float.parseFloat(c.getString(3)));
                    localizacion2.setTime(Long.parseLong(c.getString(4)));
                    punto=new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),
                            localizacion.distanceTo(localizacion2),localizacion.getSpeed(),
                            (double)((localizacion.getSpeed()-localizacion2.getSpeed())/(localizacion2.getTime()-localizacion.getTime())));
                            //Formula comun de la aceleracion: A=(V1-V0)/T
                    c.moveToNext(); //Se vuelve a situar el cursor donde estaba.
                }
                indice=indice +1;
                localizaciones.add(punto);
            } while (c.moveToNext());

            //Tambien es necesario anadir los puntos que se llevan sobreescritos en la pasada actual por la BD.
            for (puntero=0; puntero<id; puntero++ ){
                c.move(puntero);
                Point punto;
                if(puntero==0) {
                    Location localizacion = new Location("puntoA");
                    localizacion.setLatitude(Location.convert(c.getString(0)));
                    localizacion.setLongitude(Location.convert(c.getString(2)));
                    localizacion.setSpeed(Float.parseFloat(c.getString(3)));
                    localizacion.setTime(Long.parseLong(c.getString(4)));
                    if(id!=0){
                        c.moveToLast();
                        Location localizacion2=new Location("puntoB");
                        localizacion2.setLatitude(Location.convert(c.getString(0)));
                        localizacion2.setLongitude(Location.convert(c.getString(2)));
                        localizacion2.setSpeed(Float.parseFloat(c.getString(3)));
                        localizacion2.setTime(Long.parseLong(c.getString(4)));
                        punto=new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),
                                localizacion.distanceTo(localizacion2),localizacion.getSpeed(),
                                (double)((localizacion.getSpeed()-localizacion2.getSpeed())/(localizacion2.getTime()-localizacion.getTime())));
                        //Formula comun de la aceleracion: A=(V1-V0)/T
                    } else {
                        punto=new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),0,localizacion.getSpeed(),0);
                    }
                    c.move(puntero);
                } else {
                    Location localizacion = new Location("puntoA");
                    localizacion.setLatitude(Location.convert(c.getString(0)));
                    localizacion.setLongitude(Location.convert(c.getString(2)));
                    localizacion.setSpeed(Float.parseFloat(c.getString(3)));
                    localizacion.setTime(Long.parseLong(c.getString(4)));

                    Location localizacion2=new Location("puntoB");
                    localizacion2.setLatitude(Location.convert(c.getString(0)));
                    localizacion2.setLongitude(Location.convert(c.getString(2)));
                    localizacion2.setSpeed(Float.parseFloat(c.getString(3)));
                    localizacion2.setTime(Long.parseLong(c.getString(4)));

                    punto=new Point(indice, localizacion.getLatitude(),localizacion.getLongitude(),
                            localizacion.distanceTo(localizacion2),localizacion.getSpeed(),
                            (double)((localizacion.getSpeed()-localizacion2.getSpeed())/(localizacion2.getTime()-localizacion.getTime())));
                    //Formula comun de la aceleracion: A=(V1-V0)/T
                }
                localizaciones.add(punto);
            }
            db.close();
            c.close();
        }
        return localizaciones;
    }
}
