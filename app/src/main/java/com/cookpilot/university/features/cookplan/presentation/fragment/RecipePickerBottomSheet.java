package com.cookpilot.university.features.cookplan.presentation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.databinding.SheetRecipePickerBinding;
import com.cookpilot.university.features.cookplan.domain.model.MealMoment;
import com.cookpilot.university.features.cookplan.presentation.adapter.RecipePickerAdapter;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public final class RecipePickerBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "recipe_picker_result";
    public static final String RESULT_MEAL_MOMENT = "meal_moment";
    public static final String RESULT_RECIPE_IDS = "recipe_ids";

    private static final String ARG_MEAL_MOMENT = "arg_meal_moment";

    private SheetRecipePickerBinding binding;
    private RecipePickerAdapter adapter;

    public static RecipePickerBottomSheet newInstance(MealMoment mealMoment) {
        RecipePickerBottomSheet sheet = new RecipePickerBottomSheet();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_MEAL_MOMENT, mealMoment.getStorageKey());
        sheet.setArguments(arguments);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = SheetRecipePickerBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        CookPilotApplication application =
                (CookPilotApplication) requireActivity().getApplication();
        List<Recipe> recipes = application
                .getRecipeRepository()
                .getAllRecipes();

        adapter = new RecipePickerAdapter(recipes);
        binding.recipeGrid.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );
        binding.recipeGrid.setAdapter(adapter);
        binding.addRecipesButton.setOnClickListener(v -> submitSelection());
    }

    private void submitSelection() {
        ArrayList<String> selectedIds =
                new ArrayList<>(adapter.getSelectedIds());

        if (selectedIds.isEmpty()) {
            return;
        }

        Bundle result = new Bundle();
        result.putString(
                RESULT_MEAL_MOMENT,
                requireArguments().getString(ARG_MEAL_MOMENT)
        );
        result.putStringArrayList(RESULT_RECIPE_IDS, selectedIds);

        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
