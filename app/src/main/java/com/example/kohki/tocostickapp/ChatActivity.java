package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;

import jp.ksksue.driver.serial.FTDriver;

import static android.R.attr.breadCrumbShortTitle;
import static android.R.attr.entries;
import static android.R.attr.value;

/**
 * Created by Kohki on 2016/02/24.
 */
public class ChatActivity extends Activity {

    private static final String TAG = "ChatAct";
    private static final String ACTION_USB_PERMISSION =  "jp.ksksue.tutorial.USB_PERMISSION";//"com.example.kohki.USB_PERMISSION";
    private final int SERIAL_BAUDRATE = FTDriver.BAUD115200;
    private static Context mContext;

    private FTDriver mSerial;
    private boolean isMainLoopRunning = false;
    private Handler      mHandler;

    private boolean isPaintMoisture;
    private boolean isPaintRadiation;
    private boolean isPaintHumidity;
    private boolean isPaintTemperature;

    private LineChart mChart;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mContext = this;

        createEnvChart();

        isPaintTemperature = false;
        isPaintHumidity    = false;
        isPaintRadiation   = false;
        isPaintMoisture    = false;

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
    }
    private void createEnvChart() {
        mChart = (LineChart) findViewById(R.id.chart);
        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setData(getGraphData());
     //                                                                                                                                                                                                                                      mChart.set
        //  ラインの凡例の設定
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        //    xl.setLabelsToSkip(9);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        if(isPaintTemperature) {
            leftAxis.setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float v, YAxis yAxis) {
                    return v +"℃";
                }
            });
        }else if(isPaintHumidity) {
            leftAxis.setValueFormatter(new PercentFormatter());
        }
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
    //    rightAxis.setAxisMaxValue(0);
    //    rightAxis.setAxisMinValue(100f);
    //    rightAxis.setStartAtZero(true);
    //    rightAxis.setEnabled(true);
        rightAxis.setDrawGridLines(true);

        mChart.setDescription("");
        mChart.animateX(500, Easing.EasingOption.EaseInQuad);
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private LineData getGraphData(){
        ArrayList al_elements_data = FileHelper.readCsvFile(mContext,"BTVILOG1.CSV");

        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Entry> date  = new ArrayList<>();
        ArrayList<Entry> temp  = new ArrayList<>();
        ArrayList<Entry> humi  = new ArrayList<>();
        ArrayList<Entry> radi  = new ArrayList<>();
        ArrayList<Entry> mois  = new ArrayList<>();

        try {
            for (int i = 0; i < al_elements_data.size(); i++) {
                String[] elements = (String[]) al_elements_data.get(i);
                labels.add(elements[0]);
            //    date.add(new Entry(Float.parseFloat(elements[0]),i));
                temp.add(new Entry(Float.parseFloat(elements[1]),i));
                humi.add(new Entry(Float.parseFloat(elements[2]),i));
                radi.add(new Entry(Float.parseFloat(elements[3]),i));
                mois.add(new Entry(Float.parseFloat(elements[4]),i));
            }
        }catch (Exception e){
            Toast.makeText(this,""+e,Toast.LENGTH_SHORT).show();
        }

        ArrayList<Integer> colors = new ArrayList<>();
        LineData data = new LineData(labels);

        if(isPaintTemperature){
            LineDataSet temp_set = new LineDataSet(temp, "気温");
            // 色の設定
            colors.add(ColorTemplate.COLORFUL_COLORS[1]);
            temp_set.setColors(colors);
            temp_set.setDrawValues(true);
            temp_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[1]);
        //    temp_set.setDrawFilled(true);
            temp_set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                    return v +"℃";
                }
            });
            temp_set.setAxisDependency(YAxis.AxisDependency.LEFT);
            data.addDataSet(temp_set);
        }
        if(isPaintHumidity){
            LineDataSet humi_set = new LineDataSet(humi, "湿度");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.JOYFUL_COLORS[4]);//ColorTemplate.COLORFUL_COLORS[4]
            humi_set.setColors(colors);
            humi_set.setDrawValues(true);
            humi_set.setCircleColor(ColorTemplate.JOYFUL_COLORS[4]);
            humi_set.setValueFormatter(new PercentFormatter());
            humi_set.setAxisDependency(YAxis.AxisDependency.LEFT);
            data.addDataSet(humi_set);
        }
        if(isPaintRadiation){
            LineDataSet radi_set = new LineDataSet(radi, "日射量");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setColors(colors);
            radi_set.setDrawValues(true);
            radi_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(radi_set);
        }
        if(isPaintMoisture) {
            LineDataSet mois_set = new LineDataSet(mois, "土壌湿度");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[0]);
            mois_set.setColors(colors);
            mois_set.setDrawValues(true);
            mois_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[0]);
            mois_set.setValueFormatter(new PercentFormatter());
            mois_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(mois_set);

        }

        // テキストの設定
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);

        return data;
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
                        setBtnColor((Button) v);
                        createEnvChart();
                    }
                });
                break;
            case R.id.btn_humidity:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintHumidity =! isPaintHumidity;
                        setBtnColor((Button) v);
                        createEnvChart();
                    }
                });
                break;
            case R.id.btn_radiation:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintRadiation =! isPaintRadiation;
                        setBtnColor((Button) v);
                        createEnvChart();
                    }
                });
                break;
            case R.id.btn_moisture:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintMoisture =! isPaintMoisture;
                        setBtnColor((Button) v);
                        createEnvChart();
                    }
                });
                break;
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        createEnvChart();
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
        createEnvChart();
        super.onConfigurationChanged(newConfig);
    }

}
