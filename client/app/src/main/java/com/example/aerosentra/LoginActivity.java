package com.example.aerosentra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.UserAPIService;
import com.example.aerosentra.models.response.BasicResponse;
import com.example.aerosentra.ui.AsteriskPasswordTransformationMethod;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.Toaster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn;
    ImageButton togglePassVisibilityBtn;
    TextView loginToSignupRedirectLink, redirectToForgotPasswordLink;

    boolean isToggled = false;
    SharedPreferences prefs;
    PopupUtils loader;
    UserAPIService api;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        loader = new PopupUtils();
        api = APIClient.getServerClient().create(UserAPIService.class);

        emailInput = findViewById(R.id.user_email_input);
        passwordInput = findViewById(R.id.user_password_input);
        loginBtn = findViewById(R.id.signIn_btn);

        passwordInput.setTransformationMethod(new AsteriskPasswordTransformationMethod());

        togglePassVisibilityBtn = findViewById(R.id.toggle_pass_visibility_btn);
        togglePassVisibilityBtn.setOnClickListener(v -> {
            isToggled = !isToggled;
            int cursorPosition = passwordInput.getText().length(); // save cursor
            if (isToggled) {
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePassVisibilityBtn.setImageResource(R.drawable.ic_eye_off);
            } else {
                passwordInput.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                togglePassVisibilityBtn.setImageResource(R.drawable.ic_eye);
            }
            passwordInput.setSelection(cursorPosition); // restore cursor
        });


        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) emailInput.setActivated(true);
                if (password.isEmpty()) passwordInput.setActivated(true);
                Toaster.warning(this, "Fields are required!");
            } else if (!email.contains("@")) {
                emailInput.setActivated(true);
                passwordInput.setActivated(false);
                Toaster.warning(this, "Invalid email!");
            } else {
                email = email.trim();
                password = password.trim();
                emailInput.setActivated(false);
                passwordInput.setActivated(false);
                emailInput.clearFocus();
                passwordInput.clearFocus();
                sendForAuthentication(email, password);
            }
        });


        redirectToForgotPasswordLink = findViewById(R.id.forgot_password_link);
        redirectToForgotPasswordLink.setOnClickListener(v -> {
            Intent i = new Intent(this, RequestPasswordChangeActivity.class);
            i.putExtra("source", "login");
            startActivity(i);
            finish();
        });

        loginToSignupRedirectLink = findViewById(R.id.to_signup_redirect_link);
        loginToSignupRedirectLink.setOnClickListener(v -> {
           startActivity(new Intent(this, SignupActivity.class));
           overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
           finish();
        });

    }

    private void sendForAuthentication(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    String uid = user.getUid();
                                    String name = user.getDisplayName() != null ? user.getDisplayName() : "";
                                    String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
                                    String u_email = user.getEmail() != null ? user.getEmail() : "";

                                    FirebaseMessaging.getInstance().getToken()
                                                    .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                String token = task1.getResult();
                                                                prefs.edit()
                                                                     .putString("user_id", uid)
                                                                     .putString("username", name)
                                                                     .putString("email", u_email)
                                                                     .putString("photo_url", photoUrl)
                                                                     .putString("fcm_token", token)
                                                                     .apply();
                                                                sendToDatabase(token);
                                                            }
                                                    });
                                }
                            } else {
                                Exception e = task.getException();
                                if (e instanceof FirebaseAuthInvalidCredentialsException)
                                    Toaster.error(this, "Invalid email or password");
                                else
                                    Toaster.error(this, "Something's fishy..." + (e != null ? e.getMessage() : ""));
                            }
                        });
    }
    private void sendToDatabase(String fcmToken) {
        loader.showLoader(this, "Logging in...");
        String deviceId = prefs.getString("device_id", "");

        api.loginUser(deviceId, fcmToken).enqueue(new Callback<>() {
           @Override
           public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
               loader.dismiss();
               if (response.isSuccessful() && response.body() != null) {
                   BasicResponse res = response.body();
                   if (res.isSuccess()) {
                       Toaster.success(LoginActivity.this, res.getMessage());
                       startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                       finish();
                   }
               } else {
                   try {
                       String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                       Log.e("ERROR", error);
                   } catch (Exception e) {
                       String errMsg = e.getMessage();
                       Log.e("ERROR_FETCHING_DATA", errMsg != null ? errMsg : "Exception with no message");
                   }
               }
           }
           @Override
           public void onFailure(Call<BasicResponse> call, Throwable t) {
               loader.dismiss();
               String msg = t.getMessage();
               Log.e("Network Error", msg != null ? msg : "Unknown failure");
           }
        });
    }
}