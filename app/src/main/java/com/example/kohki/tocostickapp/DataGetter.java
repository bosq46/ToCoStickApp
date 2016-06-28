package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.ksksue.driver.serial.FTDriver;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;

import android.widget.ListView;
/**
 * Created by Kohki on 2016/02/25.
 */
public class DataGetter extends Activity{

    FTDriver mSerial;
    private static final String ACTION_USB_PERMISSION =  "jp.ksksue.tutorial.USB_PERMISSION";//"com.example.kohki.USB_PERMISSION";
    private boolean isMainLoopRunning = false;
    String TAG = "TWE_Line";
    Handler mHandler;
    final int SERIAL_BAUDRATE = FTDriver.BAUD9600;
    private boolean mStop = false;

    private FileHandler fileHandler_;
    private Context context_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_getter);

        mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));
        // [FTDriver] setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);

        if(mSerial.begin(SERIAL_BAUDRATE)) {
            startThread();
            Toast.makeText(this, "serial connection begin", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "serial connection failed", Toast.LENGTH_SHORT).show();
        }
        context_ = this.getApplicationContext();
        mHandler = new Handler();
        fileHandler_ = new FileHandler("sensor_data.txt", this.getApplicationContext());
        updateFileLinesView();//データファイルをビューに書き出す。
    }

    //Thread start
    private void startThread() {
        isMainLoopRunning = true;
        new Thread(mLoop).start();
    }
    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int i,len;
            // [FTDriver] Create Read Buffer
            final byte[] rbuf = new byte[5];   // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
            final TextView tv_receivedData = (TextView) findViewById(R.id.receivedData);

            do{
                // [FTDriver] Read from USB Serial
                len = mSerial.read(rbuf);
//                byte[] bytes_message = new byte[len];
                if(len > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String received_data = "";
                            StringBuilder sb = new StringBuilder(2*rbuf.length);

                            int i=0;
                            for (byte b : rbuf) {
                                sb.append(String.format("%02x", b & 0xff));
                                received_data += i+":"+String.format("%02x", b & 0xff)+"\n";
                                i++;
                            }
                            writeData(sb);
                            tv_receivedData.setText("");
                            tv_receivedData.setText(received_data);

                            //DESC: send message
        //                    mSerial.write(rbuf);

                            updateFileLinesView();
                        }
                    });
                }else{

                }
            }while (isMainLoopRunning);
        }
    };

    private void writeData(CharSequence message) {
        String readmessages = null;
        // 現在の時刻を取得
        Date date = new Date();
        // 表示形式を設定
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd kk':'mm':'ss");
        try {
            fileHandler_.saveFile(sdf.format(date) +" "+ message);//savefile()内は、書き込みの最後に改行を自動で挿入される
            readmessages = fileHandler_.readFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (readmessages != null) {
         //   Toast.makeText(this, readmessages, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "This file is null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFileLinesView(){
        String filelines = null;
        ArrayList<String> al_sensorData = new ArrayList<String>();
        try {
            filelines = fileHandler_.readFile();
        }catch (Exception e){e.printStackTrace();}

        if(filelines != null) {
            String[] arr_filelines = filelines.split("\n");
            for(String fileline : arr_filelines){
                al_sensorData.add(fileline);
            }
        }else{
            Toast.makeText(this,"not found data on file",Toast.LENGTH_SHORT).show();
        }
        //---
        ListView lv_sensorData = (ListView) findViewById(R.id.dataList);
        Collections.reverse(al_sensorData);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_layout, al_sensorData);//android.R.layout.simple_expandable_list_item_1
        
        lv_sensorData.setAdapter(adapter);
    }
    //Thread stop
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            Toast.makeText(this, "mSerial.end();", Toast.LENGTH_SHORT).show();
            mSerial.end();
            isMainLoopRunning = false;
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }
}
