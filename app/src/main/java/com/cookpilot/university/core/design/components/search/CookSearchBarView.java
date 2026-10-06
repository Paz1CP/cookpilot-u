package com.cookpilot.university.core.design.components.search;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;

public final class CookSearchBarView extends LinearLayout {

    public interface OnQueryChangedListener {
        void onQueryChanged(@NonNull String query);
    }

    private final EditText queryInput;
    private final ImageView clearButton;
    private final ImageView filterButton;

    @Nullable
    private OnQueryChangedListener queryChangedListener;

    @Nullable
    private OnClickListener filterClickListener;

    public CookSearchBarView(Context context) {
        this(context, null);
    }

    public CookSearchBarView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        this(context, attrs, 0);
    }

    public CookSearchBarView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);

        LayoutInflater.from(context).inflate(
                R.layout.view_cook_search_bar,
                this,
                true
        );

        ImageView searchIcon = findViewById(R.id.searchIcon);
        queryInput = findViewById(R.id.searchInput);
        clearButton = findViewById(R.id.clearSearchButton);
        filterButton = findViewById(R.id.filterSearchButton);

        searchIcon.setImageResource(CookIcons.search());
        clearButton.setImageResource(CookIcons.close());
        clearButton.setRotation(45f);
        filterButton.setImageResource(CookIcons.filter());

        clearButton.setOnClickListener(view -> clearQuery());
        filterButton.setOnClickListener(view -> {
            if (filterClickListener != null) {
                filterClickListener.onClick(view);
            }
        });

        queryInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
                clearButton.setVisibility(
                        text.length() == 0 ? GONE : VISIBLE
                );

                if (queryChangedListener != null) {
                    queryChangedListener.onQueryChanged(
                            text.toString()
                    );
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void setHint(@NonNull String hint) {
        queryInput.setHint(hint);
    }

    public void setOnQueryChangedListener(
            @Nullable OnQueryChangedListener listener
    ) {
        queryChangedListener = listener;
    }

    public void setOnFilterClickListener(
            @Nullable OnClickListener listener
    ) {
        filterClickListener = listener;
    }

    public void setFilterActive(boolean active) {
        filterButton.setImageResource(
                active
                        ? CookIcons.filterFilled()
                        : CookIcons.filter()
        );
        filterButton.setColorFilter(
                ContextCompat.getColor(
                        getContext(),
                        active
                                ? R.color.cook_primary
                                : R.color.cook_text_tertiary
                )
        );
    }

    private void clearQuery() {
        queryInput.setText("");
        queryInput.clearFocus();

        InputMethodManager inputMethodManager =
                (InputMethodManager) getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        inputMethodManager.hideSoftInputFromWindow(
                queryInput.getWindowToken(),
                0
        );
    }
}
