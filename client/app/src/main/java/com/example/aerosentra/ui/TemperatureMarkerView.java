package com.example.aerosentra.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.example.aerosentra.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

@SuppressLint("ViewConstructor")
public class TemperatureMarkerView extends MarkerView {

    TextView tvContent;

    public TemperatureMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.markerText);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e == null) return;
        String temperature = Math.round(e.getY()) + "°C";
        tvContent.setText(temperature);
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-((float) getWidth() / 2), -getHeight());
    }
}
