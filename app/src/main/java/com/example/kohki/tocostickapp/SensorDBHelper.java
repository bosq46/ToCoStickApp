package com.example.kohki.tocostickapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kohki on 2016/04/14.
 */
public class SensorDBHelper extends SQLiteOpenHelper {// コンストラクタ
        static final String DB_NAME = "sqlite_sample.db";   // DB名
        static final int DB_VERSION = 1;                // DBのVersion

        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String COMMA_SEP = ",";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SensorDBContract.Sensordata.TABLE_NAME + " (" +
                    SensorDBContract.Sensordata._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_RECEIVE_TIME + INT_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_TIME          + INT_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_TEMPERATURE  + INT_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_HUMIDITY     + INT_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_THERMISTOR   + TEXT_TYPE + COMMA_SEP +
                    SensorDBContract.Sensordata.COL_LIPO          + TEXT_TYPE + " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SensorDBContract.Sensordata.TABLE_NAME;

        public SensorDBHelper(Context context, String sql){
            super(context,DB_NAME,null,DB_VERSION);
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
    }