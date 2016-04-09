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

        final EditText sendData = (EditText) findViewById(R.id.sendData);
        final EditText cnt_send = (EditText) findViewById(R.id.cnt_send);
        Button btn_minus        = (Button) findViewById(R.id.minus_one);
        Button btn_plus         = (Button) findViewById(R.id.plus_one);
        Button btn_create_data  = (Button) findViewById(R.id.btn_create_data);
        sendData.setText(str_send_data_);

        // [FTDriver] setPermissionIntent() before begin()
        mSerial = new FTDriver((UsbManager) getSystemService(Context.USB_SERVICE));

        // [FTDriver] setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);


        btn_minus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder) cnt_send.getText();
                String str = sb.toString();
                int cnt_send_num = Integer.parseInt(str);
                cnt_send_num--;
                cnt_send.setText(cnt_send_num + "");
            }
        });
        btn_plus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder) cnt_send.getText();
                String str = sb.toString();
                int cnt_send_num = Integer.parseInt(str);
                cnt_send_num++;
                cnt_send.setText(cnt_send_num + "");
            }
        });
        btn_create_data.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder) cnt_send.getText();
                int cnt = Integer.parseInt(sb.toString());
                String str_send_data = makeSendData(cnt);
                sendData.setText(str_send_data);
            }
        });
    }
    public void onSwitchClicked(View view) {
        Switch swtOnOff = (Switch) view;
        Toast.makeText(getApplicationContext(), "SC start!", Toast.LENGTH_SHORT).show();

        final EditText sendData = (EditText) findViewById(R.id.sendData);
        if (swtOnOff.isChecked()) { // ON状態になったとき
            Toast.makeText(getApplicationContext(), "SwitchがONになりました。", Toast.LENGTH_SHORT).show();
            //

            if(mSerial.begin(SERIAL_BAUDRATE)) {
                mSendData();
             //   Toast.makeText(this, "serial connection begin", Toast.LENGTH_SHORT).show();
            }

        }
        if (!swtOnOff.isChecked()) { // OFF状態になったとき
            mSerial.end();
        }
    }
    public String makeSendData(int n){
        int sum_rand;
        Random rand = new Random();;
        String str_rand;
        String send_data = "";
        int i_rand;
        for(int i=1;i<=n;i++){
            str_rand = "";
            sum_rand = 0;
            for(int j=0;j<22;j++){
                i_rand = rand.nextInt(10);
                str_rand += i_rand+"";
                sum_rand += i_rand;
            }
            send_data += 1;
            if(i<10)
                send_data += "0";
            send_data += i+str_rand+sum_rand;
            if(i!=n)
                send_data +="\n";
        }
        return send_data;
    }
    private void mSendData(){
        new Thread(mSendDataLoop).start();
    }
    private Runnable mSendDataLoop = new Runnable() {
        @Override
        public void run() {

            final EditText sendData = (EditText) findViewById(R.id.sendData);
            SpannableStringBuilder sb = (SpannableStringBuilder) sendData.getText();
            String[] send_data = sb.toString().split("\n");
            for(int i=0;i<send_data.length;i++){
                String wbuf = ":"+ send_data[i] + "\r\n";//788001000F00000
                Toast.makeText(getApplicationContext(),"send:"+send_data[i],Toast.LENGTH_SHORT).show();
                mSerial.write(wbuf.getBytes());
                for(int j=0;j<1000;j++){
                    for(int k=0;k<1000;k++){}
                }
            }

            //String wbuf = ":7880010101FFFFFFFFFFFFFFFF0D\r\n"; 28
            //mSerial.write(wbuf.getBytes());
        }
    };
}
