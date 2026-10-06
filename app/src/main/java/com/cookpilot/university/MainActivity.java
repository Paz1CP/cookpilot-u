package com.cookpilot.university;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(
                this::renderDestination
        );
        binding.appBar.setAvatarOnClickListener(view -> {
            // Perfil fuera del alcance actual.
        });

        if (savedInstanceState == null) {
            renderDestination(CookBottomNavigationView.Item.HOME);
        }
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
