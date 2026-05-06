package com.example.aerosentra.utils;

import java.util.List;

public class PlanModel {
    String planType;
    int monthlyPrice;
    int yearlyPrice;
    int badge;
    List<FeatureModel> features;

    public PlanModel(String planType, int monthlyPrice, int yearlyPrice, int badge, List<FeatureModel> features) {
        this.planType = planType;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.badge = badge;
        this.features = features;
    }

    public String getPlanType() { return planType; }
    public int getMonthlyPrice() { return monthlyPrice; }
    public int getYearlyPrice() { return yearlyPrice; }
    public int getBadge() { return badge; }
    public List<FeatureModel> getFeatures() { return features; }

    public void setPlanType(String planType) { this.planType = planType; }
    public void setMonthlyPrice(int monthlyPrice) { this.monthlyPrice = monthlyPrice; }
    public void setYearlyPrice(int yearlyPrice) { this.yearlyPrice = yearlyPrice; }
    public void setBadge(int badge) { this.badge = badge; }
    public void setFeatures(List<FeatureModel> features) { this.features = features; }

    public static class FeatureModel {
        String name;
        boolean available;

        public FeatureModel(String name, boolean available) {
            this.name = name;
            this.available = available;
        }

        public String getName() { return name; }
        public boolean isAvailable() { return available; }

        public void setName(String name) { this.name = name; }
        public void setAvailable(boolean available) { this.available = available; }

    }

}
