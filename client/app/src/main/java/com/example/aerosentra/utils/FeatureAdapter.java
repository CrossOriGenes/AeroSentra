package com.example.aerosentra.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.aerosentra.R;

import java.util.List;

public class FeatureAdapter extends BaseAdapter {

    private final Context context;
    private final List<PlanModel.FeatureModel> list;

    public FeatureAdapter(Context context, List<PlanModel.FeatureModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int i) { return list.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.pricing_feature_list_item, parent, false);
        }

        TextView icon = view.findViewById(R.id.icon);
        TextView text = view.findViewById(R.id.featureText);

        PlanModel.FeatureModel feature = list.get(i);

        if (feature.isAvailable()) {
            icon.setText("✔");
            icon.setTextColor(Color.parseColor("#4CAF50"));

            text.setPaintFlags(text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            text.setAlpha(1f);
        } else {
            icon.setText("✖");
            icon.setTextColor(Color.RED);

            text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            text.setAlpha(0.5f);
        }

        text.setText(feature.getName());

        return view;
    }
}