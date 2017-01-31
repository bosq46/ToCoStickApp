package com.example.kohki.tocostickapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kohki on 2017/01/22.
 */

public class FileContract {
    private static final String TAG = "FileCont";
    static final String ASSETS_FILE = "BTVILOG1.CSV";
    static final String ID_FILE = "GatewayNodeID.csv";
    //every days
    public static String WIRELESS_DATA_FILE  = "wireless_data.CSV";
    public static String WEB_DATA_FILE        = "web_data.CSV";
    public static final String OUTSIDE_TENPERATURE = "outside_tem.CSV";
    //every months
    public static final String WIRELESS_EVERY_DAY_DATA_FILE   = "wireless_every_day_data.CSV";
    public static final String WEB_EVERY_DAY_DATA_FILE         = "web_every_day_data.CSV";
    public static final String OUTSIDE_EVERY_DAY_TENPERATURE = "outside_every_day_tem.CSV";
    public static final String VENTILATION_REC_FILE  = "ventilation_rec.CSV";
    private static final String PREFARENCE_FILE_NAME = "pre_web_data_file";
    private List mGatewayNodeID;

    private Context context;
    public FileContract(Context context){
        this.context = context;
        try {
            AssetManager assetManager = context.getResources().getAssets();
            InputStream is = assetManager.open(ID_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            mGatewayNodeID = new ArrayList();
            while ((line = reader.readLine()) != null) {
                Log.d(TAG,line);
                mGatewayNodeID.add(line.split(","));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public String getFieldName(){
        SharedPreferences prefer = context.getSharedPreferences(PREFARENCE_FILE_NAME,MODE_PRIVATE);
        String[] row = (String[])mGatewayNodeID.get(0);
        return prefer.getString("fieldname",row[1]);
    }
    public int getGateWayID(){
        SharedPreferences prefer = context.getSharedPreferences(PREFARENCE_FILE_NAME,MODE_PRIVATE);
        String[] row = (String[])mGatewayNodeID.get(0);
        return prefer.getInt("gatewayid",Integer.parseInt(row[1]));
    }
    public int getNodeID(){
        SharedPreferences prefer = context.getSharedPreferences(PREFARENCE_FILE_NAME,MODE_PRIVATE);
        String[] row = (String[])mGatewayNodeID.get(0);
        return prefer.getInt("nodeid",Integer.parseInt(row[2]));
    }
}
