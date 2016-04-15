package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Random;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Created by Kohki on 2016/02/25.
 */
//TODO: send data is "A id index 1 2 3 - - - 100 sum(1-100)"
public class PseudoArduino extends Activity {

    static  String str_send_data_ = "1199999999072";
    static  int send_period_ = 3; //1 sending every 3 seconds

    final int mOutputType = 0;
    final boolean SHOW_LOGCAT = false;
    final int SERIAL_BAUDRATE = FTDriver.BAUD115200;
    // [FTDriver] Object
    FTDriver mSerial;
    String TAG = "FTSampleTerminal";
    Handler mHandler = new Handler();
    // [FTDriver] Permission String
    private static final String ACTION_USB_PERMISSION = "jp.ksksue.tutorial.USB_PERMISSION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pseudo_arduino);

        final EditText et_sendData = (EditText) findViewById(R.id.sendData);
        et_sendData.setText(str_send_data_);

        //送信データの個数
        Button btn_minus_num        = (Button) findViewById(R.id.minus_one);
        Button btn_plus_num         = (Button) findViewById(R.id.plus_one);
        final EditText et_data_num = (EditText) findViewById(R.id.cnt_send);
        Button btn_create_data     = (Button) findViewById(R.id.btn_create_data);
        //送信周期
        Button btn_minus_period        = (Button) findViewById(R.id.minus_one_second);
        Button btn_plus_period         = (Button) findViewById(R.id.plus_one_second);
        final EditText et_send_period = (EditText) findViewById(R.id.cnt_send_period);
        et_sendData.setText(str_send_data_);

        // [FTDriver] setPermissionIntent() before begin()
        mSerial = new FTDriver((UsbManager) getSystemService(Context.USB_SERVICE));
        // [FTDriver] setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);

        //送信データの個数変更, データの作成
        btn_minus_num.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder) et_data_num.getText();
                String str = sb.toString();
                int cnt_send_num = Integer.parseInt(str);
                cnt_send_num--;
                et_data_num.setText(cnt_send_num + "");
            }
        });
        btn_plus_num.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder) et_data_num.getText();
                String str = sb.toString();
                int cnt_send_num = Integer.parseInt(str);
                cnt_send_num++;
                et_data_num.setText(cnt_send_num + "");
            }
        });
        btn_create_data.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder) et_data_num.getText();
                int cnt = Integer.parseInt(sb.toString());
                String str_send_data = makeSendData(cnt);
                et_sendData.setText(str_send_data);
            }
        });
        //送信データの周期を変更する
        btn_minus_period.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(send_period_ >= 2)
                    send_period_--;
                et_send_period.setText(send_period_ + "");
            }
        });
        btn_plus_period.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               send_period_++;
                et_send_period.setText(send_period_+"");
            }
        });
    }
    public void onSwitchClicked(View view) {
        Switch swtOnOff = (Switch) view;
        //Toast.makeText(getApplicationContext(), "SC start!", Toast.LENGTH_SHORT).show();

        final EditText sendData = (EditText) findViewById(R.id.sendData);
        if (swtOnOff.isChecked()) { // ON状態になったとき
        //    Toast.makeText(getApplicationContext(), "SwitchがONになりました。", Toast.LENGTH_SHORT).show();
            if(mSerial.begin(SERIAL_BAUDRATE)) {
                Toast.makeText(this, "serial connection start", Toast.LENGTH_SHORT).show();
                mSendData();
            }
        }
        if (!swtOnOff.isChecked()) { // OFF状態になったとき
            mSerial.end();
        }
    }
    public String makeSendData(int num_sendData){
        Random rand = new Random();
        String sum_str_rand = "";

        for(int i=1; i <= num_sendData; i++){
            //22 is digit of sendData
            sum_str_rand += i+"_";
            for(int j=1;j<22;j++){
                sum_str_rand += rand.nextInt(10) +"";
            }
            if(i != num_sendData)
                sum_str_rand += "\n";
        }
        return sum_str_rand;
    }
    private void mSendData(){
        new Thread(mSendDataLoop).start();
    }
    private Runnable mSendDataLoop = new Runnable() {
        @Override
        public void run() {
            final Switch sw_connect = (Switch) findViewById(R.id.sw_connect);
            final EditText send_period = (EditText) findViewById(R.id.cnt_send_period);
            do {
                final EditText sendData = (EditText) findViewById(R.id.sendData);
                SpannableStringBuilder sb = (SpannableStringBuilder) sendData.getText();
                String[] send_data = sb.toString().split("\n");

                //     Toast.makeText(getApplicationContext(),"send:"+send_data.length,Toast.LENGTH_SHORT).show();
                for (int i = 0; i < send_data.length; i++) {
                    String wbuf = send_data[i] + "\r\n";
                    // [FTDriver] Wirte to USB Serial
                    mSerial.write(wbuf.getBytes());
                    //Toast.makeText(getApplicationContext(), "END" + send_data.length, Toast.LENGTH_SHORT).show(); :CANT USE
                    try {
                        int period = Integer.parseInt(send_period.getText()+"");
                        Thread.sleep(send_period_*1000);
                    } catch (Exception e) {}
                }
            }while(sw_connect.isChecked());
        }
    };
}
