package com.findmystuff.pinpointplace.model;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.findmystuff.pinpointplace.utils.Constant;
import com.findmystuff.pinpointplace.utils.SQLLiteDatabaseHelper;
import com.findmystuff.pinpointplace.view.StuffListActivity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Greg on 27/12/2016.
 */

public class ApplicationModel implements Constant {

    public List<String> getAllStuffType(Context context){
        List<String> types = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY;

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                types.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning types
        return types;
    }

    public int getCategoryIdFromName(Context context, String name){
        int categoryId = -1;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY + " WHERE name = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { name });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                categoryId = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return categoryId;
    }

    public String getCategoryNameFromStuffName(Context context, String name) {
        String categoryName = null;

        // Select All Query
        String selectQuery = "SELECT  TC.name FROM " + TABLE_CATEGORY + " TC " +
                             "INNER JOIN " + TABLE_STUFF + " TS ON TC._id = TS.category_id " +
                             "AND TS.name = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { name });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                categoryName = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return categoryName;
    }


    public double getLatitudeFromStuffName(Context context, String stuffName) {
        double latitude = 0.0;
        // Select All Query
        String selectQuery = "SELECT  latitude FROM " + TABLE_STUFF + " WHERE name = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { stuffName });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                latitude = cursor.getDouble(0);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return latitude;
    }

    public double getLongitudeFromStuffName(Context context, String stuffName) {
        double longitude = 0.0;
        // Select All Query
        String selectQuery = "SELECT  longitude FROM " + TABLE_STUFF + " WHERE name = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { stuffName });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                longitude = cursor.getDouble(0);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return longitude;
    }

    public String getCategoryNameFromId(Context context, int id){
        String name = null;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY + " WHERE _id = "+ id;

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                name = cursor.getString(1);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return name;
    }

    public boolean checkIfStuffAlreadyExists(Context context, String name) {
        boolean exist = false;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STUFF + " WHERE name = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { name });

        if (cursor.getCount()  > 0){
            //cursor is not empty
            exist = true;
            // closing connection
            cursor.close();
        }

        return exist;
    }

    public boolean checkIfCategoryAlreadyExists(Context context, String name) {
        boolean exist = false;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY + " WHERE name = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { name });

        if (cursor.getCount()  > 0){
            //cursor is not empty
            exist = true;
            // closing connection
            cursor.close();
        }

        return exist;
    }

    public boolean checkIfAppHasCategories(Context context) {
        boolean exist = false;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY;

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount()  > 0){
            //cursor is not empty
            exist = true;
            // closing connection
            cursor.close();
        }

        return exist;
    }

    public void insertStuffToDatabase(Context context, String categoryName, String stuffName, double latitude, double longitude, String photoPath, byte[] thumbnailByte, String stuffDate, String description) {
        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();

        // Log.w("GJT"," BEGINNING STUFF INSERT !!!");
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(STUFF_COLUMN_NAME, stuffName);


        values.put(STUFF_COLUMN_LAT, latitude);
        values.put(STUFF_COLUMN_LONG, longitude);
        values.put(STUFF_COLUMN_IMG_PATH, photoPath);
        if (thumbnailByte != null)
            values.put(STUFF_COLUMN_IMG_THUMBNAIL, thumbnailByte);

        int categoryId = getCategoryIdFromName(context, categoryName);

        if (categoryId != -1)
            values.put(STUFF_CATEGORY_ID, categoryId);

        values.put(STUFF_COLUMN_DATE, stuffDate);

        if (description != null)
            values.put(STUFF_DESCRIPTION, description);

        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_STUFF, null, values);

        // Log.w("GJT"," STUFF INSERT SUCCESSFUL !!!");

    }

    public void insertCategoryToDatabase(Context context, String categoryName) {
        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();

        // Log.w("GJT"," BEGINNING CATEGORY INSERT !!!");
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CATEGORY_COLUMN_NAME, categoryName);

        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_CATEGORY, null, values);

        // Log.w("GJT"," CATEGORY INSERT SUCCESSFUL !!!");
    }


    public void updateStuffToDatabase(Context context, String categoryName, String oldStuffName, String newStuffName, String imagePath, byte [] thumbnailByte, String stuffDate, Double latitude, Double longitude, String description) {
        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();

        // Log.w("GJT"," BEGINNING STUFF INSERT !!!");
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(STUFF_COLUMN_NAME, newStuffName);

        int categoryId = getCategoryIdFromName(context, categoryName);

        if (categoryId != -1)
            values.put(STUFF_CATEGORY_ID, categoryId);

        if (imagePath != null)
            values.put(STUFF_COLUMN_IMG_PATH, imagePath);

        if (thumbnailByte != null)
            values.put(STUFF_COLUMN_IMG_THUMBNAIL, thumbnailByte);

        values.put(STUFF_COLUMN_DATE, stuffDate);

        if(latitude != null)
            values.put(STUFF_COLUMN_LAT, latitude);

        if(longitude != null)
            values.put(STUFF_COLUMN_LONG, longitude);

        if (description != null)
            values.put(STUFF_DESCRIPTION, description);

        db.update(TABLE_STUFF, values, "name=?", new String[]{oldStuffName});

        // Log.w("GJT"," STUFF UPDATE SUCCESSFUL !!!");

    }

    public void updateCategoryToDatabase(Context context, String oldCategoryName, String newCategoryName) {
        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();

        // Log.w("GJT"," BEGINNING CATEGORY UPDATE !!!");
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CATEGORY_COLUMN_NAME, newCategoryName);

        db.update(TABLE_CATEGORY, values, "name=?", new String[]{oldCategoryName});

        // Log.w("GJT"," STUFF UPDATE SUCCESSFUL !!!");

    }

    public String getImagePathFromStuffName(Context context, String stuffName) {
        String imagePath = null;

        // Select All Query
        String selectQuery = "SELECT " + STUFF_COLUMN_IMG_PATH + " FROM " + TABLE_STUFF + " WHERE " + STUFF_COLUMN_NAME + " = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { stuffName });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                imagePath = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return imagePath;
    }

    public Stuff getStuffFromName(Context context, String stuffName) {
        List<Stuff> lstStuff = new ArrayList<Stuff>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STUFF + " WHERE " + STUFF_COLUMN_NAME + " = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { stuffName });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                lstStuff.add(new Stuff(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getBlob(7), cursor.getString(8)));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return lstStuff.get(0);
    }

    public List<Object> getObjectListForListView(Context context, String query, String datePicked) {
        List<Object> lstObject = new ArrayList<Object>();
        List<Category> lstCategory = getCategoryList(context);
        List<Object> tmpList = new ArrayList<Object>();
        Date datePickedObject = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        // Select All Query
        String selectStuffQuery = "SELECT  * FROM " + TABLE_STUFF + " WHERE " + STUFF_CATEGORY_ID + " = ";
        String querySelect = null;

        if (query != null) {
            String likeQuery = "%" + query + "%";
            querySelect = " AND " + STUFF_COLUMN_NAME + " LIKE '" + likeQuery + "'";
        }

        if (datePicked != null) {
                try {
                    datePickedObject = sdf.parse(datePicked);
                } catch (ParseException e) {
                    Log.w("GJT", "Erreur de parsing de date");
                }
        }

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();

        if (lstCategory.size() > 0) {
            for (Category category :lstCategory) {
                Cursor cursor = null;
                String queryString = selectStuffQuery + category.getId();
                if (query != null)
                    queryString += querySelect;

                // Log.w("GJT", queryString);
                cursor = db.rawQuery(queryString, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    lstObject.add(category);
                    tmpList.add(category);
                    do {
                        Stuff stuff = new Stuff(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getBlob(7), cursor.getString(8));
                        Date dbDate = null;
                        try {
                            dbDate = sdf.parse(stuff.getDate());
                            if (datePickedObject != null) {
                                if (dbDate.getTime() >= datePickedObject.getTime()) {
                                    lstObject.add(stuff);
                                    tmpList.add(stuff);
                                }
                            } else {
                                lstObject.add(stuff);
                            }
                        } catch (ParseException e) {
                            Log.w("GJT", "Erreur de parsing de date");
                        }
                    } while (cursor.moveToNext());
                    if (datePickedObject != null && tmpList.size() == 1) {
                        lstObject.remove(category);
                        tmpList.clear();
                    }
                }

                // closing connection
                cursor.close();
            }
        }
        // Log.w("GJT", "Size : " + lstObject.size());
        return lstObject;
    }

    public Category getCategoryFromName(Context context, String categoryName) {
        List<Category> lstCategory = new ArrayList<Category>();

        // Select All Query

        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY + " WHERE "+ CATEGORY_COLUMN_NAME + "=?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { categoryName });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category(cursor.getInt(0), cursor.getString(1));
                lstCategory.add(category);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return lstCategory.get(0);
    }
    public List<Object> getObjectListForListViewFromCategoryName(Context context, String categoryName, String query, String datePicked) {
        List<Object> lstObject = new ArrayList<Object>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date datePickedObject = null;
        Category category = getCategoryFromName(context, categoryName);
        // Select All Query
        String selectStuffQuery = "SELECT  * FROM " + TABLE_STUFF + " WHERE " + STUFF_CATEGORY_ID + " = ";
        String querySelect =  null;
        if (query != null) {
            String likeQuery = "%" + query + "%";
            querySelect = " AND " + STUFF_COLUMN_NAME + " LIKE '" + likeQuery + "'";
        }

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();

        Cursor cursor = null;

        if (query != null)
            cursor = db.rawQuery(selectStuffQuery + category.getId() + querySelect, null);
        else
            cursor = db.rawQuery(selectStuffQuery + category.getId(), null);

        if (datePicked != null) {
            try {
                datePickedObject = sdf.parse(datePicked);
            } catch (ParseException e) {
                Log.w("GJT", "Erreur de parsing de date");
            }
        }

        // Log.w("GJT", selectStuffQuery);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            lstObject.add(category);
            do {
                Stuff stuff = new Stuff(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getBlob(7), cursor.getString(8));
                // Log.w("GJT", "Stuff " + stuff.getName() + ": " + stuff.getDate());
                Date dbDate = null;
                try {
                    dbDate = sdf.parse(stuff.getDate());
                    if (datePickedObject != null) {
                        if (dbDate.getTime() >= datePickedObject.getTime()) {
                            lstObject.add(stuff);
                        }
                    } else
                        lstObject.add(stuff);
                } catch (ParseException e) {
                    Log.w("GJT", "Erreur de parsing de date");
                }
            } while (cursor.moveToNext());
            if (lstObject.size() == 1)
                lstObject.remove(category);
        }

        // closing connection
        cursor.close();

        return lstObject;
    }

    public List<Object> filter(Context context, String stuffCategorySelected, String charText, String datePicked) {
        List<Object> objects = new ArrayList<Object>();
        charText = charText.toLowerCase(Locale.getDefault());
        // Log.w("GJT", "Requête " + charText);
        // Log.w("GJT", "Requête taille" + charText.length());
        if (charText.length() == 0) {
            // Log.w("GJT", "OK1");
            // Mettre à jour la listview avec la catégorie
            if (stuffCategorySelected != null) {
                if (!stuffCategorySelected.equals("Filtrer par catégorie   ")) {
                    // Log.w("GJT", "OK2");
                    objects = getObjectListForListViewFromCategoryName(context, stuffCategorySelected, null, datePicked);
                } else {
                    // Log.w("GJT", "OK3");
                    objects = getObjectListForListView(context, null, datePicked);
                }
            } else
                objects = getObjectListForListView(context, null, datePicked);
        } else {
            // Log.w("GJT", "OK1Bis");
            // Mettre à jour la listview avec la catégorie
            if (!stuffCategorySelected.equals("Filtrer par catégorie   ")) {
                // Log.w("GJT", "OK2Bis");
                objects = getObjectListForListViewFromCategoryName(context, stuffCategorySelected, charText, datePicked);
            } else
                objects = getObjectListForListView(context, charText, datePicked);
        }
        return objects;
    }


    public List<Stuff> filterMap(Context context, String stuffCategorySelected, String charText, String datePicked) {
        List<Stuff> lstStuff = new ArrayList<Stuff>();
        charText = charText.toLowerCase(Locale.getDefault());
        // Log.w("GJT", "Requête " + charText);
        // Log.w("GJT", "Requête taille" + charText.length());
        if (charText.length() == 0) {
            // Log.w("GJT", "OK1");
            // Mettre à jour la listview avec la catégorie
            if (stuffCategorySelected != null) {
                if (!stuffCategorySelected.equals("Filtrer par catégorie   ")) {
                    // Log.w("GJT", "OK2");
                    lstStuff = getStuffListFromCategoryName(context, stuffCategorySelected, null, datePicked);
                } else {
                    // Log.w("GJT", "OK3");
                    lstStuff = getStuffListForMap(context, null, datePicked);
                }
            } else
                lstStuff = getStuffListForMap(context, null, datePicked);
        } else {
            // Log.w("GJT", "OK1Bis");
            // Mettre à jour la listview avec la catégorie
            if (!stuffCategorySelected.equals("Filtrer par catégorie   ")) {
                // Log.w("GJT", "OK2Bis");
                lstStuff = getStuffListFromCategoryName(context, stuffCategorySelected, charText, datePicked);
            } else
                lstStuff = getStuffListForMap(context, charText, datePicked);
        }
        return lstStuff;
    }


    public List<Stuff> getStuffListFromCategoryName(Context context, String categoryName, String query, String datePicked) {
        List<Stuff> lstStuff = new ArrayList<Stuff>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date datePickedObject = null;
        Category category = getCategoryFromName(context, categoryName);
        // Select All Query
        String selectStuffQuery = "SELECT  * FROM " + TABLE_STUFF + " WHERE " + STUFF_CATEGORY_ID + " = ";
        String querySelect =  null;
        if (query != null) {
            String likeQuery = "%" + query + "%";
            querySelect = " AND " + STUFF_COLUMN_NAME + " LIKE '" + likeQuery + "'";
        }

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();

        Cursor cursor = null;

        if (query != null)
            cursor = db.rawQuery(selectStuffQuery + category.getId() + querySelect, null);
        else
            cursor = db.rawQuery(selectStuffQuery + category.getId(), null);

        if (datePicked != null) {
            try {
                datePickedObject = sdf.parse(datePicked);
            } catch (ParseException e) {
                // Log.w("GJT", "Erreur de parsing de date");
            }
        }

        // Log.w("GJT", selectStuffQuery);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Stuff stuff = new Stuff(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getBlob(7), cursor.getString(8));
                // Log.w("GJT", "Stuff " + stuff.getName() + ": " + stuff.getDate());
                Date dbDate = null;
                try {
                    dbDate = sdf.parse(stuff.getDate());
                    if (datePickedObject != null) {
                        if (dbDate.getTime() >= datePickedObject.getTime()) {
                            lstStuff.add(stuff);
                        }
                    } else
                        lstStuff.add(stuff);
                } catch (ParseException e) {
                    Log.w("GJT", "Erreur de parsing de date");
                }
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return lstStuff;
    }

    public List<Stuff> getStuffListForMap(Context context, String query, String datePicked) {
        List<Stuff> lstStuff = new ArrayList<Stuff>();
        List<Category> lstCategory = getCategoryList(context);
        List<Object> tmpList = new ArrayList<Object>();
        Date datePickedObject = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        // Select All Query
        String selectStuffQuery = "SELECT  * FROM " + TABLE_STUFF;
        String querySelect = null;

        if (query != null) {
            String likeQuery = "%" + query + "%";
            querySelect = " WHERE " + STUFF_COLUMN_NAME + " LIKE '" + likeQuery + "'";
        }

        if (datePicked != null) {
            try {
                datePickedObject = sdf.parse(datePicked);
            } catch (ParseException e) {
                Log.w("GJT", "Erreur de parsing de date");
            }
        }

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = null;
        String queryString = selectStuffQuery;
        if (query != null)
            queryString += querySelect;

        // Log.w("GJT", queryString);
        cursor = db.rawQuery(queryString, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Stuff stuff = new Stuff(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getBlob(7), cursor.getString(8));
                Date dbDate = null;
                try {
                    dbDate = sdf.parse(stuff.getDate());
                    if (datePickedObject != null) {
                        if (dbDate.getTime() >= datePickedObject.getTime()) {
                            lstStuff.add(stuff);
                        }
                    } else {
                        lstStuff.add(stuff);
                    }
                } catch (ParseException e) {
                    Log.w("GJT", "Erreur de parsing de date");
                }
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        return lstStuff;
    }

    public List<Stuff> getStuffList(Context context) {
        List<Stuff> lstStuff = new ArrayList<Stuff>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STUFF;

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Stuff stuff = new Stuff(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getBlob(7), cursor.getString(8));
                lstStuff.add(stuff);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return lstStuff;
    }

    public void deleteCategoryAndStuffAssociatedFromName(Context context, String name) {
        List<Stuff> lstStuff = getStuffAssociatedToCategory(context, name);
        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();

        if (lstStuff != null) {
            for(Stuff stuff : lstStuff) {
                if(stuff.getImagePath() != null) {
                    File existingfile = new File(stuff.getImagePath());
                    if (existingfile != null)
                        existingfile.delete();
                }
                db.delete(TABLE_STUFF, STUFF_COLUMN_NAME + " = ? ", new String[] {stuff.getName()});
            }
        }
        deleteCategoryFromName(context, name);
    }

    public List<Stuff> getStuffAssociatedToCategory(Context context, String name) {
        List<Stuff> lstStuff = new ArrayList<Stuff>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STUFF + " INNER JOIN " + TABLE_CATEGORY + " ON " + TABLE_STUFF + ".category_id = " + TABLE_CATEGORY  + "._id AND " + TABLE_CATEGORY + "." + CATEGORY_COLUMN_NAME + " = ?";

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] {name});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Stuff stuff = new Stuff(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getBlob(7), cursor.getString(8));
                lstStuff.add(stuff);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return lstStuff;

    }
    public void deleteStuffFromName(Context context, String name, String imagePathName) {
        File existingfile = new File(imagePathName);
        if (existingfile != null)
            existingfile.delete();

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();
        db.delete(TABLE_STUFF, STUFF_COLUMN_NAME + " = ? ", new String[] {name});
    }

    public List<Category> getCategoryList(Context context) {
        List<Category> lstCategory = new ArrayList<Category>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY;

        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category(cursor.getInt(0), cursor.getString(1));
                lstCategory.add(category);
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();

        return lstCategory;
    }


    public void deleteCategoryFromName(Context context, String name) {
        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();
        db.delete(TABLE_CATEGORY, CATEGORY_COLUMN_NAME + " = ? ", new String[] {name});
    }

    public void deleteAllStuffFromDatabase(Context context) {
        List<Stuff> stuffList = this.getStuffList(context);
        if (stuffList != null) {
            for(Stuff stuff : stuffList) {
                if(stuff.getImagePath() != null) {
                    File existingfile = new File(stuff.getImagePath());
                    if (existingfile != null)
                        existingfile.delete();
                }
            }
        }
        SQLiteDatabase db = SQLLiteDatabaseHelper.getInstance(context).getWritableDatabase();

        // Log.w("GJT"," BEGINNING ALL STUFF DELETE !!!");

        db.execSQL("DELETE FROM " + TABLE_STUFF);

        // Log.w("GJT"," ALL STUFF DELETED SUCCESSFUL !!!");
    }



}
