package com.cookpilot.university.features.auth.presentation.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.MainActivity;
import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.cookpilot.university.databinding.ActivityLoginBinding;
import com.cookpilot.university.features.auth.presentation.viewmodel.LoginUiState;
import com.cookpilot.university.features.auth.presentation.viewmodel.LoginViewModel;
import com.cookpilot.university.features.auth.presentation.viewmodel.LoginViewModelFactory;
import com.google.android.material.textfield.TextInputLayout;

public final class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private ExoPlayer videoPlayer;
    private boolean passwordVisible;
    private boolean openingHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CookPilotApplication application =
                (CookPilotApplication) getApplication();

        viewModel = new ViewModelProvider(
                this,
                new LoginViewModelFactory(application.getAuthRepository())
        ).get(LoginViewModel.class);

        applySystemBarInsets();
        configurePasswordToggle();
        bindActions();
        observeState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBackgroundVideo();
    }

    @Override
    protected void onStop() {
        releaseBackgroundVideo();
        super.onStop();
    }

    private void applySystemBarInsets() {
        int horizontalPadding = getResources().getDimensionPixelSize(
                R.dimen.cook_padding_screen_x
        );
        int verticalPadding = getResources().getDimensionPixelSize(
                R.dimen.cook_spacing_24
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                binding.loginContent,
                (view, windowInsets) -> {
                    Insets bars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    view.setPadding(
                            bars.left + horizontalPadding,
                            bars.top + verticalPadding,
                            bars.right + horizontalPadding,
                            bars.bottom + verticalPadding
                    );
                    return windowInsets;
                }
        );
        ViewCompat.requestApplyInsets(binding.loginContent);
    }

    private void configurePasswordToggle() {
        binding.passwordInputLayout.setEndIconMode(
                TextInputLayout.END_ICON_CUSTOM
        );
        binding.passwordInputLayout.setEndIconDrawable(CookIcons.eye());
        binding.passwordInputLayout.setEndIconTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(
                                this,
                                R.color.cook_light_strong
                        )
                )
        );
        binding.passwordInputLayout.setEndIconOnClickListener(view -> {
            passwordVisible = !passwordVisible;
            binding.passwordEditText.setTransformationMethod(
                    passwordVisible
                            ? HideReturnsTransformationMethod.getInstance()
                            : PasswordTransformationMethod.getInstance()
            );
            binding.passwordInputLayout.setEndIconDrawable(
                    passwordVisible
                            ? CookIcons.eyeSlash()
                            : CookIcons.eye()
            );

            if (binding.passwordEditText.getText() != null) {
                binding.passwordEditText.setSelection(
                        binding.passwordEditText.getText().length()
                );
            }
        });
    }

    private void bindActions() {
        binding.continueButton.setOnClickListener(view -> submit());
        binding.authModeToggle.setOnClickListener(
                view -> viewModel.toggleMode()
        );

        binding.passwordEditText.setOnEditorActionListener(
                (view, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        submit();
                        return true;
                    }
                    return false;
                }
        );
    }

    private void observeState() {
        viewModel.getUiState().observe(this, this::renderState);
    }

    private void submit() {
        String email = binding.emailEditText.getText() == null
                ? ""
                : binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText() == null
                ? ""
                : binding.passwordEditText.getText().toString();

        viewModel.submit(email, password);
    }

    private void renderState(LoginUiState state) {
        if (state == null) {
            return;
        }

        binding.emailInputLayout.setError(
                state.getEmailErrorResId() == 0
                        ? null
                        : getString(state.getEmailErrorResId())
        );
        binding.passwordInputLayout.setError(
                state.getPasswordErrorResId() == 0
                        ? null
                        : getString(state.getPasswordErrorResId())
        );

        String authError = state.getAuthError();
        binding.authErrorText.setText(authError);
        binding.authErrorText.setVisibility(
                authError == null ? View.GONE : View.VISIBLE
        );

        boolean loading = state.isLoading();
        binding.loginProgress.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
        binding.continueButton.setEnabled(!loading);
        binding.authModeToggle.setEnabled(!loading);
        binding.emailEditText.setEnabled(!loading);
        binding.passwordEditText.setEnabled(!loading);

        binding.continueButton.setText(
                state.isRegisterMode()
                        ? R.string.login_create_account
                        : R.string.login_sign_in
        );
        binding.authModeToggle.setText(
                state.isRegisterMode()
                        ? R.string.login_switch_to_login
                        : R.string.login_switch_to_register
        );

        if (state.isAuthenticated()) {
            openHome();
        }
    }

    private void openHome() {
        if (openingHome) {
            return;
        }
        openingHome = true;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void startBackgroundVideo() {
        if (videoPlayer != null) {
            return;
        }

        videoPlayer = new ExoPlayer.Builder(this).build();
        videoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        videoPlayer.setVolume(0f);
        videoPlayer.setMediaItem(
                MediaItem.fromUri(getString(R.string.login_video_url))
        );

        binding.loginVideo.setUseController(false);
        binding.loginVideo.setResizeMode(
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        );
        binding.loginVideo.setKeepContentOnPlayerReset(true);
        binding.loginVideo.setPlayer(videoPlayer);

        videoPlayer.prepare();
        videoPlayer.play();
    }

    private void releaseBackgroundVideo() {
        if (videoPlayer == null) {
            return;
        }

        binding.loginVideo.setPlayer(null);
        videoPlayer.release();
        videoPlayer = null;
    }
}
