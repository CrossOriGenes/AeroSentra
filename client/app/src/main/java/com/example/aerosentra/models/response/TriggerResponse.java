package com.example.aerosentra.models.response;

import java.util.List;

public class TriggerResponse {

    private boolean success;
    private String msg;
    private Data data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    // ---------------- DATA ----------------

    public static class Data {

        private MLData ml_data;
        private APIData api_data;

        public MLData getMl_data() { return ml_data; }
        public void setMl_data(MLData ml_data) { this.ml_data = ml_data; }

        public APIData getApi_data() { return api_data; }
        public void setApi_data(APIData api_data) { this.api_data = api_data; }
    }

    // ---------------- ML DATA ----------------

    public static class MLData {

        private int class_value;
        private String status;

        public int getClassValue() { return class_value; }
        public void setClassValue(int class_value) { this.class_value = class_value; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // ---------------- API DATA ----------------

    public static class APIData {

        private Coords coords;
        private String city_name;
        private String state;
        private String country;
        private double temp;
        private double max_temp;
        private double min_temp;
        private double feels_like;
        private double humidity;
        private double uv_index;
        private double pressure;
        private double wind;
        private String wind_dir;
        private double precipitation;
        private boolean is_day;
        private Condition condition;
        private AQI aqi;
        private List<HourlyForecast> hourly_forecast;
        private List<DailyForecast> daily_forecast;
        private List<Alert> alerts;

        public Coords getCoords() { return coords; }
        public void setCoords(Coords coords) { this.coords = coords; }

        public String getCity_name() { return city_name; }
        public void setCity_name(String city_name) { this.city_name = city_name; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public double getTemp() { return temp; }
        public void setTemp(double temp) { this.temp = temp; }

        public double getMax_temp() { return max_temp; }
        public void setMax_temp(double max_temp) { this.max_temp = max_temp; }

        public double getMin_temp() { return min_temp; }
        public void setMin_temp(double min_temp) { this.min_temp = min_temp; }

        public double getFeels_like() { return feels_like; }
        public void setFeels_like(double feels_like) { this.feels_like = feels_like; }

        public double getHumidity() { return humidity; }
        public void setHumidity(double humidity) { this.humidity = humidity; }

        public double getUv_index() { return uv_index; }
        public void setUv_index(double uv_index) { this.uv_index = uv_index; }

        public double getPressure() { return pressure; }
        public void setPressure(double pressure) { this.pressure = pressure; }

        public double getWind() { return wind; }
        public void setWind(double wind) { this.wind = wind; }

        public String getWind_dir() { return wind_dir; }
        public void setWind_dir(String wind_dir) { this.wind_dir = wind_dir; }

        public double getPrecipitation() { return precipitation; }
        public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

        public boolean getIs_day() { return is_day; }
        public void setIs_day(boolean is_day) { this.is_day = is_day; }

        public Condition getCondition() { return condition; }
        public void setCondition(Condition condition) { this.condition = condition; }

        public AQI getAqi() { return aqi; }
        public void setAqi(AQI aqi) { this.aqi = aqi; }

        public List<HourlyForecast> getHourly_forecast() { return hourly_forecast; }
        public void setHourly_forecast(List<HourlyForecast> hourly_forecast) { this.hourly_forecast = hourly_forecast; }

        public List<DailyForecast> getDaily_forecast() { return daily_forecast; }
        public void setDaily_forecast(List<DailyForecast> daily_forecast) { this.daily_forecast = daily_forecast; }

        public List<Alert> getAlerts() { return alerts; }
        public void setAlerts(List<Alert> alerts) { this.alerts = alerts; }
    }

    // ---------------- COORDS ----------------
    public static class Coords {

        private double lat;
        private double lng;

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }

        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
    }

    // ---------------- CONDITION ----------------
    public static class Condition {

        private String text;
        private String icon;
        private int code;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
    }

    // ---------------- AQI ----------------
    public static class AQI {

        private double pm2_5;
        private double pm10;
        private double co;
        private double no2;
        private double o3;
        private double so2;
        private int magnitude_of_5;
        private int magnitude_of_10;

        public double getPm2_5() { return pm2_5; }
        public void setPm2_5(double pm2_5) { this.pm2_5 = pm2_5; }

        public double getPm10() { return pm10; }
        public void setPm10(double pm10) { this.pm10 = pm10; }

        public double getCo() { return co; }
        public void setCo(double co) { this.co = co; }

        public double getNo2() { return no2; }
        public void setNo2(double no2) { this.no2 = no2; }

        public double getO3() { return o3; }
        public void setO3(double o3) { this.o3 = o3; }

        public double getSo2() { return so2; }
        public void setSo2(double so2) { this.so2 = so2; }

        public int getMagnitude_of_5() { return magnitude_of_5; }
        public void setMagnitude_of_5(int magnitude_of_5) { this.magnitude_of_5 = magnitude_of_5; }

        public int getMagnitude_of_10() { return magnitude_of_10; }
        public void setMagnitude_of_10(int magnitude_of_10) { this.magnitude_of_10 = magnitude_of_10; }
    }

    // ---------------- HOURLY FORECAST ----------------
    public static class HourlyForecast {

        private String time;
        private double temp;
        private String icon;
        private int chance_of_rain;
        private boolean is_current;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public double getTemp() { return temp; }
        public void setTemp(double temp) { this.temp = temp; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public int getChance_of_rain() { return chance_of_rain; }
        public void setChance_of_rain(int chance_of_rain) { this.chance_of_rain = chance_of_rain; }

        public boolean isIs_current() { return is_current; }
        public void setIs_current(boolean is_current) { this.is_current = is_current; }
    }

    // ---------------- DAILY FORECAST ----------------
    public static class DailyForecast {

        private String date;
        private double max_temp;
        private double min_temp;
        private double avg_temp;
        private String icon;
        private double chance_of_rain;
        private double uv;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public double getMax_temp() { return max_temp; }
        public void setMax_temp(double max_temp) { this.max_temp = max_temp; }

        public double getMin_temp() { return min_temp; }
        public void setMin_temp(double min_temp) { this.min_temp = min_temp; }

        public double getAvg_temp() { return avg_temp; }
        public void setAvg_temp(double avg_temp) { this.avg_temp = avg_temp; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public double getChance_of_rain() { return chance_of_rain; }
        public void setChance_of_rain(double chance_of_rain) { this.chance_of_rain = chance_of_rain; }

        public double getUv() { return uv; }
        public void setUv(double uv) { this.uv = uv; }
    }

    // ---------------- ALERT ----------------
    public static class Alert {
        public String headline;
        public String severity;
        public String areas;
        public String desc;

        public String getHeadline() {
            return headline;
        }

        public void setHeadline(String headline) {
            this.headline = headline;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getAreas() {
            return areas;
        }

        public void setAreas(String areas) {
            this.areas = areas;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}