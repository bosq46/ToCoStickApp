package com.example.kohki.tocostickapp;

import android.provider.BaseColumns;

/**
 * Created by Kohki on 2016/09/30.
 */
public final class SensorDBContract {
    public SensorDBContract(){}

    public static abstract class Sensordata implements BaseColumns { //add _id
        public static final String TABLE_NAME         = "sensordata";
        public static final String COL_RECEIVE_TIME  = "receivetime";
        public static final String COL_MEASURED_YEAR = "measuredyear";
        public static final String COL_MEASURED_MONTH = "measuredmonth";
        public static final String COL_MEASURED_DATE = "measureddate";
        public static final String COL_MEASURED_TIME = "measuredtime";
        public static final String COL_TEMPERATURE   = "temperature";
        public static final String COL_HUMIDITY      = "humidity";
        public static final String COL_RADIATION     = "radiation";
        public static final String COL_LIPO           = "lipobattery";
    }
}
