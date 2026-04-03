package com.example.aerosentra.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aerosentra.R;

import java.util.List;

public class PlaceImagesAdapter extends RecyclerView.Adapter<PlaceImagesAdapter.ImageViewHolder> {
    Context context;
    List<String> imageUrls;

    public PlaceImagesAdapter(Context context, List<String> imageUrls){
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context).load(imageUrl).into(holder.placeImage);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.placeImage);
        }
    }
}
