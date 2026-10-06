package com.cookpilot.university.core.design.components.navigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.cookpilot.university.R;
import com.google.android.material.card.MaterialCardView;

public final class CookAppBarView extends FrameLayout {

    private final MaterialCardView avatarCard;

    public CookAppBarView(Context context) {
        this(context, null);
    }

    public CookAppBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CookAppBarView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_cook_app_bar, this, true);
        avatarCard = findViewById(R.id.avatarCard);
    }

    public void setAvatarOnClickListener(@Nullable OnClickListener listener) {
        avatarCard.setOnClickListener(listener);
    }

    public void setAvatarEnabled(boolean enabled) {
        avatarCard.setEnabled(enabled);
        avatarCard.setAlpha(enabled ? 1f : 0.5f);
    }
}
