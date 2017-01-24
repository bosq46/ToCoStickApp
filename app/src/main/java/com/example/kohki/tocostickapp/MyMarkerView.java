package com.example.kohki.tocostickapp;

/**
 * Created by Kohki on 2017/01/22.
 */
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.Utils;

import java.lang.ref.WeakReference;

public class MyMarkerView extends MarkerView {
    private TextView tvContent;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tvContent);
    }
    @Override
    public void refreshContent(Entry var1, Highlight var2){

    }

    public int getXOffset(float var1){
        return 1;
    }

    public int getYOffset(float var1) {

        return 1;
    }
}