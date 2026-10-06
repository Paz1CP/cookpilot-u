package com.cookpilot.university.features.auth.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.cookpilot.university.MainActivity;
import com.cookpilot.university.R;
import com.cookpilot.university.databinding.ActivityLoginBinding;
import com.cookpilot.university.features.auth.presentation.viewmodel.LoginUiState;
import com.cookpilot.university.features.auth.presentation.viewmodel.LoginViewModel;

public final class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private ExoPlayer videoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        applySystemBarInsets();
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
                            horizontalPadding,
                            bars.top + verticalPadding,
                            horizontalPadding,
                            bars.bottom + verticalPadding
                    );
                    return windowInsets;
                }
        );
    }

    private void bindActions() {
        binding.continueButton.setOnClickListener(view -> submit());

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
        viewModel.submit(
                textOf(binding.emailEditText),
                textOf(binding.passwordEditText)
        );
    }

    private void renderState(LoginUiState state) {
        if (!state.isSubmitted()) {
            return;
        }

        binding.emailInputLayout.setError(
                resolveError(state.getEmailErrorResId())
        );
        binding.passwordInputLayout.setError(
                resolveError(state.getPasswordErrorResId())
        );

        if (!state.isValid()) {
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private CharSequence resolveError(@StringRes int errorResId) {
        return errorResId == 0 ? null : getString(errorResId);
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

    private String textOf(android.widget.EditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString();
    }
}
