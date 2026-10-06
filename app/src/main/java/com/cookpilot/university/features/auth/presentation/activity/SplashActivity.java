package com.cookpilot.university.features.auth.presentation.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.MainActivity;
import com.cookpilot.university.core.design.animation.CookAnimationTokens;
import com.cookpilot.university.databinding.ActivitySplashBinding;
import com.cookpilot.university.features.auth.data.repository.AuthRepository;

public final class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private boolean leavingSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.brandReveal.setBrandTitle();
        binding.brandReveal.setRevealDurationMs(
                CookAnimationTokens.SPLASH_REVEAL_MS
        );
        binding.brandReveal.setShimmerDurationMs(
                CookAnimationTokens.SPLASH_SHIMMER_MS
        );
        binding.brandReveal.setOnRevealCompleted(this::leaveSplash);
        binding.brandReveal.startReveal();
    }

    private void leaveSplash() {
        if (leavingSplash || isFinishing()) {
            return;
        }
        leavingSplash = true;

        CookPilotApplication application =
                (CookPilotApplication) getApplication();
        AuthRepository authRepository = application.getAuthRepository();

        Class<?> destination = authRepository
                .getAuthState()
                .isAuthenticated()
                ? MainActivity.class
                : LoginActivity.class;

        binding.getRoot()
                .animate()
                .alpha(0f)
                .setDuration(CookAnimationTokens.SPLASH_EXIT_MS)
                .withEndAction(() -> {
                    startActivity(new Intent(this, destination));
                    overridePendingTransition(0, 0);
                    finish();
                })
                .start();
    }
}
