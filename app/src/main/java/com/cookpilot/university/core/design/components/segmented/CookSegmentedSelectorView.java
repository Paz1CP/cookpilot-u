package com.cookpilot.university.core.design.components.segmented;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cookpilot.university.R;

public final class CookSegmentedSelectorView extends LinearLayout {

    public interface OnSelectedListener {
        void onSelected(int index);
    }

    private final TextView firstSegment;
    private final TextView secondSegment;
    private int selectedIndex;
    @Nullable
    private OnSelectedListener listener;

    public CookSegmentedSelectorView(Context context) {
        this(context, null);
    }

    public CookSegmentedSelectorView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        this(context, attrs, 0);
    }

    public CookSegmentedSelectorView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);

        LayoutInflater.from(context).inflate(
                R.layout.view_cook_segmented_selector,
                this,
                true
        );

        firstSegment = findViewById(R.id.firstSegment);
        secondSegment = findViewById(R.id.secondSegment);

        firstSegment.setOnClickListener(view -> select(0, true));
        secondSegment.setOnClickListener(view -> select(1, true));

        select(0, false);
    }

    public void configure(
            @NonNull String firstLabel,
            @NonNull String secondLabel,
            @Nullable OnSelectedListener listener
    ) {
        firstSegment.setText(firstLabel);
        secondSegment.setText(secondLabel);
        this.listener = listener;
        select(selectedIndex, false);
    }

    public void setSelectedIndex(int index) {
        select(index, false);
    }

    private void select(int index, boolean notify) {
        int next = index == 1 ? 1 : 0;
        if (selectedIndex == next && notify) {
            return;
        }

        selectedIndex = next;
        applyState(firstSegment, selectedIndex == 0);
        applyState(secondSegment, selectedIndex == 1);

        if (notify && listener != null) {
            listener.onSelected(selectedIndex);
        }
    }

    private void applyState(
            @NonNull TextView segment,
            boolean selected
    ) {
        segment.setBackgroundResource(
                selected
                        ? R.drawable.bg_cook_segment_selected
                        : android.R.color.transparent
        );
        segment.setTextColor(
                ContextCompat.getColor(
                        getContext(),
                        selected
                                ? R.color.cook_dark
                                : R.color.cook_text_primary
                )
        );
    }
}
