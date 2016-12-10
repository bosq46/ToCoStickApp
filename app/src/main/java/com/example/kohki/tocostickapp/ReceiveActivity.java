package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import jp.ksksue.driver.serial.FTDriver;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static android.R.attr.value;

/**
 * Created by Kohki on 2016/07/11.
 */

public class ReceiveActivity extends Activity {

    private static final String TAG = "ReceiveAct";
    private static  Context context;

    private ReceiveThreadHelper mReceiveThreadHelper;

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

        mHandler = new Handler();
        mReceiveThreadHelper = new ReceiveThreadHelper(this, strFileName, mHandler);
        mReceiveThreadHelper.start();
        setDataListView();//データファイルをビューに書き出す。
    }

    public static void setDataView(String str_data){
        final String log = str_data;
        mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
            @Override
            public void run() {
                tv_receivedData.setText(log);

            }
        });
    }
    public static void setDataListView(){
        final ListView data_list = lv_sensorData;
        mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
            @Override
            public void run() {
                String filelines = null;
                ArrayList<String> al_sensorData = new ArrayList<String>();
                try {
                    filelines = FileHelper.readAsStrFile(context, strFileName);
                } catch (Exception e) {
                    Toast.makeText(context,"E: "+e,Toast.LENGTH_SHORT).show();
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
                data_list.setAdapter(adapter);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(context, "mSerial.end();", Toast.LENGTH_SHORT).show();
            mReceiveThreadHelper.finish();
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }
}
