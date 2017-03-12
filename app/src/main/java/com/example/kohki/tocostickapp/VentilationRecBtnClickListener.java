package com.example.kohki.tocostickapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Kohki on 2017/01/17.
 */

class VentilationRecBtnClickListener implements View.OnClickListener {
    private static final String TAG = "VRCListener";
    private static final String PREFARENCE_FILE_NAME = "pre_vent_date_file";
    @Override
    public void onClick(View v) {
        final Context context = ChartActivity.getInstance();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.input_ventilation, null);
        //date
        final TextView tv_date = (TextView) layout.findViewById(R.id.tv_date);
        final Date now_date = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(now_date);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd (E)", Locale.JAPAN);//Arduino: V2016/07/11 SUN 00:37:30
        tv_date.setText(sdf.format(cal.getTime()));

        layout.findViewById(R.id.btn_date_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.DAY_OF_MONTH, -1);
                tv_date.setText(sdf.format(cal.getTime()));
            }
        });
        layout.findViewById(R.id.btn_date_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                tv_date.setText(sdf.format(cal.getTime()));
            }
        });

        //ventilation
        final TextView tv_vent_parcent = (TextView) layout.findViewById(R.id.tv_vent_parcent);

        final SharedPreferences prefer = context.getSharedPreferences(PREFARENCE_FILE_NAME, MODE_PRIVATE);
        tv_vent_parcent.setText(prefer.getFloat("vent_parcent", Float.parseFloat(tv_vent_parcent.getText() + "")) + "");

        layout.findViewById(R.id.btn_vent_one_plus).setOnClickListener(new VentilationParcentLinster(tv_vent_parcent));
        AlertDialog.Builder builder;
        AlertDialog alertDialog;

        try {
            builder = new AlertDialog.Builder(context);
            builder.setTitle("換気率 記録");
            builder.setView(layout);
            builder.setCancelable(false);
            builder.setPositiveButton("新しく記録する", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FileContract mID = new FileContract(context);
                    try {
                        SimpleDateFormat sdf_ymde = new SimpleDateFormat("yyyy'/'MM'/'dd (E)", Locale.JAPAN);
                        SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy'/'MM'/'dd", Locale.JAPAN);

                        String rec_date = tv_date.getText().toString();
                        String formated_rec_date = sdf_ymd.format(sdf_ymde.parse(rec_date));
                        float rec_vent_par = Float.parseFloat(tv_vent_parcent.getText() + "");
                        String rec_data = formated_rec_date + "," + rec_vent_par + "\n";
                        Log.d(TAG, rec_data);
                        /*
                        FileOutputStream fs_ave = ChartActivity.getInstance().openFileOutput(FileContract.VENTILATION_REC_FILE, MODE_APPEND);
                        PrintWriter vent_rec_file = new PrintWriter(fs_ave);
                        vent_rec_file.println(rec_data);
                        */

                        FileOutputStream output = context.openFileOutput(mID.getGateWayID()+"_"+mID.getNodeID()+"_vent.csv", context.MODE_APPEND);
                        output.write(rec_data.getBytes());
                        output.close();
                        //
                        SharedPreferences.Editor editor = prefer.edit();
                        editor.putFloat("vent_parcent", rec_vent_par);
                        editor.commit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //FIXME del me.
                    String te = FileHelper.readAsStrFile(context, mID.getGateWayID()+"_"+mID.getNodeID()+"_vent.csv");
                    Log.d("Vent test", te);
                }
            });
            builder.setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog = builder.show();
        } catch (Exception e) {
        }
    }
        class VentilationParcentLinster implements View.OnClickListener {
            TextView tv_vent_parcent;
            public VentilationParcentLinster(TextView tv_vent_parcent){
                this.tv_vent_parcent = tv_vent_parcent;
            }
            @Override
            public void onClick(View v) {
                float now_vent = Float.parseFloat(tv_vent_parcent.getText().toString());

                switch (v.getId()){
                    case R.id.btn_vent_one_minus:
                        if (now_vent >= 1) {
                            now_vent -= 1;
                            tv_vent_parcent.setText(now_vent+"");
                        }
                        break;
                    case R.id.btn_vent_zero_point_one_minus:
                        if (now_vent >= 1) {
                            now_vent -= 0.5;
                            tv_vent_parcent.setText(now_vent+"");
                        }
                        break;
                    case R.id.btn_vent_one_plus:
                        if (now_vent < 100) {
                            now_vent += 1;
                            tv_vent_parcent.setText(now_vent+"");
                        }
                        break;
                    case R.id.btn_vent_zero_point_one_plus:
                        if (now_vent < 100) {
                            now_vent -= 0.5;
                            tv_vent_parcent.setText(now_vent+"");
                        }
                        break;
                }
            }
        }
}
