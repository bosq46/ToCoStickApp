package com.example.kohki.tocostickapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by Kohki on 2016/02/24.
 */
public class ChartActivity extends FragmentActivity {
    private static final String TAG = "ChartAct";
    private static ChartActivity sInstance;

    private static TextView mTvGraphYearMonth;
    public static int DayRangeOfGraph = 30;
    private static int[] DayRangeOfGraphList = {3,30};// {1,3,7,14,30};
    public static boolean isPaintRadiation;
    public static boolean isPaintCumuTemp;
    public static boolean isPaintVentilation;

    public static final SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy'年'MM'月'dd'日'", Locale.JAPAN);
    public static final SimpleDateFormat sdf_ym  = new SimpleDateFormat("yyyy'年'MM'月'");

    private static GraphMaker mGMaker;
  //  private int   mGraphScale = 1;//1:every year, 2:every month, 3:every week, 4:every 3days, 5:every 1day(priority -> 1,2,3)
    private String mDataSource = "asset";
    private static final String DATASOURCE_WEB = "web";
    private static final String DATASOURCE_WIRELESS = "wireless";
    private WebAPICommunication mWebAPI;

    public static Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        sInstance = this;
        mHandler = new Handler();
        /*
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
        */

        mWebAPI = new WebAPICommunication();
    //    mWebAPI.getToken();
    //    mWebAPI.downloadSensorData();
    //TODO:after,select download range
    //    findViewById(R.id.btn_get_webapi).setOnClickListener(mWebAPI);
        findViewById(R.id.btn_get_webapi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) ChartActivity.getInstance().getSystemService(ChartActivity.getInstance().CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    mWebAPI.getToken();
                    mWebAPI.downloadSensorData();
                }else {
                    Toast.makeText(ChartActivity.getInstance(), "インターネットに接続してください",Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btn_record_ventilation).setOnClickListener(new VentilationRecBtnClickListener());

        mGMaker = new GraphMaker((LineChart) findViewById(R.id.chart), (TextView) findViewById(R.id.tv_graph_year_month) );
        mTvGraphYearMonth = (TextView) findViewById(R.id.tv_graph_year_month);

        isPaintRadiation   = false;
        isPaintCumuTemp    = false;
        isPaintVentilation = false;

        Button btn_radi = (Button) findViewById(R.id.btn_show_radiation);
        Button btn_cumu = (Button) findViewById(R.id.btn_show_cumulative);
        Button btn_vent = (Button) findViewById(R.id.btn_show_ventilation);

        setBtnColor(btn_radi);
        setElementBtnListener(btn_radi);

        setBtnColor(btn_cumu);
        setElementBtnListener(btn_cumu);

        setBtnColor(btn_vent);
        setElementBtnListener(btn_vent);

        /*
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
        */
        //TODO: NavigaationDrawer
    /*    IDSelecterFragment fragment = new IDSelecterFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.drawer, fragment);
        transaction.commit();
        */

        findViewById(R.id.btn_change_field).setOnClickListener(new FieldChangeBtnClickListener());
        Button btn_sclale = (Button) findViewById(R.id.btn_change_scale);
        btn_sclale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                changeTimeRangeOfGraph();
                btn.setText(DayRangeOfGraph+"日間");
            }
        });
        btn_sclale.setText(DayRangeOfGraph+"日間");
    }

    public static void changeTimeRangeOfGraph(){
        for(int i=0;i<DayRangeOfGraphList.length;i++){
            if(DayRangeOfGraph == DayRangeOfGraphList[i]){
                if(i<DayRangeOfGraphList.length-1)
                    DayRangeOfGraph = DayRangeOfGraphList[i+1];
                else
                    DayRangeOfGraph = DayRangeOfGraphList[0];
                break;
            }
        }
    }
    /* createChart(String file_name,String graph_title) はダウンロードした後に実行される */
    public static void createChart(String file_name, String graph_title){
        mGMaker.makeLineChart(file_name, DayRangeOfGraph);
        mTvGraphYearMonth.setText(graph_title);
        /*
        switch (mDataSource) {
            case DATASOURCE_WEB:
                if (mGraphScale <= 3) {
                    mGMaker.makeLineChart(FileContract.WEB_EVERY_DAY_DATA_FILE);
                }else {
                    mGMaker.makeLineChart(FileContract.WEB_DATA_FILE,mGraphScale);//mGraphScale -> 2,3 ?
                }
                break;
            case DATASOURCE_WIRELESS:
                if (mGraphScale <= 3){
                    mGMaker.makeLineChart(FileContract.WIRELESS_EVERY_DAY_DATA_FILE);
                }else {
                    mGMaker.makeLineChart(FileContract.WIRELESS_DATA_FILE,mGraphScale);
                }
                break;
        }
        */
    }
    /* createChart(String file_name,String graph_title) はボタンなどのリスナーで呼ばれる */
    public static void createChart(){
        FileContract mID = new FileContract(ChartActivity.getInstance());
        String file_name;
        if(DayRangeOfGraph > 3)
            file_name = mID.getGateWayID()+"_"+mID.getNodeID()+"_days.csv";
        else
            file_name = mID.getGateWayID()+"_"+mID.getNodeID()+".csv";
        ArrayList al_file = FileHelper.readFile(ChartActivity.getInstance(), file_name);
        if(al_file.size() == 0) {
            Toast.makeText(ChartActivity.getInstance(), "データがありません。\nダウンロードしてください", Toast.LENGTH_SHORT).show();
            return;
        }
        String latest_date =  ((String)al_file.get(al_file.size()-1)).split(",")[0];
        String[] ymd = latest_date.split("/");
        String latest_ym = ymd[0]+"年"+ymd[1]+"月";
        Log.d(TAG, "Graph title " + latest_ym);
        createChart(file_name, latest_ym);
    }

    private void setBtnColor(Button btn){
        switch (btn.getId()) {
            case R.id.btn_show_radiation:
                if (isPaintRadiation) {
                    btn.setBackgroundColor(Color.rgb(255,241,15));
                    btn.setAlpha(1f);
                } else {
                    btn.setBackgroundColor(Color.rgb(250, 250, 250));
                    btn.setAlpha(0.7f);
                }
                break;
            case R.id.btn_show_cumulative:
                if (isPaintCumuTemp) {
                    btn.setBackgroundColor(Color.rgb(255, 94, 25));
                    btn.setAlpha(1f);
                    break;
                } else {
                    btn.setBackgroundColor(Color.rgb(250, 250, 250));
                    btn.setAlpha(0.7f);
                }
                break;
            case R.id.btn_show_ventilation:
                if (isPaintVentilation) {
                    btn.setBackgroundColor(Color.rgb(0,255, 255));
                    btn.setAlpha(1f);
                }else {
                    btn.setBackgroundColor(Color.rgb(250, 250, 250));
                    btn.setAlpha(0.7f);
                }
                break;
        }
    }
    private void setElementBtnListener(Button btn){
        switch (btn.getId()){
            case R.id.btn_show_radiation:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintRadiation = !isPaintRadiation;
                        setBtnColor((Button) v);
                        createChart();
                    }
                });
                break;
            case R.id.btn_show_cumulative:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintCumuTemp = !isPaintCumuTemp;
                        setBtnColor((Button) v);
                        createChart();
                    }
                });
                break;
            case R.id.btn_show_ventilation:
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPaintVentilation = !isPaintVentilation;
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
        createChart();
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

