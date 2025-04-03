package com.findmystuff.pinpointplace.utils;

/**
 * Created by Greg on 26/12/2016.
 */

public interface Constant {
    /**
     * STUFF table description
     */
    public static final String TABLE_STUFF = "stuff";
    public static final String STUFF_COLUMN_ID = "_id";
    public static final String STUFF_COLUMN_NAME = "name";
    public static final String STUFF_COLUMN_LAT = "latitude";
    public static final String STUFF_COLUMN_LONG = "longitude";
    public static final String STUFF_COLUMN_IMG_PATH = "image_path";
    public static final String STUFF_COLUMN_IMG_THUMBNAIL = "image_thumbnail";
    public static final String STUFF_CATEGORY_ID = "category_id";
    public static final String STUFF_DESCRIPTION = "description";
    public static final String STUFF_COLUMN_DATE = "stuff_date";


    /**
     * CATEGORY table description
     */
    public static final String TABLE_CATEGORY = "category";
    public static final String CATEGORY_COLUMN_ID = "_id";
    public static final String CATEGORY_COLUMN_NAME = "name";


    public static final int CAMERA_REQUEST = 24;
    public static final int LOCATION_REQUEST = 3;
}
