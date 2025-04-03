package com.findmystuff.pinpointplace.utils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

/**
 * Created by Greg on 26/12/2016.
 */

public class SQLLiteDatabaseHelper  extends SQLiteOpenHelper implements Constant{

    private static final String DATABASE_NAME = "find_my_stuff.db";
    private static final int DATABASE_VERSION = 1;

    // Commande sql pour la création de la base de données
    private static final String TABLE_CATEGORY_CREATE = "create table "
            + TABLE_CATEGORY + "(" + CATEGORY_COLUMN_ID + " integer primary key autoincrement, "
            + CATEGORY_COLUMN_NAME + " text not null);";

    private static final String TABLE_STUFF_CREATE = "create table "
            + TABLE_STUFF + "(" + STUFF_COLUMN_ID + " integer primary key autoincrement, "
                                + STUFF_COLUMN_NAME  + " text not null, "
                                + STUFF_COLUMN_LAT + " real, "
                                + STUFF_COLUMN_LONG + " real, "
                                + STUFF_COLUMN_IMG_PATH + " char(50), "
                                + STUFF_CATEGORY_ID + " integer, "
                                + STUFF_DESCRIPTION + " text, "
                                + STUFF_COLUMN_IMG_THUMBNAIL + " blob, "
                                + STUFF_COLUMN_DATE + " char(50), "
                                + " FOREIGN KEY ("+STUFF_CATEGORY_ID+") REFERENCES "+TABLE_CATEGORY+"("+CATEGORY_COLUMN_ID+"));";
    private static final String DATABASE_CREATE = TABLE_CATEGORY_CREATE + TABLE_STUFF_CREATE;
    private static SQLLiteDatabaseHelper INSTANCE = null;

    /**
     * Récupération de la seule instance gérant la base de données dans l'application
     * @return
     */
    public static synchronized SQLLiteDatabaseHelper getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new SQLLiteDatabaseHelper(context);
        }

        return INSTANCE;
    }

    public SQLLiteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Insertion des différentes catégories d'objet de l'application
     */
    public static void insertCategoryEntries(SQLiteDatabase db) {
        // Log.w("GJT"," BEGINNING CATEGORY INSERT !!!");
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CATEGORY_COLUMN_NAME, "Personnel");
        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Professionnel");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Monuments");
        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Restaurants");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Bars");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Cinémas");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Spots");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Appartements Amis");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Anniversaires");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Parkings");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(CATEGORY_COLUMN_NAME, "Sport");
        db.insert(TABLE_CATEGORY, null, values);
        // Log.w("GJT"," CATEGORY INSERT SUCCESSFUL !!!");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Log.w("GJT", "DATABASE CREATION");
        // Gets the data repository in write mode
        db.execSQL(TABLE_CATEGORY_CREATE);
        db.execSQL(TABLE_STUFF_CREATE);
        insertCategoryEntries(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log.w("GJT","Upgrading database from version " + oldVersion + " to "+ newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUFF);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
