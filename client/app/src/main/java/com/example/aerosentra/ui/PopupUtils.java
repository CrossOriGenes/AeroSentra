package com.example.aerosentra.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import com.example.aerosentra.R;

public class PopupUtils {

    private Dialog dialog;

    // 🔹 SHOW LOADER
    public void showLoader(Context context) {

        if (dialog != null && dialog.isShowing()) return;

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loader_dialog);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        dialog.show();
    }

    // 🔹 OPTIONAL: dynamic message
    public void showLoader(Context context, String message) {

        showLoader(context);

        TextView msg = dialog.findViewById(R.id.loaderText);
        if (msg != null) msg.setText(message);
    }

    // 🔹 DISMISS
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    // 🔹 SAFETY (optional use in lifecycle)
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}