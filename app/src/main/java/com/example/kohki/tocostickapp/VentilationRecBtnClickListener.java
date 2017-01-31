package com.example.kohki.tocostickapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Kohki on 2017/01/17.
 */

class VentilationRecBtnClickListener implements View.OnClickListener {
    private AlertDialog.Builder builder;
    public static AlertDialog alertDialog;
    private static final String TAG = "VRCListener";

    @Override
    public void onClick(View v) {
        Context context = ChartActivity.getInstance();
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
        layout.findViewById(R.id.btn_vent_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now_vent = Integer.parseInt(tv_vent_parcent.getText().toString());
                if (now_vent >= 1)
                    tv_vent_parcent.setText(now_vent - 1 + "");
            }
        });
        layout.findViewById(R.id.btn_vent_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now_vent = Integer.parseInt(tv_vent_parcent.getText().toString());

                tv_vent_parcent.setText(now_vent + 1 + "");
            }
        });

        try {
            builder = new AlertDialog.Builder(context);
            builder.setTitle("換気率 記録");
            builder.setView(layout);
            builder.setCancelable(false);
            builder.setPositiveButton("新しく記録する", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String rec_date = tv_date.getText().toString();
                        String rec_vent = tv_vent_parcent.getText().toString();
                        FileOutputStream fs_ave = ChartActivity.getInstance().openFileOutput(FileContract.VENTILATION_REC_FILE, MODE_PRIVATE);
                        PrintWriter vent_rec_file = new PrintWriter(fs_ave);
                        String rec_data = rec_date+","+rec_vent;
                        Log.d(TAG,rec_data);
                        vent_rec_file.print(rec_data);
                    } catch (IOException e) {
                        Log.v("IntentErr:", e.getMessage() + "," + e);
                    }
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
}
