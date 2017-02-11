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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

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

    public LineData getLineDataOfDay(String file_name){
        ArrayList<String> al_labels = new ArrayList<String>();
        ArrayList<Entry>  al_ave = new ArrayList<>();
        ArrayList<Entry>  al_hig = new ArrayList<>();
        ArrayList<Entry>  al_min = new ArrayList<>();
        ArrayList<Entry>  al_ven = new ArrayList<>();
        ArrayList<Entry>  al_cum = new ArrayList<>();
        ArrayList<Entry>  al_rad = new ArrayList<>();

        try {
            FileInputStream file = ChartActivity.getInstance().openFileInput(file_name);
            BufferedReader in   = new BufferedReader(new InputStreamReader(file));
            String line;
            int cnt_file_lines = 0;

            while ((line = in.readLine()) != null) {
                Log.d(TAG + "Read data", line);
                String[] elements = line.split(",");
                al_labels.add(elements[0]);

                if(ChartActivity.isPaintVentilation){
                    FileContract mID = new FileContract(ChartActivity.getInstance());
                    ArrayList al_vent_file = FileHelper.readFile(ChartActivity.getInstance(), mID.getGateWayID()+"_"+mID.getNodeID()+"_vent.csv");
                //    Log.d(TAG,"VENTILATION size "+al_vent_file.size());
                    for(int vent_fileline=0;vent_fileline<al_vent_file.size();vent_fileline++){
                        String ventilation_date = "";
                        String graph_date = "";
                        graph_date = elements[0];

                        String[] vent_line = ((String)al_vent_file.get(vent_fileline)).split(",");
                    //    Log.d(TAG,"ventilation_date"+vent_line[0]+" - "+graph_date);
                        if(graph_date.equals(vent_line[0])){
                            float vent_par = Float.parseFloat(vent_line[1]);
                            al_ven.add(new Entry(vent_par, cnt_file_lines));
                        }
                    }
                }

                al_ave.add(new Entry(Float.parseFloat(elements[1]), cnt_file_lines));
                al_hig.add(new Entry(Float.parseFloat(elements[2]), cnt_file_lines));
                al_min.add(new Entry(Float.parseFloat(elements[3]), cnt_file_lines));
                if(ChartActivity.isPaintCumuTemp) {
                    al_cum.add(new Entry(Float.parseFloat(elements[4]) / 500, cnt_file_lines)); //TODO:値の重みを決める.
                    Log.d(TAG,"Cumu "+cnt_file_lines+" "+elements[4]);
                }if(ChartActivity.isPaintRadiation) {
                    al_rad.add(new Entry(Float.parseFloat(elements[5]) / 10 , cnt_file_lines));
                    Log.d(TAG,"Radiation "+cnt_file_lines+" "+elements[5]);
                }
                cnt_file_lines++;
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

        if(ChartActivity.isPaintRadiation) {
            LineDataSet radi_set = new LineDataSet(al_rad, "日射量");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setColors(colors);
            radi_set.setCircleRadius(0);
            radi_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setFillColor(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setDrawValues(false);
            radi_set.setDrawFilled(false);
            radi_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(radi_set);
        }

        if(ChartActivity.isPaintCumuTemp) {
            LineDataSet cumu_set = new LineDataSet(al_cum, "積算温度");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[1]);
            cumu_set.setColors(colors);
            cumu_set.setCircleRadius(0);
            cumu_set.setFillColor(ColorTemplate.VORDIPLOM_COLORS[2]);
            cumu_set.setDrawValues(false);
            cumu_set.setDrawFilled(true);
            cumu_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(cumu_set);
        }

        if(ChartActivity.isPaintVentilation) {
            LineDataSet vent_set = new LineDataSet(al_ven, "換気率");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.JOYFUL_COLORS[3]);
            vent_set.setColors(colors);
            vent_set.setDrawStepped(true);
            vent_set.setCircleRadius(0);
            vent_set.setCircleColor(ColorTemplate.JOYFUL_COLORS[3]);
            vent_set.setFillColor(ColorTemplate.JOYFUL_COLORS[3]);
            vent_set.setDrawValues(false);
            vent_set.setDrawFilled(true);
            vent_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            vent_set.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                    return v + "%";
                }
            });
            data.addDataSet(vent_set);
        }

        LineDataSet ave_set = new LineDataSet(al_ave, "平均気温");// <- 環境データセット
        colors = new ArrayList<>();
        colors.add(Color.rgb(25, 25, 25));
        ave_set.setCircleColor(Color.rgb(25, 25, 25));
        ave_set.setCircleColorHole(Color.rgb(25, 25, 25));
        ave_set.setColors(colors);
        ave_set.setLineWidth(1.8f);
        ave_set.setDrawValues(false);
        ave_set.setAxisDependency(YAxis.AxisDependency.LEFT);
        data.addDataSet(ave_set);

        LineDataSet hig_set = new LineDataSet(al_hig, "最高気温");
        colors = new ArrayList<>();
        colors.add(Color.rgb(250, 0, 0));
        hig_set.setCircleColor(Color.rgb(250, 0, 0));
        hig_set.setCircleColorHole(Color.rgb(250, 0, 0));
        hig_set.setColors(colors);
        hig_set.setLineWidth(1.8f);
        hig_set.setDrawValues(false);
        hig_set.setAxisDependency(YAxis.AxisDependency.LEFT);
        data.addDataSet(hig_set);

        LineDataSet min_set = new LineDataSet(al_min, "最低気温");
        colors = new ArrayList<>();
        colors.add(ColorTemplate.JOYFUL_COLORS[4]);
        min_set.setCircleColor(ColorTemplate.JOYFUL_COLORS[4]);
        min_set.setCircleColorHole(ColorTemplate.JOYFUL_COLORS[4]);
        min_set.setColors(colors);
        min_set.setLineWidth(1.8f);
        min_set.setDrawValues(false);
        min_set.setAxisDependency(YAxis.AxisDependency.LEFT);
        /*
        min_set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return v +"℃";
            }
        });
        */
        data.addDataSet(min_set);

        data.setValueTextSize(16f);
        data.setValueTextColor(Color.BLACK);

        return data;
    }
    public LineData getLineDataOfAllTime(String file_name){

        ArrayList<String> al_labels = new ArrayList<String>();
        ArrayList<Entry>  al_temp   = new ArrayList<>();
        ArrayList<Entry>  al_cum    = new ArrayList<>();
        ArrayList<Entry>  al_rad    = new ArrayList<>();
        ArrayList<Entry>  al_ven    = new ArrayList<>();

        try {
            FileInputStream file = ChartActivity.getInstance().openFileInput(file_name);
            BufferedReader in   = new BufferedReader(new InputStreamReader(file));
            String line;
            int cnt_file_lines = 0;

            SimpleDateFormat sdf_ymdhm = new SimpleDateFormat("yyyy'/'MM'/'dd HH:mm");
            SimpleDateFormat sdf_ymd   = new SimpleDateFormat("yyyy'/'MM'/'dd");

            while ((line = in.readLine()) != null) {
                String[] elements = line.split(",");
                Date line_date = sdf_ymdhm.parse(elements[0]);
                Calendar line_cal = Calendar.getInstance();
                Calendar now_cal = Calendar.getInstance();
                line_cal.setTime(line_date);
                now_cal.setTime(new Date());

                if(now_cal.getTimeInMillis() - line_cal.getTimeInMillis() > 2*24*60*60*1000)
                    continue;
                al_labels.add(elements[0]);

                if(ChartActivity.isPaintVentilation){
                    FileContract mID = new FileContract(ChartActivity.getInstance());
                    ArrayList al_vent_file = FileHelper.readFile(ChartActivity.getInstance(), mID.getGateWayID()+"_"+mID.getNodeID()+"_vent.csv");
                    //    Log.d(TAG,"VENTILATION size "+al_vent_file.size());
                    for(int vent_fileline=0; vent_fileline<al_vent_file.size(); vent_fileline++){
                        String ventilation_date = "";
                        String graph_date = "";
                        graph_date = sdf_ymd.format(sdf_ymdhm.parse(elements[0]));
                        String[] vent_line = ((String)al_vent_file.get(vent_fileline)).split(",");
                        //    Log.d(TAG,"ventilation_date"+vent_line[0]+" - "+graph_date);
                        if(graph_date.equals(vent_line[0])){
                            float vent_par = Float.parseFloat(vent_line[1]);
                            al_ven.add(new Entry(vent_par, cnt_file_lines));
                        }
                    }
                }
                if(!elements[1].equals("null"))
                    al_temp.add(new Entry(Float.parseFloat(elements[1]), cnt_file_lines));
                if(ChartActivity.isPaintCumuTemp && !elements[2].equals("null")) {
                    al_cum.add(new Entry(Float.parseFloat(elements[2]) / 10, cnt_file_lines));
                    Log.d(TAG,"CumuTemp "+cnt_file_lines+" "+elements[2]);
                }if(ChartActivity.isPaintRadiation && !elements[3].equals("null")) {
                    al_rad.add(new Entry(Float.parseFloat(elements[3]) /20, cnt_file_lines));
                    Log.d(TAG,"Radiation "+cnt_file_lines+" "+elements[3]);
                }
                cnt_file_lines++;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }catch (ParseException e){
            e.printStackTrace();
        }

        ArrayList<Integer> colors = new ArrayList<>();
        LineData data             = new LineData(al_labels);

        if(ChartActivity.isPaintRadiation) {
            LineDataSet radi_set = new LineDataSet(al_rad, "日射量");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setColors(colors);
            radi_set.setCircleRadius(0);
            radi_set.setLineWidth(1.8f);
            radi_set.setCircleColor(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setFillColor(ColorTemplate.COLORFUL_COLORS[2]);
            radi_set.setDrawValues(false);
            radi_set.setDrawFilled(false);
            radi_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(radi_set);
        }

        if(ChartActivity.isPaintCumuTemp) {
            LineDataSet cumu_set = new LineDataSet(al_cum, "積算温度");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.COLORFUL_COLORS[1]);
            cumu_set.setColors(colors);
            cumu_set.setCircleRadius(0);
            cumu_set.setFillColor(ColorTemplate.VORDIPLOM_COLORS[2]);
            cumu_set.setDrawValues(false);
            cumu_set.setDrawFilled(true);
            cumu_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(cumu_set);
        }

        if(ChartActivity.isPaintVentilation) {
            LineDataSet vent_set = new LineDataSet(al_ven, "換気率");
            colors = new ArrayList<>();
            colors.add(ColorTemplate.JOYFUL_COLORS[3]);
            vent_set.setColors(colors);
            vent_set.setDrawStepped(true);
            vent_set.setCircleRadius(0);
            vent_set.setCircleColor(ColorTemplate.JOYFUL_COLORS[3]);
            vent_set.setFillColor(ColorTemplate.JOYFUL_COLORS[3]);
            vent_set.setDrawValues(false);
            vent_set.setDrawFilled(true);
            vent_set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            data.addDataSet(vent_set);
        }

        LineDataSet temp_set = new LineDataSet(al_temp, "気温");
        colors = new ArrayList<>();
        colors.add(Color.rgb(25, 25, 25));
        temp_set.setCircleColor(Color.rgb(25, 25, 25));
        temp_set.setCircleColorHole(Color.rgb(25, 25, 25));
        temp_set.setColors(colors);
        temp_set.setLineWidth(1.5f);
        temp_set.setCircleRadius(0);
        temp_set.setDrawValues(false);
        temp_set.setAxisDependency(YAxis.AxisDependency.LEFT);
        data.addDataSet(temp_set);

        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);

        return data;
    }
}
