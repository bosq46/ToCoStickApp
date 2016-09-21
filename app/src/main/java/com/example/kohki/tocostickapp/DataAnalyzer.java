package com.example.kohki.tocostickapp;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kohki on 2016/05/13.
 *
 * Description: The purpose of this class is Analysis of received sensor data
 * data is contain id, kind of sensor, num of data, data, parity
 */
public class DataAnalyzer {
    private static Context context_;

    public DataAnalyzer(Context context){
        context_ = context;
    }

    public String firstStep(byte[] data){
        data[0] &= 0x01;
        switch (data[0]){
            case 0x01:
                int date_len = (int)data[1];
                byte[] date = new byte[date_len];
                for(int i=0;i<date_len;i++){
                    date[i] = data[i+2];
                }
                String str_date = new String(data);
                Toast.makeText(context_,str_date,Toast.LENGTH_SHORT).show();
                String[] arr_date = str_date.split(" ");//Arduino: V2016/07/11 SUN 00:37:30

                break;
            default:
                Toast.makeText(context_, "default",Toast.LENGTH_SHORT).show();
                break;
        }

/*      日付比較
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd EEE kk':'mm':'ss", Locale.US);//EEEは漢字の曜日になる。
        Toast.makeText(context_, sdf.format(date)+"\n"+ str_date, Toast.LENGTH_SHORT).show();
        if(str_date != sdf.format(date)) {
            return sdf.format(date);
        }
*/
        return "";
    }
    public float[] secondStep(byte[] receiveSensorData){
        float[] data = new float[0];

        return data;
    }
    /*
    val[0]=ReadSens_ch(0,5,50);  //サーミスタ電圧 AD0の値50ms間隔で5回測定平均値(個別ch, 読取回数, intarvalms)
    val[1]=ReadSens_ch(1,5,50);  //Eneloop電圧AD1の5回平均値(個別ch, 読取回数, intarvalms)
    val[2]=ReadSens_ch(2,5,50);  //土壌湿度(個別ch, 読取回数, intarvalms)
    val[3]=ReadSens_ch(3,5,50);  //2Wパネル日射量AD3の5回平均値(個別ch, 読取回数, intarvalms)
    */
    /* receive sensor data*/
    public boolean thirdStep(byte[] data){

        return false;
    }
}
