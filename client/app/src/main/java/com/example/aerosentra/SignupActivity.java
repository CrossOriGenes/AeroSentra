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
import com.example.aerosentra.models.requests.SignupRequest;
import com.example.aerosentra.models.response.BasicResponse;
import com.example.aerosentra.ui.AsteriskPasswordTransformationMethod;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.Toaster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    EditText nameInput, emailInput, passwordInput;
    Button signupBtn;
    ImageButton togglePassVisibilityBtn;
    TextView signupToLoginRedirectLink;

    String fcmToken;
    boolean isToggled = false;
    SharedPreferences prefs;
    PopupUtils loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        loader = new PopupUtils();


        nameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        signupBtn = findViewById(R.id.signup_btn);

        passwordInput.setTransformationMethod(new AsteriskPasswordTransformationMethod());

        togglePassVisibilityBtn = findViewById(R.id.toggle_pass_visibility_btn_2);
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


        signupBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                if (name.isEmpty()) nameInput.setActivated(true);
                if (email.isEmpty()) emailInput.setActivated(true);
                if (password.isEmpty()) passwordInput.setActivated(true);
                Toaster.warning(this, "Fields are required!");
            } else if (name.length() < 3) {
                nameInput.setActivated(true);
                emailInput.setActivated(false);
                passwordInput.setActivated(false);
                Toaster.warning(this, "Username too short!");
            } else if (!email.contains("@")) {
                emailInput.setActivated(true);
                nameInput.setActivated(false);
                passwordInput.setActivated(false);
                Toaster.warning(this, "Invalid email!");
            } else if (password.length() < 6) {
                passwordInput.setActivated(true);
                emailInput.setActivated(false);
                nameInput.setActivated(false);
                Toaster.warning(this, "Password must be at least 6 characters long!");
            } else {
                name = name.trim();
                email = email.trim();
                password = password.trim();
                nameInput.setActivated(false);
                emailInput.setActivated(false);
                passwordInput.setActivated(false);
                nameInput.clearFocus();
                emailInput.clearFocus();
                passwordInput.clearFocus();
                createNewAccount(name, email, password);
            }
        });

        signupToLoginRedirectLink = findViewById(R.id.to_login_redirect_link);
        signupToLoginRedirectLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    private void createNewAccount(String name, String email, String password) {
        String deviceId = prefs.getString("device_id", "");

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fcmToken = task.getResult();
                Log.d("FCM_TOKEN", fcmToken != null ? fcmToken : "null");
                Log.d("DEVICE_ID", deviceId);
            }

            if (deviceId.isEmpty() || fcmToken == null || fcmToken.isEmpty()) {
                Toaster.error(this, "Unable to find device details! Please try later.");
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, authTask -> {
                        if (authTask.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                Log.d("USER_ID", uid);
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                Log.d("PROFILE", "User profile created");
                                                prefs.edit()
                                                        .putString("user_id", uid)
                                                        .putString("username", name)
                                                        .putString("email", email)
                                                        .apply();
                                                sendToDatabase(uid, name, email, deviceId);
                                            }
                                        });
                            }
                        } else {
                            Exception e = authTask.getException();
                            if (e instanceof FirebaseAuthUserCollisionException)
                                Toaster.error(this, "User already exists!");
                            else
                                Toaster.error(this, "Something's fishy..." + (e != null ? e.getMessage() : ""));
                        }
                    });
        });
    }

    private void sendToDatabase(String uid, String name, String email, String deviceId) {
        loader.showLoader(this, "Creating account...");
        UserAPIService api = APIClient.getServerClient().create(UserAPIService.class);
        SignupRequest req = new SignupRequest(name, email, deviceId, uid, fcmToken, "");
        api.registerUser(req).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                loader.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse res = response.body();
                    if (res.isSuccess()) {
                        Toaster.success(SignupActivity.this, res.getMessage());
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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