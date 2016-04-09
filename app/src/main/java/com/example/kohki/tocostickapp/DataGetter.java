package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Created by Kohki on 2016/02/25.
 */
public class DataGetter extends Activity{

    FTDriver mSerial;
    private static final String ACTION_USB_PERMISSION =  "jp.ksksue.tutorial.USB_PERMISSION";//"com.example.kohki.USB_PERMISSION";
    private boolean mRunningMainLoop = false;
    final int mOutputType = 0;
    final boolean SHOW_LOGCAT = false;
    String TAG = "TWE_Line";
    Handler mHandler = new Handler();
    final int SERIAL_BAUDRATE = FTDriver.BAUD115200;
    private boolean mStop = false;

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

    }
    public void toaster(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
            //        byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
            byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte

            final TextView tv_receivedData = (TextView) findViewById(R.id.receivedData);
            for(;;){
                // [FTDriver] Read from USB Serial
                len = mSerial.read(rbuf);
                final String str1 = new String(rbuf);
                final int fin_len = len;

                if(len > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_receivedData.setText("");
                            tv_receivedData.setText("Message: " + str1);
                            toaster(str1);
                        }
                    });
                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_receivedData.setText("");
                            tv_receivedData.setText("Message: " + str1);
                        }
                    });
                }
            }
        }
    };
}
