package com.example.kohki.tocostickapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kohki on 2016/04/14.
 */
public class SensorDBHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "sensor_data.db";
    static final int DB_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";

    private static final String COMMA_SEP = ",";

    private static final String SENSOR_SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SensorDBContract.SensorData.TABLE_NAME + " (" +
                    SensorDBContract.SensorData._ID + " INTEGER PRIMARY KEY"     + COMMA_SEP +
                    SensorDBContract.SensorData.COL_MEASURED_YEAR    + INT_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_MEASURED_MONTH   + INT_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_MEASURED_DATE    + INT_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_MEASURED_TIME    + TEXT_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_TEMPERATURE      + REAL_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_HUMIDITY         + REAL_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_RADIATION        + REAL_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_MOISTURE         + REAL_TYPE + COMMA_SEP +
                    SensorDBContract.SensorData.COL_CUMULATIVE_TEMP + REAL_TYPE + " )";

    private static final String VENTILATION_SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SensorDBContract.VentilationRec.TABLE_NAME + " (" +
                    SensorDBContract.VentilationRec._ID + " INTEGER PRIMARY KEY"     + COMMA_SEP +
                    SensorDBContract.VentilationRec.COL_VENTILATION      + TEXT_TYPE + COMMA_SEP +
                    SensorDBContract.VentilationRec.COL_RECOREDED_YEAR   + INT_TYPE + COMMA_SEP +
                    SensorDBContract.VentilationRec.COL_RECOREDED_MONTH  + INT_TYPE + COMMA_SEP +
                    SensorDBContract.VentilationRec.COL_RECOREDED_DATE   + INT_TYPE + COMMA_SEP +
                    SensorDBContract.VentilationRec.COL_RECOREDED_TIME   + TEXT_TYPE + " )";

    private static final String SENSOR_SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SensorDBContract.SensorData.TABLE_NAME;
    private static final String VENTILATION_SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SensorDBContract.VentilationRec.TABLE_NAME;

    public SensorDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //TODO:
    public SensorDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SENSOR_SQL_CREATE_ENTRIES);
        db.execSQL(VENTILATION_SQL_CREATE_ENTRIES);
    }

    // コンストラクタで指定したバージョンと、参照先のDBのバージョンに差異があるときにコールされる
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SENSOR_SQL_DELETE_ENTRIES);
        db.execSQL(VENTILATION_SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public static HashMap<String, String> getAllRow(SQLiteDatabase db) {
    //    SensorDBHelper mDbHelper = new SensorDBHelper(context);
    //    SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.SensorData.TABLE_NAME, null);
        HashMap<String, String> row = new HashMap<>();
        try {
            if (cursor.moveToNext()) {
                row.put(SensorDBContract.SensorData._ID,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData._ID)));
                row.put(SensorDBContract.SensorData.COL_MEASURED_YEAR,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_YEAR)));
                row.put(SensorDBContract.SensorData.COL_MEASURED_MONTH,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_MONTH)));
                row.put(SensorDBContract.SensorData.COL_MEASURED_DATE,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_DATE)));
                row.put(SensorDBContract.SensorData.COL_MEASURED_TIME,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_TIME)));
                row.put(SensorDBContract.SensorData.COL_TEMPERATURE,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_TEMPERATURE)));
                row.put(SensorDBContract.SensorData.COL_HUMIDITY,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_HUMIDITY)));
                row.put(SensorDBContract.SensorData.COL_RADIATION,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_RADIATION)));
                        row.put(SensorDBContract.SensorData.COL_MOISTURE,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MOISTURE)));
                row.put(SensorDBContract.SensorData.COL_CUMULATIVE_TEMP,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_CUMULATIVE_TEMP)));
            }
        } finally {
            cursor.close();
        }
        return row;
    }
    public static ArrayList<String> getRowFromDate(SQLiteDatabase db, String year, String month, String date) {
        Cursor cursor = null;
        if(year != null && month != null && date != null) {
            cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.SensorData.TABLE_NAME +" WHERE "+
                    SensorDBContract.SensorData.COL_MEASURED_YEAR +" = ? and "+
                    SensorDBContract.SensorData.COL_MEASURED_MONTH+" = ? and "+
                    SensorDBContract.SensorData.COL_MEASURED_DATE +" = ? ",
                    new String[]{String.valueOf(year), String.valueOf(month),String.valueOf(date)});
        }else if(year != null && month != null && date == null) {
            cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.SensorData.TABLE_NAME +" WHERE "+
                            SensorDBContract.SensorData.COL_MEASURED_YEAR +" = ? and "+
                            SensorDBContract.SensorData.COL_MEASURED_MONTH+" = ?",
                    new String[]{String.valueOf(year), String.valueOf(month)});
        }else if(year != null && month == null && date == null) {
            cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.SensorData.TABLE_NAME +" WHERE "+
                            SensorDBContract.SensorData.COL_MEASURED_YEAR +" = ?",
                    new String[]{String.valueOf(year)});
        }
        if(cursor == null){
            Log.e("getRowFromTime","key is null");
            return null;
        }else {
            ArrayList column = new ArrayList();
            HashMap<String, String> row = new HashMap<>();
            try {
                while (cursor.moveToNext()) {
                    row.put(SensorDBContract.SensorData._ID,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData._ID)));
                    row.put(SensorDBContract.SensorData.COL_MEASURED_YEAR,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_YEAR)));
                    row.put(SensorDBContract.SensorData.COL_MEASURED_MONTH,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_MONTH)));
                    row.put(SensorDBContract.SensorData.COL_MEASURED_DATE,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_DATE)));
                    row.put(SensorDBContract.SensorData.COL_MEASURED_TIME,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MEASURED_TIME)));
                    row.put(SensorDBContract.SensorData.COL_TEMPERATURE,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_TEMPERATURE)));
                    row.put(SensorDBContract.SensorData.COL_HUMIDITY,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_HUMIDITY)));
                    row.put(SensorDBContract.SensorData.COL_RADIATION,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_RADIATION)));
                    row.put(SensorDBContract.SensorData.COL_MOISTURE,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_MOISTURE)));
                    row.put(SensorDBContract.SensorData.COL_CUMULATIVE_TEMP,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.SensorData.COL_CUMULATIVE_TEMP)));
                    column.add(row);
                }
            } finally {
                cursor.close();
            }
            return column;
        }
    }
}