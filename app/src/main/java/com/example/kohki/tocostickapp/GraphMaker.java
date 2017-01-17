package com.example.kohki.tocostickapp;

import android.graphics.Color;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

/**
 * Created by Kohki on 2017/01/17.
 */

public class GraphMaker {
    private static final String TAG = "GraphMakerClass";

    private GraphData mGData;
    private LineChart mLineChart;
    public static String mNowYearMonth;

    GraphMaker(LineChart line_chart){
        mLineChart = line_chart;
        mGData = new GraphData(ChartActivity.getInstance(), mLineChart);

    }
    void makeLineChart(int graph_scalse){
        // tv_year_month.setText(mGData.mNowYearMonth);

        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);
        // set an alternative background color
        mLineChart.setBackgroundColor(Color.WHITE);
        mLineChart.setData(mGData.getLineData());
        //  ラインの凡例の設定
        Legend l = mLineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = mLineChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        //    xl.setLabelsToSkip(9);

        mLineChart.setDescription("");
        mLineChart.animateX(500, Easing.EasingOption.EaseInQuad);
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();


        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        if(ChartActivity.isPaintTemperature) {
            leftAxis.setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float v, YAxis yAxis) {
                    return v +"℃";
                }
            });
        }else if(ChartActivity.isPaintHumidity) {
            leftAxis.setValueFormatter(new PercentFormatter());
        }
        leftAxis.setValueFormatter(new PercentFormatter());
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mLineChart.getAxisRight();
        //    rightAxis.setAxisMaxValue(0);
        //    rightAxis.setAxisMinValue(100f);
        //    rightAxis.setStartAtZero(true);
        //    rightAxis.setEnabled(true);
        rightAxis.setDrawGridLines(true);
    }
}
