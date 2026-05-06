package com.example.aerosentra.ui;

import android.content.Context;
import android.widget.TextView;

import com.example.aerosentra.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class PercentageValueMarkerView extends MarkerView {

    TextView tvContent;

    public PercentageValueMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.markerText);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e == null) return;
        String val = Math.round(e.getY()) + "%";
        tvContent.setText(val);
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() { return new MPPointF(-((float) getWidth() / 2), -getHeight()); }

}
