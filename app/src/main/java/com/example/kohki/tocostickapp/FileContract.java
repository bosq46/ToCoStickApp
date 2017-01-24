package com.example.kohki.tocostickapp;

/**
 * Created by Kohki on 2017/01/22.
 */

public interface FileContract {
    static final String ASSETS_FILE = "BTVILOG1.CSV";
    //every days
    public static final String WIRELESS_DATA_FILE  = "wireless_data.CSV";
    public static final String WEB_DATA_FILE        = "web_data.CSV";
    public static final String OUTSIDE_TENPERATURE = "outside_tem.CSV";
    //every months
    public static final String WIRELESS_EVERY_DAY_DATA_FILE   = "wireless_every_day_data.CSV";
    public static final String WEB_EVERY_DAY_DATA_FILE         = "web_every_day_data.CSV";
    public static final String OUTSIDE_EVERY_DAY_TENPERATURE = "outside_every_day_tem.CSV";
    public static final String VENTILATION_REC_FILE  = "ventilation_rec.CSV";
}
