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
    public static Context mContext; //FIXME: static
    private ReceiveThread mCommuThread;

    private  int commuPhase;

    public ReceiveThreadHelper(Context context, String file_name, Handler handler){
        mContext = context;
        mCommuThread  = new ReceiveThread(context);
    }

    public boolean start(){
        if (mCommuThread.beginSerial()) {
            try{
                ReceiveThread.isLoopingCommu = true;
                new Thread(mCommuThread).start();//--
            }catch (Exception e){
                Toast.makeText(mContext,"E: "+e,Toast.LENGTH_SHORT).show();
            }
     //       Toast.makeText(mContext,"true",Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(mContext,"false",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean finish(){
        commuPhase = 0;
        ReceiveThread.isLoopingCommu = false;
        //    mCommuThread.finish();
        return true;
    }

    public static boolean saveReceivedData(int[] receive_data){
        final int mes = receive_data.length;

        /*
        final StringBuilder sb_receive_data = new StringBuilder(3 * receive_data.length);// 1 char is 'upper4bit and lower4bit and space'
        for (int i = 0; i < receive_data.length; i++) {
            sb_receive_data.append(Integer.toHexString((receive_data[i] & 0xF0) >> 4));
            sb_receive_data.append(Integer.toHexString(receive_data[i] & 0x0F));
            sb_receive_data.append(" ");
        }
        */
        try {
            //write file.
            //FileHelper.writeAsBinFile(ReceiveActivity.strFileName, receive_data);
          //  FileHelper.writeAsBinFile(ReceiveActivity.strFileName, receive_data);
            //update view.
           // ReceiveActivity.updateFileLinesView(Integer.toHexString(receive_data[receive_data.length-1]));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public static boolean saveReceivedData(String received_hexdata) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd EEE kk':'mm':'ss");//Arduino: V2016/07/11 SUN 00:37:30
        String str_date = sdf.format(date);
        try {
            FileHelper.writeAsStrFile(mContext,ReceiveActivity.strFileName,str_date, received_hexdata);
            ReceiveActivity.setDataView(str_date+"\n"+received_hexdata);
            ReceiveActivity.setDataListView();
        } catch (final Exception e) {

            return false;
        }
        return true;
    }

    private boolean checkNextPhase(int received_phase){//TODO
        return received_phase > commuPhase ? true : false;
    }
}
