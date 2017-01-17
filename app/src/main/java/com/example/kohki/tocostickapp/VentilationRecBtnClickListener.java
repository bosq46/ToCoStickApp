package com.example.kohki.tocostickapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Kohki on 2017/01/17.
 */

class VentilationRecBtnClickListener implements View.OnClickListener {
    private AlertDialog.Builder builder;
    public static AlertDialog alertDialog;

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
        final TextView tv_parcent = (TextView) layout.findViewById(R.id.tv_parcent);
        layout.findViewById(R.id.btn_vent_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now_vent = Integer.parseInt(tv_parcent.getText().toString());
                if (now_vent >= 1)
                    tv_parcent.setText(now_vent - 1 + "");
            }
        });
        layout.findViewById(R.id.btn_vent_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now_vent = Integer.parseInt(tv_parcent.getText().toString());

                tv_parcent.setText(now_vent + 1 + "");
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

                    } catch (Exception e) {
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
