package com.example.kohki.tocostickapp;

import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;

import java.util.Date;

/**
 * Created by Kohki on 2017/01/17.
 */

public class GraphMaker {
    private static final String TAG = "GraphMakerClass";
    public static Date mLatestMonth;
    public GraphData mGData;
    private LineChart mLineChart;
    private TextView mChartTitle;
    private LineData mDataSet;

    GraphMaker(LineChart line_chart, TextView tv){
        mLineChart = line_chart;
        mChartTitle = tv;
    }

    void makeLineChart(String data_file,boolean isDayScale) {
        mGData   = new GraphData(ChartActivity.getInstance(), mLineChart);
        mDataSet = mGData.getLineData(data_file,isDayScale);
        mLineChart.setData(mDataSet);
        setChart();
    }

    void setChart(){
        mLineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int i, Highlight h) {
                Log.i("Entry selected", e.toString());
                Log.i("MIN MAX", "xmin: " + mLineChart.getXChartMin() + ", xmax: " + mLineChart.getXChartMax() +
                        ", ymin: " + mLineChart.getYChartMin() + ", ymax: " + mLineChart.getYChartMax());
                Log.d("->",mLineChart.getLineData().getDataSetByIndex(i).toString());
            }

            @Override
            public void onNothingSelected() {
                Log.i("Nothing selected", "Nothing selected.");
            }
        });
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
        //  ラインの凡例の設定
        Legend l = mLineChart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextColor(Color.BLACK);
        l.setTextSize(8);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float v, YAxis yAxis) {
                    return v +"℃";
                }
            });
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mLineChart.getAxisRight();
        //    rightAxis.setAxisMaxValue(0);
        //    rightAxis.setAxisMinValue(100f);
        rightAxis.setStartAtZero(true);
        rightAxis.setEnabled(true);
        rightAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, YAxis yAxis) {
                return v +"%";
            }
        });
        rightAxis.setDrawGridLines(true);


        XAxis xl = mLineChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        //    xl.setLabelsToSkip(9);

        mLineChart.setDescription("");
        mLineChart.animateX(500, Easing.EasingOption.EaseInQuad);
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }
}
