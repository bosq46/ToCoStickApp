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
/*
  BAUD9600
 BAUD14400
 BAUD19200
 BAUD38400
 BAUD57600
BAUD115200
BAUD230400
*/
public class ReceiveActivity extends Activity {
    private static final String TAG = "ReceiveAct";
    private static final String ACTION_USB_PERMISSION = "com.example.kohki.USB_PERMISSION";//jp.ksksue.tutorial.USB_PERMISSION";
    private static final int SERIAL_BAUDRATE = FTDriver.BAUD115200;

    private Context context;
    private FTDriver mSerial;
    private boolean isMainLoopRunning = false;
    private Handler mHandler;
    private FileHandler mFileHandler;
    private DataAnalyzer mDataAnalyzer;

    private int commuStep = 1;

    private byte[] receiveData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        context = this;

        mSerial = new FTDriver((UsbManager) getSystemService(Context.USB_SERVICE));
        // [FTDriver] setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);

        mHandler = new Handler();
        mFileHandler = new FileHandler(this, "sensor_data.txt");
        mDataAnalyzer = new DataAnalyzer(this);
        updateFileLinesView();//データファイルをビューに書き出す。

        if (mSerial.begin(SERIAL_BAUDRATE)) {
            startThread();
            //    Toast.makeText(this, "serial connection begin", Toast.LENGTH_SHORT).show();
        } else {
            //    Toast.makeText(this, "serial connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void startThread() {
        isMainLoopRunning = true;
        new Thread(mLoop).start();
    }

    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            final byte[] rbuf = new byte[64];   // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
            final TextView tv_receivedData = (TextView) findViewById(R.id.receivedData);
            int len, i;

            do {
                //context 参照できない(post 内で書く)
                // [FTDriver] Create Read Buffer
                len = mSerial.read(rbuf);
                if (len > 0) {
                    mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                        @Override
                        public void run() {
                            if (true) {//(rbuf[0] & 0xf0) == 0x30 ) {
                                //        rbuf[0] &= 0x0f; //cant use upper 4bit of first byte(upper 4bit is 0x03)
                                //        rbuf[len] = 0;
                            /* byte[] => String */
                                StringBuilder sb_hexbuf = new StringBuilder(3 * rbuf.length);
                                char[] chrs_receive_data = new char[rbuf.length];
                                char receive_datum;
                                String log = "";
                                for (int i = 0; i < rbuf.length; i++) {//rbuf.length

                                    receive_datum = convByteToChar(rbuf[i]);
                                    chrs_receive_data[i] = receive_datum;
                                    //     String hex_receive_data = Integer.toHexString(receive_datum);//ANO: String.format("%02x", chr_receive_data & 0xff)
                                    //     sb_hexbuf.append(hex_receive_data);
                                    String str_buf = String.format("%02x", rbuf[i] & 0xff);
                                    //        sb_hexbuf.append(Integer.toHexString((receive_datum & 0xF0) >> 4));
                                    //        sb_hexbuf.append(Integer.toHexString(receive_datum & 0xF));
                                    sb_hexbuf.append(str_buf);
                                    sb_hexbuf.append(" ");
                                    log += str_buf;
                                    if (i == rbuf.length - 1)
                                        log += "(" + i + "," + (int) receive_datum + ")";
                                    else
                                        log += "(" + i + "," + (int) receive_datum + "),";
                                }
                                try {
                                    String date = new String(rbuf, "ASCII");
                                } catch (Exception e) {
                                }
                                tv_receivedData.setText(log);
                                writeHexData(sb_hexbuf);
                                updateFileLinesView();
                            } else {
                                Toast.makeText(context, "not:[0]" + rbuf[0] + ",[1]" + rbuf[1], Toast.LENGTH_SHORT).show();
                            }
                            /*     byte[] currect_date = mDataAnalyzer.firstStep(rbuf).getBytes();
                            if(currect_date.length > 0){
                                Toast.makeText(context_, "current_date", Toast.LENGTH_SHORT).show();
                            mSerial.write(currect_date);
                            }
                            */
                        }
                    });
                }
/*
                try {
                    Thread.sleep(4900);
                }catch (InterruptedException e){
                    Toast.makeText(context,e+"",Toast.LENGTH_SHORT).show();
                }
  */
            } while (isMainLoopRunning);

        }
    };

    private void writeHexData(CharSequence message) {
        //  Toast.makeText(this,message+"",Toast.LENGTH_SHORT).show();
        String readmessages = null;
        // 現在の時刻を取得
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd EEE kk':'mm':'ss");//Arduino: V2016/07/11 SUN 00:37:30
        try {
            mFileHandler.writeStrFile(sdf.format(date) + " " + message);//savefile()内は、書き込みの最後に改行を自動で挿入される
            readmessages = mFileHandler.readStrFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (readmessages != null) {
            //    Toast.makeText(this, readmessages, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "This file is null.", Toast.LENGTH_SHORT).show();
        }
    }

    public char convByteToChar(byte byte_val) {
        char chr_val = (char) byte_val;
        if ((chr_val & 0x80) == 0x80) {
            chr_val += 256;
        }
        return chr_val;
    }

    private void updateFileLinesView() {
        String filelines = null;
        ArrayList<String> al_sensorData = new ArrayList<String>();
        try {
            filelines = mFileHandler.readStrFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (filelines != null) {
            String[] arr_filelines = filelines.split("\n");
            for (String fileline : arr_filelines) {
                al_sensorData.add(fileline);
            }
        } else {
            Toast.makeText(context, "not found data on file", Toast.LENGTH_SHORT).show();
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(context, "mSerial.end();", Toast.LENGTH_SHORT).show();
            mSerial.end();
            isMainLoopRunning = false;
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }
}
