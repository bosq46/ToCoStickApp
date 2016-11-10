package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import jp.ksksue.driver.serial.FTDriver;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Kohki on 2016/07/11.
 */

public class ReceiveActivity extends Activity {

    private static final String TAG = "ReceiveAct";
    private static  Context context;

    public static boolean isMainLoopRunning = false;
    private ReceiveThreadHelper mReceiveThreadHelper;
    private DataAnalyzer    mDataAnalyzer;
    private static Handler  mHandler;//TODO;
    private static TextView tv_receivedData;
    private static ListView lv_sensorData;

    public static String strFileName = "bin_sensor_data.txt";
  //  public static String binFileName = "str_sensor_data.txt";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        context = this;
        tv_receivedData = (TextView) findViewById(R.id.receivedData);
        lv_sensorData   = (ListView) findViewById(R.id.dataList);

        mReceiveThreadHelper = new ReceiveThreadHelper(this);
        isMainLoopRunning = mReceiveThreadHelper.start();

        //   mHandler = new Handler();
        updateFileLinesView("");//データファイルをビューに書き出す。
    }

    public static void updateFileLinesView(String log){
        String filelines = null;
        ArrayList<String> al_sensorData = new ArrayList<String>();
        if(log != "" || log == null)
            tv_receivedData.setText(log);
        try {
            filelines = FileHelper.readAsStrFile(context, strFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filelines != null) {
            String[] arr_filelines = filelines.split("\n");
            for (String fileline : arr_filelines) {
                al_sensorData.add(fileline);//fileline = date +"\n"+ data
            }
        } else {
            Toast.makeText(context, "not found data file", Toast.LENGTH_SHORT).show();
        }
        Collections.reverse(al_sensorData);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                R.layout.list_layout, al_sensorData); //android.R.layout.simple_expandable_list_item_1
        lv_sensorData.setAdapter(adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(context, "mSerial.end();", Toast.LENGTH_SHORT).show();

            isMainLoopRunning = mReceiveThreadHelper.end();
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }
}
