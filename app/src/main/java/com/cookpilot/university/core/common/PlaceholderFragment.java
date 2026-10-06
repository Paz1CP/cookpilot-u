package com.cookpilot.university.core.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cookpilot.university.databinding.FragmentPlaceholderBinding;

public final class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "title";

    public static PlaceholderFragment newInstance(String title) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        FragmentPlaceholderBinding binding =
                FragmentPlaceholderBinding.inflate(
                        inflater,
                        container,
                        false
                );
        binding.title.setText(
                requireArguments().getString(ARG_TITLE, "")
        );
        return binding.getRoot();
    }
}
