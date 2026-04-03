package com.example.aerosentra.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.aerosentra.R;
import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.WeatherAPIService;
import com.example.aerosentra.models.ImageAdapter;
import com.example.aerosentra.models.PlaceAdapter;
import com.example.aerosentra.models.PlaceAdapterModel;
import com.example.aerosentra.models.response.NearbyPlacesResponse;
import com.example.aerosentra.models.response.TriggerResponse;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.Toaster;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap map;
    RecyclerView recyclerView, placeImagesView;
    LinearLayout placeDetailsContainer, bottomSheet;
    TextView tvPlaceCity, tvPlaceRegion, tvPlaceTemp, tvPlaceHumidity, tvPlaceUV, tvPlacePressure, tvPlaceLatLng, tvPlaceWeatherType;
    ImageView ivPlaceWeatherIcon, singleImageView;
    LinearLayout goToPlaceDetailsPageBtn;

    SharedPreferences prefs;
    double userLat, userLon;
    TriggerResponse.Data data;
    WeatherAPIService api;
    PopupUtils loader;
    ArrayList<PlaceAdapterModel> placesList;
    PlaceAdapter adapter;
    Marker selectedMarker = null;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        prefs = getActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userLat = Double.parseDouble(prefs.getString("lat", "0"));
        userLon = Double.parseDouble(prefs.getString("lon", "0"));
        String json = prefs.getString("weather_data", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            data = gson.fromJson(json, TriggerResponse.Data.class);
        }

        api = APIClient.getServerClient().create(WeatherAPIService.class);
        loader = new PopupUtils();

        placesList = new ArrayList<>();
        adapter = new PlaceAdapter(getContext(), placesList);
        recyclerView = view.findViewById(R.id.placeListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);


        bottomSheet = view.findViewById(R.id.bottomSheet);
        placeDetailsContainer = view.findViewById(R.id.placeDetailsContainer);
        placeImagesView = view.findViewById(R.id.placeImagesRecycler);
        singleImageView = view.findViewById(R.id.placeSingleImageView);
        tvPlaceCity = view.findViewById(R.id.placeCity);
        tvPlaceRegion = view.findViewById(R.id.placeRegion);
        tvPlaceTemp = view.findViewById(R.id.placeTemp);
        tvPlaceHumidity = view.findViewById(R.id.placeHumidity);
        tvPlacePressure = view.findViewById(R.id.placePressure);
        tvPlaceUV = view.findViewById(R.id.placeUV);
        tvPlaceWeatherType = view.findViewById(R.id.placeWeaType);
        tvPlaceLatLng = view.findViewById(R.id.placeLatLng);
        ivPlaceWeatherIcon = view.findViewById(R.id.placeWeatherIcon);
        goToPlaceDetailsPageBtn = view.findViewById(R.id.openDetailsBtn);

        placeDetailsContainer.setVisibility(View.GONE);
        placeImagesView.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        goToPlaceDetailsPageBtn.setOnClickListener(v -> {
            if (selectedMarker != null) {
                PlaceAdapterModel place = (PlaceAdapterModel) selectedMarker.getTag();
                if (place != null) Toaster.info(getContext(), "Place: "+place.getCity());
            }
        });


        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        LatLng current = new LatLng(userLat, userLon);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

        fetchNearbyPlaces();

        map.setOnMarkerClickListener(marker -> {
           float density = getResources().getDisplayMetrics().density;
           int heightInPx = (int) (610 * density);
           ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
           params.height = heightInPx;
           bottomSheet.setLayoutParams(params);
           goToPlaceDetailsPageBtn.setVisibility(View.VISIBLE);

           PlaceAdapterModel place = (PlaceAdapterModel) marker.getTag();

           if (place == null) return true;
           if (selectedMarker != null && selectedMarker != marker) {
               PlaceAdapterModel oldPlace = (PlaceAdapterModel) selectedMarker.getTag();
               LatLng position = selectedMarker.getPosition();
               selectedMarker.remove();
               createMarker(
                       getContext(),
                       position,
                       "https:"+oldPlace.getIcon(),
                       new MarkerOptions().anchor(0.5f, 0.5f),
                       oldPlace
               );
           }
           LatLng position = marker.getPosition();
           marker.remove();
           createInfoWindow(
                 getContext(),
                 position,
                 "https:"+place.getIcon(),
                 new MarkerOptions().position(position).anchor(0.5f, 0.5f),
                 place
           );
           fetchPlaceDetails(place);
           map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

           return true;
        });
    }

    private void createMarker(Context ctx, LatLng position, String iconUrl, MarkerOptions markerOptions, PlaceAdapterModel place) {
        View markerView = LayoutInflater.from(ctx).inflate(R.layout.marker_weather, null);
        ImageView weatherIcon = markerView.findViewById(R.id.weatherIcon);
        Glide.with(ctx)
             .asBitmap()
             .load(iconUrl)
             .into(new CustomTarget<Bitmap>() {
                       @Override
                       public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                           weatherIcon.setImageBitmap(resource);
                           markerView.measure(
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                           );
                           markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
                           Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                           Canvas canvas = new Canvas(bitmap);
                           markerView.draw(canvas);
                           bitmap = Bitmap.createScaledBitmap(bitmap, 121, 128, false);
                           markerOptions
                                   .position(position)
                                   .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                           Marker marker = map.addMarker(markerOptions);
                           if (marker != null) marker.setTag(place);
                       }

                       @Override
                       public void onLoadCleared(@Nullable Drawable placeholder) {}
             });
    }
    private void createInfoWindow(Context ctx, LatLng position, String iconUrl, MarkerOptions markerOptions, PlaceAdapterModel place) {
        View markerView = LayoutInflater.from(ctx).inflate(R.layout.map_info_window, null);
        TextView cityName = markerView.findViewById(R.id.cityNameInfoWindow);
        TextView temperature = markerView.findViewById(R.id.temperatureInfoWindow);
        cityName.setText(place.getCity());
        String temp = Math.round(place.getTemperature()) + "°";
        temperature.setText(temp);

        ImageView weatherIcon = markerView.findViewById(R.id.weatherIconInfoWindow);
        Glide.with(ctx)
                .asBitmap()
                .load(iconUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        weatherIcon.setImageBitmap(resource);
                        markerView.measure(
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        );
                        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
                        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        markerView.draw(canvas);
                        markerOptions
                                .position(position)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        Marker marker = map.addMarker(markerOptions);
                        if (marker != null) {
                            marker.setTag(place);
                            selectedMarker = marker;
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void fetchNearbyPlaces() {
        loader.showLoader(getContext(), "Loading nearby cities...");
        api.getNearbyPlaces(userLat, userLon).enqueue(new Callback<NearbyPlacesResponse>() {
            @Override
            public void onResponse(Call<NearbyPlacesResponse> call, Response<NearbyPlacesResponse> response) {
                loader.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    NearbyPlacesResponse res = response.body();
                    if (res.isSuccess()) {
                        Log.d("SUCCESS_MESSAGE", res.getMsg());
                        List<PlaceAdapterModel> list = res.getPlaces();
                        if (list != null && !list.isEmpty()) {
                            placesList.clear();
                            placesList.addAll(list);
                            adapter.notifyDataSetChanged();
                            addMarkersToMap(placesList);
                        } else {
                            addFallbackMarker();
                            Toaster.warning(getContext(), "No nearby places found!");
                        }
                    }
                } else {
                    try {
                        String error = response.errorBody().toString();
                        JSONObject obj = new JSONObject(error);
                        String message = obj.getString("message");
                        Toaster.error(getContext(), message);
                        Log.e("ERROR_MESSAGE:", message);
                    } catch (Exception e) {
                        addFallbackMarker();
                        Toaster.error(getContext(), "Unknown Error");
                        Log.e("ERROR_MESSAGE:", e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<NearbyPlacesResponse> call, Throwable t) {
                loader.dismiss();
                addFallbackMarker();
                Toaster.error(getContext(), "Network Error: "+t.getMessage());
            }
        });
    }
    private void fetchPlaceDetails(PlaceAdapterModel place) {
//        loader.showLoader(getContext(), "Getting place details...");
        placeDetailsContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        setPlaceDetails(place);
    }

    private void addMarkersToMap(List<PlaceAdapterModel> list) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (PlaceAdapterModel place : list) {
            LatLng position = new LatLng(place.getLat(), place.getLng());
            builder.include(position);
            createMarker(
                    getContext(),
                    position,
                    "https:"+place.getIcon(),
                    new MarkerOptions().anchor(0.5f, 0.5f),
                    place
            );
        }
        LatLngBounds bounds = builder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }
    private void addFallbackMarker() {
        LatLng current = new LatLng(userLat, userLon);
        MarkerOptions markerOptions = new MarkerOptions().position(current).anchor(0.5f, 0.5f);
        PlaceAdapterModel place = null;
        if (data != null) {
            String iconUrl = data.getApi_data().getCondition().getIcon();
            String userCurrCity = data.getApi_data().getCity_name();
            String region = data.getApi_data().getState() + ", " + data.getApi_data().getCountry();
            String weatherType = data.getApi_data().getCondition().getText();
            double temperature = data.getApi_data().getTemp();
            double uv = data.getApi_data().getUv_index();
            double humidity = data.getApi_data().getHumidity();
            double pressure = data.getApi_data().getPressure();
            place = new PlaceAdapterModel(
                    iconUrl,
                    weatherType,
                    userCurrCity,
                    region,
                    userLat,
                    userLon,
                    temperature,
                    uv,
                    humidity,
                    pressure,
                    null
            );

            if (!iconUrl.isEmpty()) createMarker(getContext(), current, "https:"+iconUrl, markerOptions, place);
            markerOptions.title(userCurrCity != null ? userCurrCity : "Current Location");
        } else {
            Marker marker = map.addMarker(markerOptions.title("Current Location"));
            if (marker != null) marker.setTag(place);
        }

    }
    private void setPlaceDetails(PlaceAdapterModel place) {
        String temperature = Math.round(place.getTemperature()) + "°C";
        String humidity = Math.round(place.getHumidity()) + "%";
        String region = place.getRegion();
        String weatherType = place.getType();
        double uv = Math.round(place.getUv());
        String pressure = Math.round(place.getPressure()) +"mb";
        String iconUrl = "https:" + place.getIcon();
        double lat = place.getLat();
        double lon = place.getLng();
        String latDir = lat > 0 ? "°N" : "°S";
        String lonDir = lon > 0 ? "°E" : "°W";
        String valCoords = String.format("%.4f%s, %.4f%s", lat, latDir, lon, lonDir);
        tvPlaceCity.setText(place.getCity());
        tvPlaceRegion.setText(region);
        tvPlaceWeatherType.setText(weatherType);
        tvPlaceTemp.setText(temperature);
        tvPlaceHumidity.setText(humidity);
        tvPlaceUV.setText(String.valueOf(uv));
        tvPlacePressure.setText(pressure);
        tvPlaceLatLng.setText(valCoords);
        Glide.with(this).load(iconUrl).into(ivPlaceWeatherIcon);
        List<String> images = place.getImages();
        if (images == null || images.isEmpty()) {
            singleImageView.setVisibility(View.GONE);
            placeImagesView.setVisibility(View.GONE);
        } else if (images.size() == 1) {
            placeImagesView.setVisibility(View.GONE);
            singleImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(images.get(0)).into(singleImageView);
        } else {
            placeImagesView.setVisibility(View.VISIBLE);
            singleImageView.setVisibility(View.GONE);
            ImageAdapter adapter = new ImageAdapter(getContext(), images);
            placeImagesView.setAdapter(adapter);
        }
    }
}
