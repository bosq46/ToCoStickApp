package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kohki on 2016/02/24.
 */
public class ChartActivity extends Activity implements FileContract {
    private static final String TAG = "ChartAct";
    private static ChartActivity sInstance;

    private TextView mTvGraphYearMonth;
    public static boolean isPaintMoisture;
    public static boolean isPaintRadiation;
    public static boolean isPaintHumidity;
    public static boolean isPaintTemperature;
    public static boolean isPaintCumuTemp;

    public static final SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy'年'MM'月'dd'日'", Locale.JAPAN);
    public static final SimpleDateFormat sdf_ym  = new SimpleDateFormat("yyyy'年'MM'月'");

    private GraphMaker mGMaker;
    private int   mGraphScale = 1;//1:every year, 2:every month, 3:every week, 4:every 3days, 5:every 1day(priority -> 1,2,3)
    private String mDataSource = "asset";
    private static final String DATASOURCE_WEB = "web";
    private static final String DATASOURCE_WIRELESS = "wireless";
    private WebAPICommunication mWebAPI;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        sInstance = this;

        if(checkFile(this, WEB_DATA_FILE)){
            mDataSource = DATASOURCE_WEB;
            Toast.makeText(this,"Web取得したデータがあります",Toast.LENGTH_SHORT).show();
            if(!checkFile(this, WEB_EVERY_DAY_DATA_FILE)) {
                Toast.makeText(this,"日毎のデータ抽出中",Toast.LENGTH_SHORT).show();
                FileHelper.pickUpDistinguishingValue(WEB_DATA_FILE,WEB_EVERY_DAY_DATA_FILE);
            }
        }else if (checkFile(this,WIRELESS_DATA_FILE)){
                mDataSource = DATASOURCE_WIRELESS;
            Toast.makeText(this,"無線取得したデータがあります",Toast.LENGTH_SHORT).show();
            if(!checkFile(this, WIRELESS_EVERY_DAY_DATA_FILE) ) {
                Toast.makeText(this,"日毎のデータ抽出中",Toast.LENGTH_SHORT).show();
                FileHelper.pickUpDistinguishingValue(WIRELESS_DATA_FILE,WIRELESS_EVERY_DAY_DATA_FILE);
            }
        }else {
            //TODO:画面遷移
            Toast.makeText(this,"データがないので、取得してください",Toast.LENGTH_SHORT).show();
            //TODO:後々消す
            FileHelper.moveFromAssetsToLocal(this, ASSETS_FILE, WEB_DATA_FILE);
            mDataSource = DATASOURCE_WEB;
            if(!checkFile(this, WEB_EVERY_DAY_DATA_FILE) ) {
                FileHelper.pickUpDistinguishingValue(WEB_DATA_FILE, WEB_EVERY_DAY_DATA_FILE);
            }
        }

        mGMaker = new GraphMaker((LineChart) findViewById(R.id.chart));
        mTvGraphYearMonth = (TextView) findViewById(R.id.tv_graph_year_month);
        createChart();

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
                Date graph_date = mGMaker.mLatestMonth;
                Calendar cal = Calendar.getInstance();
                cal.setTime(graph_date);
                cal.add(Calendar.MONTH, -1);
                mTvGraphYearMonth.setText(sdf_ym.format(cal.getTime()));
                mGMaker.mLatestMonth = cal.getTime();
            }
        });
        findViewById(R.id.btn_month_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date graph_date = mGMaker.mLatestMonth;
                Calendar cal = Calendar.getInstance();
                cal.setTime(graph_date);
                cal.add(Calendar.MONTH, 1);

                mTvGraphYearMonth.setText(sdf_ym.format(cal.getTime()));
                mGMaker.mLatestMonth = cal.getTime();
                Log.d(TAG, sdf_ym.format(mGMaker.mLatestMonth));
            }
        });
        mWebAPI = new WebAPICommunication();
        findViewById(R.id.btn_get_webapi).setOnClickListener(mWebAPI.generateWebDataDLLictener());

    }

    private boolean checkFile(Context context, String file_name){
        File file = context.getFileStreamPath(file_name);
        return file.exists();
    }
    private void createChart(){
        Date graph_latest_date = new Date();
        switch (mDataSource) {
            case DATASOURCE_WEB:
                if (mGraphScale <= 3) {
                    mGMaker.makeLineChart(WEB_EVERY_DAY_DATA_FILE);
                }else {
                    mGMaker.makeLineChart(WEB_DATA_FILE,mGraphScale);//mGraphScale -> 2,3 ?
                }
                break;
            case DATASOURCE_WIRELESS:
                if (mGraphScale <= 3){
                    mGMaker.makeLineChart(WIRELESS_EVERY_DAY_DATA_FILE);
                }else {
                    mGMaker.makeLineChart(WIRELESS_DATA_FILE,mGraphScale);
                }
                break;
        }
        mTvGraphYearMonth.setText(sdf_ym.format(graph_latest_date));
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
                        createChart();
                    }
                });
                break;
            case R.id.btn_humidity:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintHumidity =! isPaintHumidity;
                        setBtnColor((Button) v);
                        createChart();
                    }
                });
                break;
            case R.id.btn_radiation:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintRadiation =! isPaintRadiation;
                        setBtnColor((Button) v);
                        createChart();
                    }
                });
                break;
            case R.id.btn_moisture:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintMoisture =! isPaintMoisture;
                        setBtnColor((Button) v);
                        createChart();
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
    ///    mGMaker.makeLineChart(mGraphScale);
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
        createChart();
        super.onConfigurationChanged(newConfig);
    }
}

