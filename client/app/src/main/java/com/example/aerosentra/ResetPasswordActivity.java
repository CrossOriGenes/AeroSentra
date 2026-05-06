package com.example.aerosentra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.ui.AsteriskPasswordTransformationMethod;
import com.example.aerosentra.ui.Toaster;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    FrameLayout backBtn;
    EditText passwordNew, passwordNewConfirm;
    ImageButton newPasswordToggleBtn, confirmPasswordToggleBtn;
    Button saveNewPasswordBtn;

    SharedPreferences prefs;
    String oobCode;
    boolean isToggled1 = false, isToggled2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        passwordNew = findViewById(R.id.password_new);
        passwordNewConfirm = findViewById(R.id.password_new_confirm);
        newPasswordToggleBtn = findViewById(R.id.toggle_pass_visibility_btn_3);
        confirmPasswordToggleBtn = findViewById(R.id.toggle_pass_visibility_btn_4);

        Uri data = getIntent().getData();
        if (data != null) oobCode = data.getQueryParameter("oobCode");
        if (oobCode == null) {
            Toaster.error(this, "Invalid reset link!");
            finish();
            return;
        }

        passwordNew.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        passwordNewConfirm.setTransformationMethod(new AsteriskPasswordTransformationMethod());

        newPasswordToggleBtn.setOnClickListener(v -> {
            isToggled1 = !isToggled1;
            int cursorPosition = passwordNew.getText().length(); // save cursor
            if (isToggled1) {
                passwordNew.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                newPasswordToggleBtn.setImageResource(R.drawable.ic_eye_off);
            } else {
                passwordNew.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                newPasswordToggleBtn.setImageResource(R.drawable.ic_eye);
            }
            passwordNew.setSelection(cursorPosition); // restore cursor
        });
        confirmPasswordToggleBtn.setOnClickListener(v -> {
            isToggled2 = !isToggled2;
            int cursorPosition = passwordNewConfirm.getText().length(); // save cursor
            if (isToggled2) {
                passwordNewConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPasswordToggleBtn.setImageResource(R.drawable.ic_eye_off);
            } else {
                passwordNewConfirm.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                confirmPasswordToggleBtn.setImageResource(R.drawable.ic_eye);
            }
            passwordNewConfirm.setSelection(cursorPosition); // restore cursor
        });


        saveNewPasswordBtn = findViewById(R.id.save_new_password_btn);
        saveNewPasswordBtn.setOnClickListener(v -> {
           String newPassword = passwordNew.getText().toString();
           String newPasswordConfirm = passwordNewConfirm.getText().toString();
           if (newPassword.isEmpty() || newPasswordConfirm.isEmpty()) {
               if (newPassword.isEmpty()) passwordNew.setActivated(true);
               if (newPasswordConfirm.isEmpty()) passwordNewConfirm.setActivated(true);
               Toaster.warning(this, "Fields are required!");
           } else if (newPassword.length() < 6) {
               passwordNew.setActivated(true);
               passwordNewConfirm.setActivated(false);
               Toaster.warning(this, "Password must be at least 6 characters long!");
           } else if (!newPassword.equals(newPasswordConfirm)) {
               passwordNew.setActivated(false);
               passwordNewConfirm.setActivated(true);
               Toaster.warning(this, "Passwords do not match!");
           } else {
               passwordNew.setActivated(false);
               passwordNewConfirm.setActivated(false);
               passwordNew.clearFocus();
               passwordNewConfirm.clearFocus();
               newPassword = newPassword.trim();
               savePassword(newPassword);
           }
        });


        backBtn = findViewById(R.id.backBtn2);
        backBtn.setOnClickListener(v -> {
            String email = prefs.getString("email", "");
            if (email.isEmpty())
                startActivity(new Intent(this, LoginActivity.class));
            else startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

    }

    private void savePassword(String newPassword) {
        Log.d("NEW_PASSWORD", newPassword);
        FirebaseAuth.getInstance().confirmPasswordReset(oobCode, newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toaster.success(this, "Password changed successfully!");
                    prefs.edit()
                         .remove("email")
                         .apply();
                         startActivity(new Intent(this, LoginActivity.class));
                         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    })
                .addOnFailureListener(e -> Toaster.error(this, "Error: " + e.getMessage()));
    }
}