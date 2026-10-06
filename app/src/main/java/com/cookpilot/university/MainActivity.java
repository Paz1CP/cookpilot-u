package com.cookpilot.university;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.cookpilot.university.core.common.PlaceholderFragment;
import com.cookpilot.university.core.design.components.navigation.CookBottomNavigationView;
import com.cookpilot.university.databinding.ActivityMainBinding;
import com.cookpilot.university.features.auth.data.repository.AuthRepository;
import com.cookpilot.university.features.auth.presentation.activity.LoginActivity;
import com.cookpilot.university.features.home.presentation.fragment.HomeFragment;
import com.cookpilot.university.features.recipes.presentation.fragment.RecipesFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableDeviceFullscreen();

        CookPilotApplication application =
                (CookPilotApplication) getApplication();
        authRepository = application.getAuthRepository();

        if (!authRepository.getAuthState().isAuthenticated()) {
            openLogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applySystemBarInsets();
        binding.appBar.setAvatarOnClickListener(
                view -> showSessionDialog()
        );
        binding.bottomNavigation.setOnItemSelectedListener(
                this::renderDestination
        );

        if (savedInstanceState == null) {
            renderDestination(CookBottomNavigationView.Item.HOME);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void enableDeviceFullscreen() {
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        );
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setStatusBarContrastEnforced(false);
        }

        hideSystemBars();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);
    }

    private void applySystemBarInsets() {
        int margin = getResources().getDimensionPixelSize(
                R.dimen.cook_spacing_16
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                binding.rootContainer,
                (view, windowInsets) -> {
                    Insets bars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    setMargins(
                            binding.appBar,
                            bars.left + getResources().getDimensionPixelSize(
                                    R.dimen.cook_padding_screen_x
                            ),
                            bars.top + margin,
                            bars.right + getResources().getDimensionPixelSize(
                                    R.dimen.cook_padding_screen_x
                            ),
                            0
                    );

                    setMargins(
                            binding.bottomNavigation,
                            bars.left + getResources().getDimensionPixelSize(
                                    R.dimen.cook_padding_screen_x
                            ),
                            0,
                            bars.right + getResources().getDimensionPixelSize(
                                    R.dimen.cook_padding_screen_x
                            ),
                            bars.bottom + margin
                    );

                    return windowInsets;
                }
        );
        ViewCompat.requestApplyInsets(binding.rootContainer);
    }

    private void setMargins(
            View view,
            int left,
            int top,
            int right,
            int bottom
    ) {
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.leftMargin = left;
        params.topMargin = top;
        params.rightMargin = right;
        params.bottomMargin = bottom;
        view.setLayoutParams(params);
    }

    private void renderDestination(CookBottomNavigationView.Item item) {
        Fragment fragment;

        switch (item) {
            case RECIPES:
                fragment = new RecipesFragment();
                break;
            case SHOPPING:
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.nav_shopping)
                );
                break;
            case HOME:
            default:
                fragment = new HomeFragment();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .commit();
    }

    private void showSessionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.auth_session_title)
                .setMessage(R.string.auth_logout_confirm)
                .setNegativeButton(R.string.auth_cancel, null)
                .setPositiveButton(
                        R.string.auth_logout,
                        (dialog, which) -> {
                            authRepository.logout();
                            openLogin();
                        }
                )
                .show();
    }

    private void openLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
