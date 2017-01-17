package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.ksksue.driver.serial.FTDriver;

import static com.example.kohki.tocostickapp.ReceiveThreadHelper.mContext;

/**
 * Created by Kohki on 2016/02/24.
 */
public class ChartActivity extends Activity {
    private static final String TAG = "ChartAct";
    private static ChartActivity sInstance;

    private TextView mTvGraphYearMonth;
    public static boolean isPaintMoisture;
    public static boolean isPaintRadiation;
    public static boolean isPaintHumidity;
    public static boolean isPaintTemperature;
    public static boolean isPaintCumuTemp;

    static final SimpleDateFormat sdf_csv = new SimpleDateFormat("yyyy'/'MM'/'dd HH:mm", Locale.JAPAN);
    static final SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy'年'MM'月'dd'日'", Locale.JAPAN);
    static final SimpleDateFormat sdf_ym  = new SimpleDateFormat("yyyy'年'MM'月'");

    private GraphMaker mGMaker;
    private int mGraphScale = 1;//0:every year, 1:every month, 2:every week, 3:every 3days, 4:every 1day(priority -> 1,2,3)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        sInstance = this;

        mGMaker = new GraphMaker((LineChart) findViewById(R.id.chart));
        mTvGraphYearMonth = (TextView) findViewById(R.id.tv_graph_date);

        try {
            Date formatDate = sdf_csv.parse(mGMaker.mNowYearMonth);
            mTvGraphYearMonth.setText(sdf_ym.format(formatDate));
            Log.d(TAG, sdf_ym.format(formatDate));
        }catch (ParseException e){}

        isPaintTemperature = false;
        isPaintHumidity    = false;
        isPaintRadiation   = false;
        isPaintMoisture    = false;
        isPaintCumuTemp    = false;

        Button btn_temp = (Button) findViewById(R.id.btn_temperature);
        Button btn_humi = (Button) findViewById(R.id.btn_humidity);
        Button btn_radi = (Button) findViewById(R.id.btn_radiation);
        Button btn_mois = (Button) findViewById(R.id.btn_moisture);

        setBtnColor(btn_temp);
        setElementBtnCfg(btn_temp);

        setBtnColor(btn_humi);
        setElementBtnCfg(btn_humi);

        setBtnColor(btn_radi);
        setElementBtnCfg(btn_radi);

        setBtnColor(btn_mois);
        setElementBtnCfg(btn_mois);

        findViewById(R.id.btn_record_ventilation).setOnClickListener(new VentilationRecBtnClickListener());
        findViewById(R.id.btn_month_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Date graph_date = sdf_ym.parse(mTvGraphYearMonth.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(graph_date);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    mTvGraphYearMonth.setText(sdf_ym.format(cal.getTime()));
                }catch (ParseException e){
                    Log.d(TAG,e.toString());
                }
            }
        });
        findViewById(R.id.btn_month_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Date graph_date = sdf_ym.parse(mTvGraphYearMonth.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(graph_date);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    mTvGraphYearMonth.setText(sdf_ym.format(cal.getTime()));
                }catch (ParseException e){
                    Log.d(TAG,e.toString());
                }
            }
        });
    }

    private void setBtnColor(Button btn){
        switch (btn.getId()) {
            case R.id.btn_temperature:
                if (isPaintTemperature) {
                    btn.setBackgroundColor(Color.rgb(255, 94, 25));
                    btn.setAlpha(1f);
                    break;
                } else {
                    btn.setBackgroundColor(Color.rgb(250, 250, 250));
                    btn.setAlpha(0.7f);
                }
                break;
            case R.id.btn_humidity:
                if (isPaintHumidity) {
                    btn.setBackgroundColor(Color.rgb(0,255, 255));
                    btn.setAlpha(1f);
                }else {
                    btn.setBackgroundColor(Color.rgb(250, 250, 250));
                    btn.setAlpha(0.7f);
                }
                break;
            case R.id.btn_radiation:
                if (isPaintRadiation) {
                    btn.setBackgroundColor(Color.rgb(255,241,15));
                    btn.setAlpha(1f);
                } else {
                    btn.setBackgroundColor(Color.rgb(250, 250, 250));
                    btn.setAlpha(0.7f);
                }
                break;
            case R.id.btn_moisture:
                if (isPaintMoisture) {
                    btn.setBackgroundColor(Color.rgb(177, 104, 51));
                    btn.setAlpha(1f);
                } else {
                    btn.setBackgroundColor(Color.rgb(250, 250, 250));
                    btn.setAlpha(0.7f);
                }
                break;
        }
    }
    private void setElementBtnCfg(Button btn){
        switch (btn.getId()){
            case R.id.btn_temperature:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintTemperature =! isPaintTemperature;
                        isPaintCumuTemp =! isPaintCumuTemp;
                        setBtnColor((Button) v);
                        mGMaker.makeLineChart(mGraphScale);
                    }
                });
                break;
            case R.id.btn_humidity:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintHumidity =! isPaintHumidity;
                        setBtnColor((Button) v);
                        mGMaker.makeLineChart(mGraphScale);
                    }
                });
                break;
            case R.id.btn_radiation:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintRadiation =! isPaintRadiation;
                        setBtnColor((Button) v);
                        mGMaker.makeLineChart(mGraphScale);

                    }
                });
                break;
            case R.id.btn_moisture:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintMoisture =! isPaintMoisture;
                        setBtnColor((Button) v);
                        mGMaker.makeLineChart(mGraphScale);
                    }
                });
                break;
        }
    }
    public static synchronized ChartActivity getInstance() {
        return sInstance;
    }
    @Override
    public void onResume(){
        super.onResume();
        mGMaker.makeLineChart(mGraphScale);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        /*
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:  // 縦長
                // 処理
                break;
            case Configuration.ORIENTATION_LANDSCAPE:  // 横長
                // 処理
                break;
            default:
                break;
        }*/
        mGMaker.makeLineChart(mGraphScale);
        super.onConfigurationChanged(newConfig);
    }
}

