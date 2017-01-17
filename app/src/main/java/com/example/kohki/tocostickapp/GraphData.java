package com.example.kohki.tocostickapp;

import android.content.Context;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kohki on 2017/01/16.
 */

public class GraphData {
    private ArrayList al_elements_data;

    GraphData(Context context, LineChart lineChart){
        al_elements_data = FileHelper.readCsvFile(context,"BTVILOG1.CSV");
        if(al_elements_data != null)
            GraphMaker.mNowYearMonth = ((String[]) al_elements_data.get(al_elements_data.size()-1))[0];
    }
    public LineData getLineData(){
        ArrayList<String> labels     = new ArrayList<String>();
        ArrayList<Entry>  date       = new ArrayList<>();
        ArrayList<Entry>  temp       = new ArrayList<>();
        ArrayList<Entry>  humi       = new ArrayList<>();
        ArrayList<Entry>  radi       = new ArrayList<>();
        ArrayList<Entry>  mois       = new ArrayList<>();
        ArrayList<Entry>  cumu_temp  = new ArrayList<>();

        try {
            for (int i = 0; i < al_elements_data.size(); i++) {
                String[] elements = (String[]) al_elements_data.get(i);
                labels.add(elements[0]);
                //    date.add(new Entry(Float.parseFloat(elements[0]),i));
                temp.add(     new Entry(Float.parseFloat(elements[1]),i));
                humi.add(     new Entry(Float.parseFloat(elements[2]),i));
                radi.add(     new Entry(Float.parseFloat(elements[3]),i));
                mois.add(     new Entry(Float.parseFloat(elements[4]),i));
                cumu_temp.add(new Entry(Float.parseFloat(elements[5]),i));
            }
        }catch (Exception e){
            Toast.makeText(ChartActivity.getInstance(),""+e,Toast.LENGTH_SHORT).show();
        }

        ArrayList<Integer> colors = new ArrayList<>();
        LineData data             = new LineData(labels);

        if(ChartActivity.isPaintTemperature){
            LineDataSet data_set = new LineDataSet(temp, "気温");// <- 環境データセット
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
        if(ChartActivity.isPaintRadiation){
            LineDataSet data_set = new LineDataSet(radi, "日射量");
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
            LineDataSet data_set = new LineDataSet(mois, "土壌湿度");
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
            LineDataSet data_set = new LineDataSet(cumu_temp, "積算温度");
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
}
