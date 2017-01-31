package com.example.kohki.tocostickapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Kohki on 2017/01/24.
 */

public abstract class WebDataDLBtnClickListener implements View.OnClickListener {
    private static final String TAG = "VRCListener";
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    protected int indexTimeRange = 0;//偶数はDate 奇数はTime

    private Button btn_negative;
    private Button btn_positive;
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick()");
        final WebDateTimeLayoutManager mLayoutManager = new WebDateTimeLayoutManager();

        try {
            Context context = ChartActivity.getInstance();
            builder = new AlertDialog.Builder(context);
            builder.setTitle("WebAPI ダウンロード");
            builder.setView(mLayoutManager.getLayout());
            builder.setCancelable(true);
            builder.setPositiveButton("次へ", null);
            builder.setNegativeButton("戻る", null);
            alertDialog = builder.show();

            btn_negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            btn_positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            btn_negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (indexTimeRange > 0) {
                        indexTimeRange--;
                        mLayoutManager.changeLayout(indexTimeRange+1, indexTimeRange);
                    }else {
                        alertDialog.dismiss();
                    }
                }
            });
            btn_positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (indexTimeRange < 3) {
                        indexTimeRange++;
                        mLayoutManager.changeLayout(indexTimeRange-1,indexTimeRange);
                    }else {
                        downloadSensorData();
                    }
                    //alertDialog.dismiss();//close
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    abstract public void downloadSensorData();

    class WebDateTimeLayoutManager{
        LinearLayout menu_layout;
        LinearLayout date_view;
        LinearLayout time_view;

        private TextView tv_fromDate;
        private TextView tv_fromTime;
        private TextView tv_toDate;
        private TextView tv_toTime;

        WebDateTimeLayoutManager(){
            Context context = ChartActivity.getInstance();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            final View dialog_layout = inflater.inflate(R.layout.input_webapi_menu, null);
            menu_layout = (LinearLayout) dialog_layout.findViewById(R.id.input_webapi_layout);
            date_view   = (LinearLayout) inflater.inflate(R.layout.input_webapi_date, null);
            time_view   = (LinearLayout) inflater.inflate(R.layout.input_webapi_time, null);
            menu_layout.addView(date_view);

            tv_fromDate = (TextView)menu_layout.findViewById(R.id.tv_webapi_fromDate);
            tv_fromTime = (TextView)menu_layout.findViewById(R.id.tv_webapi_fromTime);
            tv_toDate   = (TextView)menu_layout.findViewById(R.id.tv_webapi_toDate);
            tv_toTime   = (TextView)menu_layout.findViewById(R.id.tv_webapi_toTime);

            tv_fromDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeLayout(indexTimeRange,0);
                    indexTimeRange = 0;
                }
            });
            menu_layout.findViewById(R.id.tv_webapi_fromTime).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeLayout(indexTimeRange,1);
                    indexTimeRange = 1;

                }
            });
            menu_layout.findViewById(R.id.tv_webapi_toDate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeLayout(indexTimeRange,2);
                    indexTimeRange = 2;

                }
            });
            menu_layout.findViewById(R.id.tv_webapi_toTime).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeLayout(indexTimeRange,3);
                    indexTimeRange = 3;
                }
            });
        }
        public LinearLayout getLayout(){
            tv_fromDate.setTextColor(Color.RED);
            tv_fromTime.setTextColor(Color.BLACK);
            tv_toDate.setTextColor(Color.BLACK);
            tv_toTime.setTextColor(Color.BLACK);
            setDateOrTimePicker(0,menu_layout);
            return menu_layout;
        }
        public LinearLayout changeLayout(int index, int next_index){//index 1,3 date 2,4 time TODO:戻り値いらない？
            if(next_index == 3)
                btn_positive.setText("ダウンロード");
            else
                btn_positive.setText("次へ");
            switch (next_index){
                case 0:
                    tv_fromDate.setTextColor(Color.RED);
                    tv_fromTime.setTextColor(Color.BLACK);
                    tv_toDate.setTextColor(Color.BLACK);
                    tv_toTime.setTextColor(Color.BLACK);
                    break;
                case 1:
                    tv_fromDate.setTextColor(Color.BLACK);
                    tv_fromTime.setTextColor(Color.RED);
                    tv_toDate.setTextColor(Color.BLACK);
                    tv_toTime.setTextColor(Color.BLACK);
                    break;
                case 2:
                    tv_fromDate.setTextColor(Color.BLACK);
                    tv_fromTime.setTextColor(Color.BLACK);
                    tv_toDate.setTextColor(Color.RED);
                    tv_toTime.setTextColor(Color.BLACK);
                    break;
                case 3:
                    tv_fromDate.setTextColor(Color.BLACK);
                    tv_fromTime.setTextColor(Color.BLACK);
                    tv_toDate.setTextColor(Color.BLACK);
                    tv_toTime.setTextColor(Color.RED);
            }
            if(index % 2 == 0 && next_index % 2 == 0)
                return menu_layout;
            else if(index % 2 != 0 && next_index % 2 != 0)
                return menu_layout;
            if(next_index % 2 == 0){
                menu_layout.removeView(time_view);
                menu_layout.addView(date_view);
            }else {
                menu_layout.removeView(date_view);
                menu_layout.addView(time_view);
            }
            setDateOrTimePicker(next_index,menu_layout);
            return menu_layout;
        }
        void setDateOrTimePicker(final int index, LinearLayout layout) {

            if (index == 0 || index == 2) {
                DatePicker date_picker = (DatePicker) layout.findViewById(R.id.webapi_datePicker);
                date_picker.init(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if(index == 0){
                            tv_fromDate.setText(year+"年"+monthOfYear+"月"+dayOfMonth+"日");
                        }else if(index == 2){
                            tv_toDate.setText(year+"年"+monthOfYear+"月"+dayOfMonth+"日");
                        }
                    }
                });

            }else if (index == 1 || index == 3) {
                TimePicker time_picker = (TimePicker) layout.findViewById(R.id.webapi_timePicker);
                time_picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        if(index == 1){
                            tv_fromTime.setText(hourOfDay+"時"+minute+"分");
                        }else if(index == 3){
                            tv_toTime.setText(hourOfDay+"時"+minute+"分");
                        }
                    }
                });
            }
            /* version problem
            *
int hour;
int minute;
int currentApiVersion = Build.VERSION.SDK_INT;
if (currentApiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
    hour = timePicker.getHour();
    minute = timePicker.getMinute();
} else {
    hour = timePicker.getCurrentHour();
    minute = timePicker.getCurrentMinute();
}
             */
        }
    }
}