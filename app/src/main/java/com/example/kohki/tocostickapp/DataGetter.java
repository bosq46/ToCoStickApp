package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Created by Kohki on 2016/02/25.
 */
public class DataGetter extends Activity{

    FTDriver mSerial;
    private static final String ACTION_USB_PERMISSION =  "jp.ksksue.tutorial.USB_PERMISSION";//"com.example.kohki.USB_PERMISSION";
    private boolean mRunningMainLoop = false;
    final   int     mOutputType       = 0;
    final   boolean SHOW_LOGCAT      = false;
    String TAG = "TWE_Line";
    Handler mHandler;
    final int SERIAL_BAUDRATE = FTDriver.BAUD115200;
    private boolean mStop = false;

    private FileHandler fileHandler_;
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
            mainloop();
            Toast.makeText(this, "serial connection begin", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "serial connection failed", Toast.LENGTH_SHORT).show();
        }
        mHandler = new Handler();

        fileHandler_ = new FileHandler("sensor_data.txt", this.getApplicationContext());
    }
    private void toaster(String message){
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void mainloop() {
        mRunningMainLoop = true;
        new Thread(mLoop).start();
    }
    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int i,len;
            // [FTDriver] Create Read Buffer
            //  byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
            final byte[] rbuf = new byte[4096];   // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
            final TextView tv_receivedData = (TextView) findViewById(R.id.receivedData);
            final ArrayList arrayList = new ArrayList();

            for(;;){
                // [FTDriver] Read from USB Serial
                len = mSerial.read(rbuf);

                byte[] bytes_message = new byte[len];

            //    final byte[] hex_message = bytes_message;
                  if(len > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String message = "";
                           // for (byte b : hex_message) {
                            for (byte b : rbuf) {
                                if(b != 0x00)
                                    message += Integer.toHexString(0xff & b)+" ";
                            }
/*
                            try {
                                message = new String(hex_message, "UTF-8");
                            }catch (Exception e){e.printStackTrace();}
*/
                            writeData(message);
                            arrayList.add(message);

                            tv_receivedData.setText("");
                            String messages = "";
                            for(int i=0;i<arrayList.size();i++){
                                messages += (String)arrayList.get(i) +"\n";
                            }
                            tv_receivedData.setText(message);//messages
                        }
                    });
                }else{
             //       toaster("no message.");
                }
            }
        }
    };
    private void writeData(String message){
        String readmessages = null;
        try {
            fileHandler_.saveFile(message);
            readmessages = fileHandler_.readFile();
        }catch (Exception e){e.printStackTrace();}
        if(readmessages != null)
            Toast.makeText(this, readmessages, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "This file is null.", Toast.LENGTH_SHORT).show();

    }
}
