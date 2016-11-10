package com.example.kohki.tocostickapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Kohki on 2016/04/14.
 */
public class SensorDBHelper extends SQLiteOpenHelper {// コンストラクタ
    static final String DB_NAME = "sensor_data.db";   // DB名
    static final int DB_VERSION = 1;                // DBのVersion

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SensorDBContract.Sensordata.TABLE_NAME + " (" +
                    SensorDBContract.Sensordata._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_RECEIVE_TIME + TEXT_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_MEASURED_TIME + TEXT_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_TEMPERATURE + REAL_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_HUMIDITY + REAL_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_RADIATION + REAL_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_LIPO + REAL_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SensorDBContract.Sensordata.TABLE_NAME;

    public SensorDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //TODO:
    public SensorDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    // コンストラクタで指定したバージョンと、参照先のDBのバージョンに差異があるときにコールされる
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public static HashMap<String, String> getAllRow(SQLiteDatabase db) {
    //    SensorDBHelper mDbHelper = new SensorDBHelper(context);
    //    SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.Sensordata.TABLE_NAME, null);
        HashMap<String, String> row = new HashMap<>();
        try {
            if (cursor.moveToNext()) {
                row.put(SensorDBContract.Sensordata._ID,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata._ID)));
                row.put(SensorDBContract.Sensordata.COL_RECEIVE_TIME,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_RECEIVE_TIME)));
                row.put(SensorDBContract.Sensordata.COL_MEASURED_YEAR,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_YEAR)));
                row.put(SensorDBContract.Sensordata.COL_MEASURED_MONTH,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_MONTH)));
                row.put(SensorDBContract.Sensordata.COL_MEASURED_DATE,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_DATE)));
                row.put(SensorDBContract.Sensordata.COL_MEASURED_TIME,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_TIME)));
                row.put(SensorDBContract.Sensordata.COL_TEMPERATURE,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_TEMPERATURE)));
                row.put(SensorDBContract.Sensordata.COL_HUMIDITY,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_HUMIDITY)));
                row.put(SensorDBContract.Sensordata.COL_RADIATION,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_RADIATION)));
                row.put(SensorDBContract.Sensordata.COL_LIPO,
                        cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_LIPO)));
            }
        } finally {
            cursor.close();
        }
        return row;
    }
    public static HashMap<String, String> getRowFromTime(SQLiteDatabase db, String year, String month, String date, String time) {
        Cursor cursor = null;
        if(year != null && month != null && date != null && time != null) {
            cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.Sensordata.TABLE_NAME +" WHERE "+
                    SensorDBContract.Sensordata.COL_MEASURED_YEAR +" = ? and "+
                    SensorDBContract.Sensordata.COL_MEASURED_MONTH+" = ? and "+
                    SensorDBContract.Sensordata.COL_MEASURED_DATE +" = ? and "+
                    SensorDBContract.Sensordata.COL_MEASURED_TIME +" = ?",
                    new String[]{String.valueOf(year), String.valueOf(month),String.valueOf(date), String.valueOf(time)});
        }else if(year != null && month == null) {
            cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.Sensordata.TABLE_NAME +" WHERE "+
                            SensorDBContract.Sensordata.COL_MEASURED_YEAR +" = ?",new String[]{String.valueOf(year)});
        }else if(year != null && month != null && date == null) {
            cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.Sensordata.TABLE_NAME +" WHERE "+
                            SensorDBContract.Sensordata.COL_MEASURED_YEAR +" = ? and "+
                            SensorDBContract.Sensordata.COL_MEASURED_MONTH+" = ?",
                    new String[]{String.valueOf(year), String.valueOf(month)});
        }else if(year != null && month != null && date != null && time == null) {
            cursor = db.rawQuery("SELECT * FROM " + SensorDBContract.Sensordata.TABLE_NAME +" WHERE "+
                            SensorDBContract.Sensordata.COL_MEASURED_YEAR +" = ? and "+
                            SensorDBContract.Sensordata.COL_MEASURED_MONTH+" = ? and "+
                            SensorDBContract.Sensordata.COL_MEASURED_DATE +" = ?",
                    new String[]{String.valueOf(year), String.valueOf(month),String.valueOf(date), String.valueOf(time)});
        }
        if(cursor == null){
            Log.e("getRowFromTime","key is null");
            return null;
        }else {
            HashMap<String, String> row = new HashMap<>();
            try {
                if (cursor.moveToNext()) {
                    row.put(SensorDBContract.Sensordata._ID,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata._ID)));
                    row.put(SensorDBContract.Sensordata.COL_RECEIVE_TIME,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_RECEIVE_TIME)));
                    row.put(SensorDBContract.Sensordata.COL_MEASURED_YEAR,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_YEAR)));
                    row.put(SensorDBContract.Sensordata.COL_MEASURED_MONTH,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_MONTH)));
                    row.put(SensorDBContract.Sensordata.COL_MEASURED_DATE,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_DATE)));
                    row.put(SensorDBContract.Sensordata.COL_MEASURED_TIME,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_MEASURED_TIME)));
                    row.put(SensorDBContract.Sensordata.COL_TEMPERATURE,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_TEMPERATURE)));
                    row.put(SensorDBContract.Sensordata.COL_HUMIDITY,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_HUMIDITY)));
                    row.put(SensorDBContract.Sensordata.COL_RADIATION,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_RADIATION)));
                    row.put(SensorDBContract.Sensordata.COL_LIPO,
                            cursor.getString(cursor.getColumnIndex(SensorDBContract.Sensordata.COL_LIPO)));
                }
            } finally {
                cursor.close();
            }
            return row;
        }
    }
}