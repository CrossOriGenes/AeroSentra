package com.example.aerosentra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.ui.Toaster;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

public class RequestPasswordChangeActivity extends AppCompatActivity {

    TextView tvInputErrMsg;
    EditText emailForResetLink;
    FrameLayout backBtn;
    Button verifyAndSendResetLinkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_password_change);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String source = getIntent().getStringExtra("source");

        emailForResetLink = findViewById(R.id.email_for_reset_link);
        backBtn = findViewById(R.id.backBtn);
        verifyAndSendResetLinkBtn = findViewById(R.id.verifyAndSendResetLinkBtn);
        tvInputErrMsg = findViewById(R.id.errMsg);

        verifyAndSendResetLinkBtn.setOnClickListener(v -> {
            String email = emailForResetLink.getText().toString();
            String errMsg = "";
            if (email.isEmpty()) {
                tvInputErrMsg.setVisibility(View.VISIBLE);
                errMsg = "This field is required";
                tvInputErrMsg.setText(errMsg);
                emailForResetLink.setActivated(true);
            } else if (!email.contains("@")) {
                tvInputErrMsg.setVisibility(View.VISIBLE);
                errMsg = "Invalid email";
                tvInputErrMsg.setText(errMsg);
                emailForResetLink.setActivated(true);
            } else {
                errMsg = "";
                tvInputErrMsg.setText(errMsg);
                tvInputErrMsg.setVisibility(View.GONE);
                emailForResetLink.setActivated(false);
                emailForResetLink.clearFocus();

                sendResetLink(email);
            }
        });


        backBtn.setOnClickListener(v -> {
            if (source != null) {
                if (source.equalsIgnoreCase("login"))
                    startActivity(new Intent(this, LoginActivity.class));
                else startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            } else finish();
        });

    }

    private void sendResetLink(String email) {
        ActionCodeSettings acs = ActionCodeSettings.newBuilder()
                        .setUrl("https://aerosentra.page.link/reset_password")
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(getPackageName(), true, null)
                        .build();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email, acs)
                .addOnSuccessListener(aVoid -> Toaster.success(this, "Reset link sent to " + email))
                .addOnFailureListener(e -> Toaster.error(this, "Error: " + e.getMessage()));
    }
}