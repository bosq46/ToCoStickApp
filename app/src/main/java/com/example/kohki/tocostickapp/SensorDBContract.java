package com.example.kohki.tocostickapp;

import android.provider.BaseColumns;

/**
 * Created by Kohki on 2016/09/30.
 */
public final class SensorDBContract {
    public SensorDBContract(){}

    public static abstract class SensorData implements BaseColumns {
        public static final String TABLE_NAME           = "sensordata";
        public static final String COL_MEASURED_YEAR   = "measuredyear";
        public static final String COL_MEASURED_MONTH  = "measuredmonth";
        public static final String COL_MEASURED_DATE   = "measureddate";
        public static final String COL_MEASURED_TIME   = "measuredtime";
        public static final String COL_TEMPERATURE     = "temperature";
        public static final String COL_HUMIDITY        = "humidity";
        public static final String COL_RADIATION       = "radiation";
        public static final String COL_MOISTURE        = "moisture";
        public static final String COL_CUMULATIVE_TEMP = "cumulative_temp";
    }
    public static abstract class VentilationRec implements BaseColumns {
        public static final String TABLE_NAME           = "ventillationdata";
        public static final String COL_VENTILATION     = "ventilation";
        public static final String COL_RECOREDED_YEAR   = "recordedyear";
        public static final String COL_RECOREDED_MONTH  = "recordedmonth";
        public static final String COL_RECOREDED_DATE   = "recordeddate";
        public static final String COL_RECOREDED_TIME   = "recordedtime";
    }
}
