package com.example.aerosentra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.aerosentra.api.APIClient;
import com.example.aerosentra.api.UserAPIService;
import com.example.aerosentra.models.response.BasicResponse;
import com.example.aerosentra.models.response.UpdateProfileResponse;
import com.example.aerosentra.ui.PopupUtils;
import com.example.aerosentra.ui.Toaster;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    ImageView backToHome;
    ShapeableImageView profileImg;
    TextView pictureChooserOpener, tvSubscriptionType;
    EditText nameEditInput, emailEditInput;
    LinearLayout saveChangesBtn, logoutBtn;
    MaterialButton changePasswordBtn, removeAccountBtn, subscribeBtn;

    SharedPreferences prefs;
    PopupUtils loader, popup;
    UserAPIService api;
    private final String[] PLANS = { "Free", "Basic", "Standard", "Premium" };
    private ActivityResultLauncher<String> galleryLauncher;
    private byte[] compressedImageBytes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        loader = new PopupUtils();
        popup = new PopupUtils();
        api = APIClient.getServerClient().create(UserAPIService.class);


        profileImg = findViewById(R.id.profileImage);
        pictureChooserOpener = findViewById(R.id.changeProfilePictureBtn);
        nameEditInput = findViewById(R.id.username_edit_box);
        emailEditInput = findViewById(R.id.user_email_edit_box);
        saveChangesBtn = findViewById(R.id.btn_saveChanges);
        logoutBtn = findViewById(R.id.btn_logout);

        String photoUrl = prefs.getString("photo_url", "");
        if (photoUrl.isEmpty()) profileImg.setImageResource(R.drawable.user_dummy);
        else Glide
                .with(ProfileActivity.this)
                .load(photoUrl)
                .transition(DrawableTransitionOptions.withCrossFade(350))
                .placeholder(R.drawable.user_dummy)
                .error(R.drawable.user_dummy)
                .into(profileImg);
        String username = prefs.getString("username", "");
        String email = prefs.getString("email", "");
        nameEditInput.setText(username);
        emailEditInput.setText(email);
        saveChangesBtn.setEnabled(false);
        saveChangesBtn.setAlpha(0.4f);

        nameEditInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equalsIgnoreCase(username)) {
                    saveChangesBtn.setEnabled(true);
                    saveChangesBtn.setAlpha(1f);
                } else {
                    saveChangesBtn.setEnabled(false);
                    saveChangesBtn.setAlpha(0.4f);
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        emailEditInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equalsIgnoreCase(email)) {
                    saveChangesBtn.setEnabled(true);
                    saveChangesBtn.setAlpha(1f);
                } else {
                    saveChangesBtn.setEnabled(false);
                    saveChangesBtn.setAlpha(0.4f);
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        pictureChooserOpener = findViewById(R.id.changeProfilePictureBtn);
        pictureChooserOpener.setOnClickListener(v -> showImagePicker());
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImg.setImageURI(uri);
                        compressImage(uri);
                        saveChangesBtn.setEnabled(true);
                        saveChangesBtn.setAlpha(1.0f);
                    }
                }
        );

        saveChangesBtn.setOnClickListener(v -> {
            MultipartBody.Part imagePart = getImagePart();
            String newUsername = nameEditInput.getText().toString();
            String newEmail = emailEditInput.getText().toString();
            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                if (newUsername.isEmpty()) nameEditInput.setActivated(true);
                if (newEmail.isEmpty()) emailEditInput.setActivated(true);
                Toaster.error(this, "Please fill all the fields");
            } else if (newUsername.length() < 3) {
                Toaster.error(this, "Username must be at least 3 characters long");
                nameEditInput.setActivated(true);
            } else if (!newEmail.contains("@")) {
                Toaster.error(this, "Invalid email address");
                emailEditInput.setActivated(true);
            } else {
                nameEditInput.setActivated(false);
                emailEditInput.setActivated(false);
                nameEditInput.clearFocus();
                emailEditInput.clearFocus();
                newUsername = newUsername.trim();
                newEmail = newEmail.trim();

                updateProfile(imagePart, newUsername, newEmail);
            }
        });


        tvSubscriptionType = findViewById(R.id.subscription_type);
        String planType = prefs.getString("plan_type", "");
        initPlanBadge(planType);


        changePasswordBtn = findViewById(R.id.btn_changePassword);
        changePasswordBtn.setOnClickListener(v -> {
           Intent i = new Intent(this, RequestPasswordChangeActivity.class);
           i.putExtra("source", "profile");
           startActivity(i);
           finish();
        });


        removeAccountBtn = findViewById(R.id.btn_deleteAccount);
        removeAccountBtn.setOnClickListener(v -> {
            popup.showConfirmationPopup(
                    ProfileActivity.this,
                    "Wait",
                    "Are you sure you to remove your account? This would remove all your records along with all subscriptions and others & is IRREVERSIBLE",
                    this::reAuthenticateUser
            );
        });


        logoutBtn.setOnClickListener(v -> {
            popup.showConfirmationPopup(
                    ProfileActivity.this,
                    "Confirm",
                    "Are you sure you want to logout from the app?",
                    () -> {
                        FirebaseAuth.getInstance().signOut();
                        prefs.edit()
                                .remove("user_id")
                                .remove("username")
                                .remove("email")
                                .remove("photo_url")
                                .remove("fcm_token")
                                .apply();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                        }, 200);
                    }
            );
        });


        subscribeBtn = findViewById(R.id.btn_goToSubscription);
        subscribeBtn.setOnClickListener(v -> {
           Intent i = new Intent(this, SubscriptionsActivity.class);
           i.putExtra("source", "profile");
           startActivity(i);
           finish();
        });


        backToHome = findViewById(R.id.btn_backToDash);
        backToHome.setOnClickListener(v -> {
           startActivity(new Intent(this, DashboardActivity.class));
           overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
           finish();
        });
    }

    private void initPlanBadge(String planType) {
        switch (planType.toLowerCase()) {
            case "basic":
                tvSubscriptionType.setText(PLANS[1]);
                tvSubscriptionType.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0099CC")));
                break;
            case "standard":
                tvSubscriptionType.setText(PLANS[2]);
                tvSubscriptionType.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0066")));
                break;
            case "premium":
                tvSubscriptionType.setText(PLANS[3]);
                tvSubscriptionType.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#993399")));
                break;
            default:
                tvSubscriptionType.setText(PLANS[0]);
                tvSubscriptionType.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3E764B")));
        }
    }

    private void showImagePicker() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.image_picker_bottomsheet, null);

        LinearLayout optionCamera = view.findViewById(R.id.open_camera);
        LinearLayout optionGallery = view.findViewById(R.id.open_gallery);

        optionCamera.setOnClickListener(v -> {
            sheetDialog.dismiss();
            openCamera();
        });
        optionGallery.setOnClickListener(v -> {
            sheetDialog.dismiss();
            openGallery();
        });

        sheetDialog.setContentView(view);
        sheetDialog.show();
    }
    private void openCamera() {
        Toaster.info(this, "This Feature is coming soon...");
    }
    private void openGallery() {
        galleryLauncher.launch("image/*");
    }
    private void compressImage(Uri uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            compressedImageBytes = stream.toByteArray();
            Log.d("IMAGE_SIZE","Compressed size : "+compressedImageBytes.length);
        }
        catch (Exception e){
            Log.d("IMAGE_SIZE","Error : "+e.getMessage());
        }
    }
    private MultipartBody.Part getImagePart() {
        if (compressedImageBytes == null) {
            return null;
        }
        RequestBody requestFile = RequestBody.create(compressedImageBytes, MediaType.parse("image/jpeg"));
        return MultipartBody.Part.createFormData("profile", "profile.jpg", requestFile);
    }

    private void updateProfile(MultipartBody.Part imagePart, String newUsername, String newEmail) {
        loader.showLoader(this, "Updating profile...");

        String deviceId = prefs.getString("device_id", "");

        RequestBody usernameBody = RequestBody.create(newUsername, MultipartBody.FORM);
        RequestBody emailBody = RequestBody.create(newEmail, MultipartBody.FORM);

        api.updateProfile(deviceId, usernameBody, emailBody, imagePart).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                loader.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    UpdateProfileResponse res = response.body();
                    if (res.isSuccess()) {
                        String uid = res.getUser().getUserId();
                        String username = res.getUser().getUsername();
                        String email = res.getUser().getEmail();
                        String photoUrl = res.getUser().getPhotoUrl();
                        boolean isEmailChanged = !email.equalsIgnoreCase(prefs.getString("email", ""));

                        //  Firebase update
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            if (isEmailChanged)
                                user.verifyBeforeUpdateEmail(email)
                                    .addOnFailureListener(e -> {
                                        Toaster.warning(ProfileActivity.this, "Please login again (email change)");
                                        forceLogout();
                                    });
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .setPhotoUri(photoUrl.isEmpty() ? null : Uri.parse(photoUrl))
                                    .build();
                            user.updateProfile(request);
                        }
                        prefs.edit()
                                .putString("user_id", uid)
                                .putString("username", username)
                                .putString("email", email)
                                .putString("photo_url", photoUrl)
                                .apply();
                        Toaster.success(ProfileActivity.this, res.getMessage());
                        startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        finish();
                    }
                } else {
                    try {
                        String error = response.errorBody().toString();
                        JSONObject obj = new JSONObject(error);
                        String message = obj.getString("message");
                        Toaster.error(ProfileActivity.this, message);
                        Log.e("ERROR", message);
                    } catch (Exception e) {
                        Toaster.error(ProfileActivity.this, "Unknown Error");
                        Log.e("ERROR_FETCHING_DATA", e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                loader.dismiss();
                Toaster.error(ProfileActivity.this, "Network Error: "+t.getMessage());
            }
        });
    }
    private void forceLogout() {
        FirebaseAuth.getInstance().signOut();
        prefs.edit()
                .remove("user_id")
                .remove("username")
                .remove("email")
                .remove("photo_url")
                .apply();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        startActivity(i);
    }

    private void reAuthenticateUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            EditText passwordInput = new EditText(this);
            new AlertDialog.Builder(this)
                .setTitle("Confirm Password")
                .setView(passwordInput)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    if (email != null) {
                        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                        user.reauthenticate(credential)
                                .addOnSuccessListener(unused -> deleteAccount())
                                .addOnFailureListener(e -> Toaster.error(this, "Authentication failed"));
                    } else Toaster.error(this, "Email not found");
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
        } else Toaster.error(this, "User not found!");

    }
    private void deleteAccount() {
        String deviceId = prefs.getString("device_id", "");
        loader.showLoader(this, "Deleting account...");
        api.deleteAccount(deviceId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                loader.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                   BasicResponse res = response.body();
                   if (res.isSuccess()) deleteFirebaseUser();
               } else {
                   try {
                       String error = response.errorBody().toString();
                       JSONObject obj = new JSONObject(error);
                       String message = obj.getString("message");
                       Toaster.error(ProfileActivity.this, message);
                   } catch (Exception e) {
                       Toaster.error(ProfileActivity.this, "Unknown Error");
                   }
               }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                loader.dismiss();
                Toaster.error(ProfileActivity.this, "Network Error: "+t.getMessage());
                Log.e("Network Error", t.getMessage());
            }
        });
    }
    private void deleteFirebaseUser() {
        FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnSuccessListener(unused -> {
                    prefs.edit()
                            .remove("user_id")
                            .remove("username")
                            .remove("email")
                            .remove("photo_url")
                            .apply();
                    Toaster.success(ProfileActivity.this, "Account deleted");
                    startActivity(new Intent(this, DashboardActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                })
                .addOnFailureListener(e -> Toaster.error(ProfileActivity.this, "Deletion failed:"+e.getMessage()));
    }

}