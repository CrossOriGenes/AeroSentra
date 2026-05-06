package com.example.aerosentra.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerosentra.R;

import java.util.List;

public class PricingAdapter extends RecyclerView.Adapter<PricingAdapter.ViewHolder> {

    private final Context context;
    private final List<PlanModel> list;
    private boolean isYearly;

    public PricingAdapter(Context context, List<PlanModel> list, boolean isYearly) {
        this.context = context;
        this.list = list;
        this.isYearly = isYearly;
    }

    public void setYearly(boolean yearly) {
        this.isYearly = yearly;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout badge;
        TextView planTitle, price, savings, timing;
        ListView featuresList;
        Button cta;

        public ViewHolder(View itemView) {
            super(itemView);
            planTitle = itemView.findViewById(R.id.tv_plan_type);
            price = itemView.findViewById(R.id.price);
            badge = itemView.findViewById(R.id.badge);
            savings = itemView.findViewById(R.id.savingsValue);
            timing = itemView.findViewById(R.id.timingType);
            featuresList = itemView.findViewById(R.id.featuresList);
            cta = itemView.findViewById(R.id.ctaBtn);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_pricing_card, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlanModel model = list.get(position);
        String priceText, timingText = "/yr";

        // Plan Type
        holder.planTitle.setText(model.getPlanType());

        // Per month/year
        if (isYearly) {
            priceText = "₹"+model.getYearlyPrice();
            holder.price.setText(priceText);
            holder.timing.setText(timingText);
        } else {
            priceText = "₹"+model.getMonthlyPrice();
            holder.price.setText(priceText);
            timingText = "/mo";
            holder.timing.setText(timingText);
        }

        // Savings badge
        if (model.getBadge() != 0) {
            holder.badge.setVisibility(View.VISIBLE);
            String value = model.getBadge() + "%";
            holder.savings.setText(value);
        } else {
            holder.badge.setVisibility(View.GONE);
        }
        // Free plan condition
        if (model.getPlanType().equalsIgnoreCase("Free"))
            holder.cta.setVisibility(View.GONE);
        else holder.cta.setVisibility(View.VISIBLE);


        // Features List
        FeatureAdapter featureAdapter = new FeatureAdapter(context, model.getFeatures());
        holder.featuresList.setAdapter(featureAdapter);
        // 🚫 Disable ListView scrolling
        holder.featuresList.setOnTouchListener((v, event) -> true);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}