package com.example.aerosentra.ui;

import android.content.Context;
import android.widget.TextView;

import com.example.aerosentra.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class RainfallMarkerView extends MarkerView {

    TextView tvContent;

    public RainfallMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.markerText);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e == null) return;
        String rainfall = Math.round(e.getY()) + "%";
        tvContent.setText(rainfall);
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() { return new MPPointF(-((float) getWidth() / 2), -getHeight()); }

}
