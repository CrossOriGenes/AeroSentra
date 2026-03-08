package com.example.aerosentra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.aerosentra.utils.IntroAdapter;
import com.example.aerosentra.utils.IntroModel;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    Button btnNext;
    TextView btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPager = findViewById(R.id.introViewPager);
        btnNext = findViewById(R.id.nextIntroSlideBtn);
        btnSkip = findViewById(R.id.skipBtn);

        String[] titles = getResources().getStringArray(R.array.intro_titles);
        String[] descriptions = getResources().getStringArray(R.array.intro_descriptions);
        int[] imageIcons = {
             R.drawable.intro_img_1,
             R.drawable.intro_img_2,
             R.drawable.intro_img_3
        };

        List<IntroModel> list = new ArrayList<>();
        for (int i = 0; i < titles.length; i++)
            list.add(new IntroModel(imageIcons[i], titles[i], descriptions[i]));
        IntroAdapter adapter = new IntroAdapter(list);
        viewPager.setAdapter(adapter);

        // shift to next slide on 'Next >' button click
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < list.size() - 1)
                viewPager.setCurrentItem(current + 1, true);
            else
                goToAuth();
        });
        // go to auth page on 'Skip' button click
        btnSkip.setOnClickListener(v -> goToAuth());

        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        ViewGroup.LayoutParams params = btnNext.getLayoutParams();
                        if (position == list.size() - 1) {
                            btnNext.setText(R.string.start_text);
                            btnNext.setTextSize(18);
                            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            btnNext.setLayoutParams(params);
                            btnSkip.setVisibility(View.GONE);
                        } else {
                            btnNext.setText(R.string.btn_next);
                            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            btnNext.setLayoutParams(params);
                            btnSkip.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    private void goToAuth() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFirstLaunch", false);
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}