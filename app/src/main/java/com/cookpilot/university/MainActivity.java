package com.cookpilot.university;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.cookpilot.university.core.design.components.navigation.CookBottomNavigationView;
import com.cookpilot.university.databinding.ActivityMainBinding;

public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(this::renderDestination);
        binding.appBar.setAvatarOnClickListener(view -> {
            // Perfil se implementará cuando forme parte del alcance académico.
        });

        renderDestination(CookBottomNavigationView.Item.HOME);
    }

    private void renderDestination(CookBottomNavigationView.Item item) {
        int label;

        switch (item) {
            case RECIPES:
                label = R.string.nav_recipes;
                break;
            case SHOPPING:
                label = R.string.nav_shopping;
                break;
            case HOME:
            default:
                label = R.string.nav_home;
                break;
        }

        binding.screenTitle.setText(label);
    }
}
