package com.example.aerosentra.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.aerosentra.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ProfileMenuBottomSheet extends BottomSheetDialogFragment {

    public interface OnActionListener {
        void onLogout();
        void onManageAccount();
        void onLoginRequest();
    }
    private OnActionListener listener;
    public void setListener(OnActionListener listener) { this.listener = listener; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.userprofile_actions_bottomsheet_holder, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String photoUrl = prefs.getString("photo_url", "");
        String username = prefs.getString("username", "");
        String email = prefs.getString("email", "");

        if (email.isEmpty()) {
            view.findViewById(R.id.unauthenticated_redirector_wrapper).setVisibility(View.VISIBLE);
            view.findViewById(R.id.authenticated_redirector_wrapper).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.unauthenticated_redirector_wrapper).setVisibility(View.GONE);
            view.findViewById(R.id.authenticated_redirector_wrapper).setVisibility(View.VISIBLE);
        }

        ImageView profileImagePreview = view.findViewById(R.id.profileImagePreview);
        LinearLayout logoutLink = view.findViewById(R.id.logoutLink);
        LinearLayout profilePageLink = view.findViewById(R.id.profilePageLink);
        TextView tvUsername = view.findViewById(R.id.username);
        TextView tvEmail = view.findViewById(R.id.emailId);
        Button gotoLoginBtn = view.findViewById(R.id.goto_login_btn);

        tvUsername.setText(username.isEmpty() ? "N.A." : username);
        tvEmail.setText(email.isEmpty() ? "N.A." : email);
        if (photoUrl.isEmpty())
            profileImagePreview.setImageResource(R.drawable.user_dummy);
        else
            Glide.with(this)
                    .load(photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(350))
                    .placeholder(R.drawable.user_dummy)
                    .error(R.drawable.user_dummy)
                    .into(profileImagePreview);

        gotoLoginBtn.setOnClickListener(v -> {
            if (listener != null) listener.onLoginRequest();
            dismiss();
        });
        logoutLink.setOnClickListener(v -> {
            if (listener != null) listener.onLogout();
            dismiss();
        });
        profilePageLink.setOnClickListener(v -> {
            if (listener != null) listener.onManageAccount();
            dismiss();
        });


        return view;
    }
}
