package com.example.transbord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.transbord.adapters.OnboardingAdapter;
import com.example.transbord.models.OnboardingItem;
import com.example.transbord.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicators;
    private MaterialButton buttonNext;
    private TextView textSkip;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize preferences manager
        preferencesManager = new PreferencesManager(this);

        // Initialize views
        layoutOnboardingIndicators = findViewById(R.id.indicators_container);
        buttonNext = findViewById(R.id.btn_next);
        textSkip = findViewById(R.id.tv_skip);

        setupOnboardingItems();

        // Set up ViewPager
        ViewPager2 onboardingViewPager = findViewById(R.id.onboarding_view_pager);
        onboardingViewPager.setAdapter(onboardingAdapter);

        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
            }
        });

        // Set up click listeners
        buttonNext.setOnClickListener(v -> {
            int currentPosition = onboardingViewPager.getCurrentItem();
            if (currentPosition < onboardingAdapter.getItemCount() - 1) {
                // Go to next page
                onboardingViewPager.setCurrentItem(currentPosition + 1);
            } else {
                // Last page, finish onboarding
                finishOnboarding();
            }
        });

        textSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        // Page 1: Welcome
        OnboardingItem welcomeItem = new OnboardingItem(
                R.drawable.ic_onboarding_welcome,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
        );

        // Page 2: Record and Transcribe
        OnboardingItem recordItem = new OnboardingItem(
                R.drawable.ic_onboarding_record,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
        );

        // Page 3: Floating Button
        OnboardingItem floatingItem = new OnboardingItem(
                R.drawable.ic_onboarding_floating,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
        );

        // Page 4: AI Enhancement
        OnboardingItem aiItem = new OnboardingItem(
                R.drawable.ic_onboarding_ai,
                getString(R.string.onboarding_title_4),
                getString(R.string.onboarding_desc_4)
        );

        onboardingItems.add(welcomeItem);
        onboardingItems.add(recordItem);
        onboardingItems.add(floatingItem);
        onboardingItems.add(aiItem);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupOnboardingIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.onboarding_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int position) {
        int childCount = layoutOnboardingIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutOnboardingIndicators.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.onboarding_indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.onboarding_indicator_inactive
                ));
            }
        }

        // Change button text on last page
        if (position == onboardingAdapter.getItemCount() - 1) {
            buttonNext.setText(R.string.get_started_now);
        } else {
            buttonNext.setText(R.string.next);
        }
    }

    private void finishOnboarding() {
        // Mark onboarding as completed
        preferencesManager.setFirstTimeLaunch(false);

        // Navigate to MainActivity
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
