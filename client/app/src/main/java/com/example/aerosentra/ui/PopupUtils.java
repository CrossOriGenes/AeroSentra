package com.example.aerosentra.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0.8f);
        }

        dialog.show();
    }

    // 🔹 OPTIONAL: dynamic message
    public void showLoader(Context context, String message) {

        showLoader(context);

        TextView msg = dialog.findViewById(R.id.loaderText);
        if (msg != null) msg.setText(message);
    }

    // 🔹 CONFIRMATION POPUP (DELETE)
    public void showConfirmationPopup(Context context, String title, String message, Runnable onConfirm) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirmation_dialog);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0.8f);
        }

        TextView tvTitle = dialog.findViewById(R.id.confirmation_header_title);
        TextView tvMsg = dialog.findViewById(R.id.confirmation_message);
        Button cancelBtn = dialog.findViewById(R.id.cancel_btn);
        Button confirmBtn = dialog.findViewById(R.id.confirm_btn);

        if (tvTitle != null && tvMsg != null) {
            tvTitle.setText(title);
            tvMsg.setText(message);
        }

        cancelBtn.setOnClickListener(v -> dismiss());

        confirmBtn.setOnClickListener(v -> {
            dismiss();
            if (onConfirm != null) onConfirm.run();
        });

        dialog.show();
    }

    // 🔹 CUSTOM FORM POPUP
    public Dialog showFormPopup(Context ctx, int layoutResId) {
        dialog = new Dialog(ctx);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0.8f);
        }

        dialog.show();

        return dialog;
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