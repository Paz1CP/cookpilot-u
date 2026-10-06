package com.cookpilot.university.features.home.presentation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.R;
import com.cookpilot.university.databinding.FragmentHomeBinding;
import com.cookpilot.university.features.cookplan.domain.model.MealMoment;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.cookplan.presentation.fragment.RecipePickerBottomSheet;
import com.cookpilot.university.features.cookplan.presentation.view.CookPlanMealMomentView;
import com.cookpilot.university.features.cookplan.presentation.viewmodel.CookPlanViewModel;
import com.cookpilot.university.features.cookplan.presentation.viewmodel.CookPlanViewModelFactory;
import com.cookpilot.university.features.home.presentation.adapter.WeekDayAdapter;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HomeFragment extends Fragment
        implements CookPlanMealMomentView.Listener {

    private FragmentHomeBinding binding;
    private CookPlanViewModel viewModel;
    private CookPilotApplication application;
    private WeekDayAdapter weekDayAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(
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

        application = (CookPilotApplication) requireActivity()
                .getApplication();

        viewModel = new ViewModelProvider(
                this,
                new CookPlanViewModelFactory(
                        application.getCookPlanRepository()
                )
        ).get(CookPlanViewModel.class);

        configureWeek();
        configureMealCards();
        configurePickerResult();
        observeState();
    }

    private void configureWeek() {
        weekDayAdapter = new WeekDayAdapter(viewModel::selectDate);
        binding.weekDays.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );
        binding.weekDays.setAdapter(weekDayAdapter);
    }

    private void configureMealCards() {
        binding.breakfastCard.configure(MealMoment.BREAKFAST, this);
        binding.lunchCard.configure(MealMoment.LUNCH, this);
        binding.dinnerCard.configure(MealMoment.DINNER, this);
    }

    private void configurePickerResult() {
        getParentFragmentManager().setFragmentResultListener(
                RecipePickerBottomSheet.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    MealMoment mealMoment = MealMoment.fromStorageKey(
                            result.getString(
                                    RecipePickerBottomSheet.RESULT_MEAL_MOMENT
                            )
                    );
                    ArrayList<String> recipeIds = result.getStringArrayList(
                            RecipePickerBottomSheet.RESULT_RECIPE_IDS
                    );

                    if (mealMoment == null || recipeIds == null) {
                        return;
                    }

                    List<Recipe> recipes = new ArrayList<>();
                    for (String recipeId : recipeIds) {
                        application.getRecipeRepository()
                                .findById(recipeId)
                                .ifPresent(recipes::add);
                    }

                    viewModel.addRecipes(mealMoment, recipes);
                }
        );
    }

    private void observeState() {
        viewModel.getSelectedDate().observe(
                getViewLifecycleOwner(),
                weekDayAdapter::setSelectedDate
        );

        viewModel.getPlannedRecipes().observe(
                getViewLifecycleOwner(),
                this::renderPlan
        );
    }

    private void renderPlan(List<PlannedRecipe> plan) {
        binding.breakfastCard.render(
                byMoment(plan, MealMoment.BREAKFAST)
        );
        binding.lunchCard.render(
                byMoment(plan, MealMoment.LUNCH)
        );
        binding.dinnerCard.render(
                byMoment(plan, MealMoment.DINNER)
        );
    }

    private List<PlannedRecipe> byMoment(
            List<PlannedRecipe> plan,
            MealMoment mealMoment
    ) {
        if (plan == null || plan.isEmpty()) {
            return Collections.emptyList();
        }

        List<PlannedRecipe> result = new ArrayList<>();
        for (PlannedRecipe item : plan) {
            if (item.getMealMoment() == mealMoment) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public void onAddRecipe(@NonNull MealMoment mealMoment) {
        RecipePickerBottomSheet
                .newInstance(mealMoment)
                .show(
                        getParentFragmentManager(),
                        "recipe_picker"
                );
    }

    @Override
    public void onRemoveRecipe(@NonNull PlannedRecipe plannedRecipe) {
        viewModel.removeRecipe(plannedRecipe);
    }

    @Override
    public void onCook(@NonNull MealMoment mealMoment) {
        Toast.makeText(
                requireContext(),
                R.string.cooking_pending,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
