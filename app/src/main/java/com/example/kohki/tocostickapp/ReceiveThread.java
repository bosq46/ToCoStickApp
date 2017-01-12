package com.example.kohki.tocostickapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.widget.Toast;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Created by Kohki on 2016/12/10.
 */

class ReceiveThread implements Runnable {
    //    String log = "";
    private static final String ACTION_USB_PERMISSION = "com.example.kohki.USB_PERMISSION";//jp.ksksue.tutorial.USB_PERMISSION";
    private static final int   SERIAL_BAUDRATE = FTDriver.BAUD115200;
    public FTDriver mSerial;
    private Context mContext;
    private int   mReceiveDataSize = 64;
    public static boolean isLoopingCommu;

    ReceiveThread(Context context){
            mContext = context;
            mSerial = new FTDriver((UsbManager) mContext.getSystemService(mContext.USB_SERVICE));
            // [FTDriver] setPermissionIntent() before begin()
            PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mSerial.setPermissionIntent(permissionIntent);
    }
    public boolean beginSerial(){
        if(mSerial.begin(SERIAL_BAUDRATE)){
            isLoopingCommu=true;
           return true;
        }else {return false;}
    }
    public boolean finish(){
        mSerial.end();
        return true;//REVIEW:
    }
    @Override
    public void run() {
        do {
            try {
                Thread.sleep(1000); //3000ミリ秒Sleepする
            } catch (InterruptedException e) {}
            //context 参照できない(post 内で書く)
            final byte[] rbuf = new byte[mReceiveDataSize];   // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
            final int len;
            // [FTDriver] Create Read Buffer
            len = mSerial.read(rbuf); //this method return length of argument

            if ((rbuf[0] & 0xf0) == 0x30) {//checkCorrectReception(rbuf)){// && (rbuf[0] & 0xf0) == 0x30) {//FIXME:03 is wireless id. seek better method.
                // cant use upper 4bit of first byte(upper 4bit is 0x03)
                final StringBuilder sb_receive_data = new StringBuilder(3 * len);// 1 char is 'upper4bit and lower4bit and space'
                int[] receive_data = new int[len];
                int receive_datum;
                //        log = "";
                for (int i = 0; i < len; i++) {
                    receive_datum = correctUnsignedNum(rbuf[i]);
                    receive_data[i] = receive_datum;
                    // hex 確認用
                    sb_receive_data.append(Integer.toHexString((receive_datum & 0xF0) >> 4));
                    sb_receive_data.append(Integer.toHexString(receive_datum & 0x0F));
                    sb_receive_data.append(" ");
                }

                final int receive_pagenum = receive_data[1];
                final int receive_csum    = receive_data[2];
                final int local_pagenum   = ReceiveThreadHelper.commuPhase;
                ReceiveActivity.mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "receive page:" + receive_pagenum + ",csum:" + receive_csum +
                                "\n local page" + local_pagenum, Toast.LENGTH_SHORT).show();
                    }
                });
                ReceiveThreadHelper.saveReceivedData(sb_receive_data.toString());//data save
                if (local_pagenum == receive_pagenum) {
                    ReceiveThreadHelper.saveReceivedData(sb_receive_data.toString());//data save

                    ReceiveActivity.mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "save", Toast.LENGTH_SHORT).show();
                        }
                    });
                    int is_send = 0;
                    int i = 0;
                    while (is_send <= 0 || i < 2) {
                        is_send = mSerial.write(receive_pagenum + "");
                        i++;
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                    }
                    ReceiveThreadHelper.commuPhase++;
                }
            }
        }while (isLoopingCommu) ;
    }
    public int correctUnsignedNum(byte byte_val) {
        int int_val = (int) byte_val;
        if (byte_val < 0){// < (int_val & 0x80) == 0x80) {
            int_val += 256;
        }
        return int_val;
    }

    private boolean checkCorrectReception(final byte[] bytes){//checksum
        if(bytes[0] == 0)
            return false;
        int check_sum = correctUnsignedNum(bytes[2]);
        int bytes_sum = 0;
        for(int i=3;i<bytes.length;i++){
            bytes_sum += correctUnsignedNum(bytes[i]);

            if(bytes[i] == 0)
                return false;
        }
        return true;
        /*
        final byte a = Byte.parseByte(Integer.toHexString(bytes_sum));//BUG
        mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
            @Override
            public void run() {
                Toast.makeText(mContext, "check_sum" + correctUnsignedNum(bytes[2]) + ", bytes_sum"+a,
                        Toast.LENGTH_SHORT).show();
            }
        });

        if(check_sum == bytes_sum) {
            mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                @Override
                public void run() {
                    Toast.makeText(mContext, "true",Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }else{
            mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                @Override
                public void run() {
                    Toast.makeText(mContext, "false",Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
        */
    }

}
