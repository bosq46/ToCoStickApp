package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Created by Kohki on 2016/02/24.
 */
public class ChatActivity extends Activity {

    FTDriver mSerial;
    private static final String ACTION_USB_PERMISSION =  "jp.ksksue.tutorial.USB_PERMISSION";//"com.example.kohki.USB_PERMISSION";
    private boolean mRunningMainLoop = false;
    final int mOutputType = 0;
    final boolean SHOW_LOGCAT = false;
    String TAG = "TWE_Line";
    Handler mHandler = new Handler();
    final int SERIAL_BAUDRATE = FTDriver.BAUD115200;
    private boolean mStop = false;

    SensorDBHelper dbHelper = new SensorDBHelper(ChatActivity.this);
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        //---

        mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));

        // [FTDriver] setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);

        if(mSerial.begin(SERIAL_BAUDRATE)) {
            mainloop();
            Toast.makeText(this, "serial connection begin", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "serial connection failed", Toast.LENGTH_SHORT).show();
        }
    }
    public void toaster(String message){
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
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
            byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte

            final TextView tv_message = (TextView) findViewById(R.id.message);
            for(;;){
                // [FTDriver] Read from USB Serial
                len = mSerial.read(rbuf);
                final String str1 = new String(rbuf);
                final int fin_len = len;
                if(len > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String message = (String)tv_message.getText();
                            tv_message.setText("");
                            message += "\n\n" + str1;
                            tv_message.setText(str1);//"\nlen: " + fin_len +
                            toaster(str1);



                        }
                    });
                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        //    tv_message.setText("\nlen: " + fin_len + "\nMessage: " + str1);
                        }
                    });
                }
            }
        }
    };
}
