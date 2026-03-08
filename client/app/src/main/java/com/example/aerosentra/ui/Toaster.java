package com.example.aerosentra.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aerosentra.R;

public class Toaster {

    public static void success(Context context, String message) {
        showToast(context, message, R.drawable.ic_check_circle, "#274521");
    }
    public static void warning(Context context, String message) {
        showToast(context, message, R.drawable.ic_exclamation_circle, "#453A21");
    }
    public static void error(Context context, String message) {
        showToast(context, message, R.drawable.ic_cross_circle, "#45212A");
    }
    public static void info(Context context, String message) {
        showToast(context, message, R.drawable.ic_info_circle, "#20203E");
    }

    private static void showToast(Context context, String message, int icon, String color) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toaster, null);

        ImageView iconView = view.findViewById(R.id.toastIcon);
        TextView iconText = view.findViewById(R.id.toastText);

        iconView.setImageResource(icon);
        iconText.setText(message);

        GradientDrawable bg = (GradientDrawable) view.getBackground();
        bg.setColor(Color.parseColor(color));

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }
}
