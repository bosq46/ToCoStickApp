package com.example.kohki.tocostickapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Created by Kohki on 2016/12/05.
 */
public class SendThreadHelper extends AsyncTask<Integer,Integer,Integer>{
    private static final String ACTION_USB_PERMISSION = "com.example.kohki.USB_PERMISSION";//jp.ksksue.tutorial.USB_PERMISSION";
    private static final int   SERIAL_BAUDRATE = FTDriver.BAUD115200;

    private Context mContext;
    private FTDriver mSerial;
    SendThreadHelper(Context context){
        mContext = context;
    }
    @Override
    protected Integer doInBackground(Integer... value) {

        try {
            mSerial = new FTDriver((UsbManager) mContext.getSystemService(mContext.USB_SERVICE));
            PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mSerial.setPermissionIntent(permissionIntent);
            mSerial.begin(SERIAL_BAUDRATE);
            byte[] send_data = new byte[1];
            send_data[0] = Byte.parseByte(value[0]+"");
            mSerial.write(send_data);
            //1秒停止します。
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return value[0] + 2;
    }
}
