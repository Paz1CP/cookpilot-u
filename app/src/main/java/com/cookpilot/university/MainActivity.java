package com.cookpilot.university;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.cookpilot.university.core.common.PlaceholderFragment;
import com.cookpilot.university.core.design.components.navigation.CookBottomNavigationView;
import com.cookpilot.university.databinding.ActivityMainBinding;
import com.cookpilot.university.features.home.presentation.fragment.HomeFragment;

public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applySystemBarInsets();
        binding.bottomNavigation.setOnItemSelectedListener(
                this::renderDestination
        );

        if (savedInstanceState == null) {
            renderDestination(CookBottomNavigationView.Item.HOME);
        }
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
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.nav_recipes)
                );
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
}
