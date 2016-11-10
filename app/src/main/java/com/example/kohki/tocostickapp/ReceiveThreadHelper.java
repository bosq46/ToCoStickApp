package com.example.kohki.tocostickapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.zip.Inflater;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Created by Kohki on 2016/10/31.
 */
public class ReceiveThreadHelper {

    private static final String ACTION_USB_PERMISSION = "com.example.kohki.USB_PERMISSION";//jp.ksksue.tutorial.USB_PERMISSION";
    private static final int   SERIAL_BAUDRATE = FTDriver.BAUD115200;

    private Context mContext;
    private int commuStep = 1;
    private FTDriver mSerial;

    private Handler mHandler;
    public static String mSaveFileName  = "sensor_data.txt";
    private int   mReceiveDataSize = 64;

    TextView tv_receivedData;

    public ReceiveThreadHelper(Context context){
        mContext = context;
        mSerial = new FTDriver((UsbManager) mContext.getSystemService(mContext.USB_SERVICE));
        // [FTDriver] setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);

        mHandler = new Handler();
    }

    public boolean start(){
        if (mSerial.begin(SERIAL_BAUDRATE)) {
            new Thread(new ReceiveThread()).start();
            return true;
        } else {
            return false;
        }
    }

    public boolean end(){
        mSerial.end();
        return false;
    }
    class ReceiveThread implements Runnable {
        String log = "";
        private boolean is_correct_receive;

        @Override
        public void run() {
            do {
                //context 参照できない(post 内で書く)
                final byte[] rbuf = new byte[mReceiveDataSize];   // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
                int len;
                // [FTDriver] Create Read Buffer
                len = mSerial.read(rbuf);

                if (len > 0) {
                    if((rbuf[0] & 0xf0) == 0x30 ) {
                        // rbuf[0] &= 0x0f;
                        // cant use upper 4bit of first byte(upper 4bit is 0x03)
                        //  byte[] => String
                        StringBuilder sb_receive_data = new StringBuilder(3 * rbuf.length);
                        char[] receive_data = new char[rbuf.length];
                        char receive_datum;
                        log = "";
                        is_correct_receive = false;
                        for (int i = 0; i < rbuf.length; i++) {//rbuf.length
                            receive_datum = convByteToChar(rbuf[i]);
                            receive_data[i] = receive_datum;
                            // hex 確認用
                            sb_receive_data.append(Integer.toHexString((receive_datum & 0xF0) >> 4));
                            sb_receive_data.append(Integer.toHexString(receive_datum & 0xF));
                            sb_receive_data.append(" ");
                            log += String.format("%02x", receive_datum & 0xff);// = Integer.toHexString(receive_datum);
                            if (i == rbuf.length - 1)
                                log += "(" + i + "," + (int) receive_datum + ")";
                            else
                                log += "(" + i + "," + (int) receive_datum + "),";
                        }
                        //正しい受信(not containing '0x00')ができれば保存,viewの更新.
                        is_correct_receive = checkCorrectReception(receive_data);
                        if(is_correct_receive){
                            writeHexData(sb_receive_data.toString());
                            mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                                @Override
                                public void run() {
                                    ReceiveActivity.updateFileLinesView(log);
                                }
                            });
                            //TODO:受信したことを伝える.
                            byte[] send_data = {0x01,0x01,0x01};
                            char[] r = new char[0];
                            byte[] a = new byte[mReceiveDataSize];
                            do{
                                mSerial.write(send_data);
                                mSerial.read(a);
                            }while (rbuf[1] == a[1]);
                        }

                    } else {
                   //     Toast.makeText(mContext, "not:[0]" + rbuf[0] + ",[1]" + rbuf[1], Toast.LENGTH_SHORT).show();
                    }
                }
            } while (ReceiveActivity.isMainLoopRunning);
        }
    }
    private void writeHexData(String message) {
        //  Toast.makeText(this,message+"",Toast.LENGTH_SHORT).show();
        // 現在の時刻を取得
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd EEE kk':'mm':'ss");//Arduino: V2016/07/11 SUN 00:37:30
        try {
            FileHelper.writeAsStrFile(mContext, ReceiveActivity.strFileName, sdf.format(date), message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public char convByteToChar(byte byte_val) {
        char chr_val = (char) byte_val;
        if ((chr_val & 0x80) == 0x80) {
            chr_val += 256;
        }
        return chr_val;
    }
    private boolean checkCorrectReception(char[] receive_data){
        for(int datum : receive_data){
            if(datum == 0x00){ //failed
                return false;
            }
        }
        return true;
    }
}
