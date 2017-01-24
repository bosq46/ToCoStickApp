package com.example.kohki.tocostickapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;

/**
 * Created by Kohki on 2017/01/16.
 */

public class GraphData {
    private static final String TAG = "GraphDataCls";
    protected static SQLiteDatabase mDB;

    private static final int TIME_COLUMN_NUM         = 0;
    private static final int TEMPERATURE_COLUMN_NUM = 1;
    private static final int HUMIDITY_COLUMN_NUM    = 2;
    private static final int RADIATION_COLUMN_NUM   = 9;
    private static final int MOISTURE_COLUMN_NUM    = 12;
    GraphData(Context context, LineChart lineChart){
        /*DB not use
        mDbHelper = new SensorDBHelper(context);
        mDB = mDbHelper.getWritableDatabase();
        FileHelper.setDbFromCsv(context,,mDB);
        */
    }
    public LineData getLineDataOfEveryDay(String data_file){
        ArrayList<String> al_labels     = new ArrayList<String>();
       // ArrayList<Entry>  al_date       = new ArrayList<>();
        ArrayList<Entry>  al_temp       = new ArrayList<>();
        ArrayList<Entry>  al_humi       = new ArrayList<>();
        ArrayList<Entry>  al_radi       = new ArrayList<>();
        ArrayList<Entry>  al_mois       = new ArrayList<>();
        ArrayList<Entry>  al_cumu_temp  = new ArrayList<>();

        BufferedReader in = null;
        try {
            FileInputStream file = ChartActivity.getInstance().openFileInput(data_file);
            in = new BufferedReader(new InputStreamReader(file));
            String line;
            int i = 0;
            float cumu_temp = 0;
            String time="";
            while ( (line = in.readLine()) != null){
                Log.d(TAG+"L69",line);
                if(i==0) {//TODO最高気温などのデータファイルは最初飛ばさない
                    i++;
                    continue;
                }
                String[] elements = line.split(",");
            //    Log.d(TAG,elements[TEMPERATURE_COLUMN_NUM]);
            //    Log.d(TAG,elements[HUMIDITY_COLUMN_NUM]);
            //    Log.d(TAG,elements[TIME_COLUMN_NUM]);
                time = elements[TIME_COLUMN_NUM];
                Float temp = Float.parseFloat(elements[TEMPERATURE_COLUMN_NUM]);
                Float humi = Float.parseFloat(elements[HUMIDITY_COLUMN_NUM]);
                Float radi = Float.parseFloat(elements[RADIATION_COLUMN_NUM]);
                Float mois = Float.parseFloat(elements[MOISTURE_COLUMN_NUM]);
                cumu_temp += temp;
                al_labels.add(elements[0]);
                al_temp.add(new Entry(temp, i));
                al_humi.add(new Entry(humi, i));
                al_radi.add(new Entry(radi, i));
                al_mois.add(new Entry(mois, i));
                al_cumu_temp.add(new Entry(cumu_temp, i));
                i++;
            }
            GraphMaker.mLatestMonth = FileHelper.sdf_csv.parse(time);
            in.close();
        }catch(FileNotFoundException e){
            Log.e(TAG,e.toString());
        }catch(IOException e){
            Log.e(TAG,e.toString());
        }catch (ParseException e) {
            Log.e(TAG, e.toString());
        }

        ArrayList<Integer> colors = new ArrayList<>();
        LineData data             = new LineData(al_labels);

        if(ChartActivity.isPaintTemperature){
            LineDataSet data_set = new LineDataSet(al_temp, "気温");// <- 環境データセット
            colors.add(ColorTemplate.COLORFUL_COLORS[1]);
            data_set.setColors(colors);
            data_set.setDrawValues(true);
            data_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[1]);
            data_set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                    return v +"℃";
                }
            });
            data_set.setAxisDependency(YAxis.AxisDependency.LEFT);
            data.addDataSet(data_set);
        }
        if(ChartActivity.isPaintHumidity){
            LineDataSet humi_set = new LineDataSet(al_humi, "湿度");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.JOYFUL_COLORS[4]);//ColorTemplate.COLORFUL_COLORS[4]
            humi_set.setColors(colors);
            humi_set.setDrawValues(true);
            humi_set.setCircleColor(ColorTemplate.JOYFUL_COLORS[4]);
            humi_set.setValueFormatter(new PercentFormatter());
            humi_set.setAxisDependency(YAxis.AxisDependency.LEFT);
            data.addDataSet(humi_set);
        }
        if(ChartActivity.isPaintRadiation){
            LineDataSet data_set = new LineDataSet(al_radi, "日射量");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[2]);
            data_set.setColors(colors);
            data_set.setDrawValues(true);
            data_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[2]);
            data_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data_set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                    return v +"V";
                }
            });
            data.addDataSet(data_set);
        }
        if(ChartActivity.isPaintMoisture) {
            LineDataSet data_set = new LineDataSet(al_mois, "土壌湿度");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[0]);
            data_set.setColors(colors);
            data_set.setDrawValues(true);
            data_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[0]);
            data_set.setValueFormatter(new PercentFormatter());
            data_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(data_set);
        }
        if(ChartActivity.isPaintCumuTemp) {
            LineDataSet data_set = new LineDataSet(al_cumu_temp, "積算温度");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[0]);
            data_set.setColors(colors);
            data_set.setDrawValues(true);
            data_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[0]);
            data_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            //            data_set.setAxisDependency(YAxis.AxisDependency.LEFT);
            data_set.setDrawFilled(true);
            data_set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                    return v +"℃";
                }
            });
            data.addDataSet(data_set);
        }

        // テキストの設定
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);

        return data;
    }

    public LineData getLineDataOfEveryMonth(String file_name){
        ArrayList<String> al_labels = new ArrayList<String>();
        ArrayList<Entry>  al_ave = new ArrayList<>();
        ArrayList<Entry>  al_hig = new ArrayList<>();
        ArrayList<Entry>  al_min = new ArrayList<>();
        ArrayList<Entry>  al_cum = new ArrayList<>();
        try {
            FileInputStream file = ChartActivity.getInstance().openFileInput(file_name);
            BufferedReader in = new BufferedReader(new InputStreamReader(file));
            Log.d(TAG,"file_name"+file_name);
            String line;
            int i = 0;
            float cumu_temp = 0;
            while ((line = in.readLine()) != null) {
                Log.d(TAG + "getData", line);
                String[] elements = line.split(",");
            //    Date d = sdf_
                al_labels.add(elements[0]);
                float ave_temp = Float.parseFloat(elements[1]);
                al_ave.add(new Entry(ave_temp, i));
                al_hig.add(new Entry(Float.parseFloat(elements[2]), i));
                al_min.add(new Entry(Float.parseFloat(elements[3]), i));
        //        al_min.add(new Entry(Float.parseFloat(elements[4]), i));
                i++;
            }
            in.close();
        } catch (IOException e) {
            Log.e(TAG,e.toString());
            Toast.makeText(ChartActivity.getInstance(), "" + e, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Log.e(TAG,e.toString());
            Toast.makeText(ChartActivity.getInstance(), "" + e, Toast.LENGTH_SHORT).show();
        }

        ArrayList<Integer> colors = new ArrayList<>();
        LineData data             = new LineData(al_labels);

        LineDataSet ave_set = new LineDataSet(al_ave, "平均気温");// <- 環境データセット
        colors.add(ColorTemplate.COLORFUL_COLORS[2]);
        ave_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[2]);
        ave_set.setColors(colors);
        ave_set.setDrawValues(true);
        ave_set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                    return v +"℃";
            }
            });
        ave_set.setAxisDependency(YAxis.AxisDependency.LEFT);
        data.addDataSet(ave_set);


        LineDataSet hig_set = new LineDataSet(al_hig, "最高気温");
        colors = new ArrayList<>();
        colors.add(ColorTemplate.COLORFUL_COLORS[1]);
        hig_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[1]);
        hig_set.setColors(colors);
        hig_set.setDrawValues(true);
        hig_set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return v +"℃";
            }
        });
        hig_set.setAxisDependency(YAxis.AxisDependency.LEFT);
        data.addDataSet(hig_set);

        LineDataSet min_set = new LineDataSet(al_min, "最低気温");
        colors = new ArrayList<>();
        colors.add(ColorTemplate.JOYFUL_COLORS[4]);
        min_set.setCircleColor(ColorTemplate.JOYFUL_COLORS[4]);
        min_set.setColors(colors);
        min_set.setDrawValues(true);
        min_set.setAxisDependency(YAxis.AxisDependency.LEFT);
        min_set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return v +"℃";
            }
        });
        data.addDataSet(min_set);
/*
        LineDataSet cum_set = new LineDataSet(al_min, "積算温度");
        colors = new ArrayList<>();
        colors.add(ColorTemplate.COLORFUL_COLORS[2]);
        data_set.setColors(colors);
        data_set.setDrawValues(true);
        data_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[2]);
        data_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        data_set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return v +"℃";
            }
        });
        data.addDataSet(data_set);
*/
        // テキストの設定
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);

        return data;
    }
}
