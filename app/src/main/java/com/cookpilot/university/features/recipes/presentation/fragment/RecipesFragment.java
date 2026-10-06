package com.cookpilot.university.features.recipes.presentation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.R;
import com.cookpilot.university.databinding.FragmentRecipesBinding;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.cookpilot.university.features.recipes.presentation.adapter.RecipeAdapter;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipesUiState;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipesViewModel;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipesViewModelFactory;

import java.util.Collections;
import java.util.List;

public final class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private RecipesViewModel viewModel;
    private RecipeAdapter adapter;
    private List<Recipe> currentRecipes = Collections.emptyList();
    private RecipesUiState currentState = RecipesUiState.loading();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentRecipesBinding.inflate(
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

        viewModel = new ViewModelProvider(
                this,
                new RecipesViewModelFactory(
                        application.getRecipeRepository()
                )
        ).get(RecipesViewModel.class);

        adapter = new RecipeAdapter();
        binding.recipeGrid.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );
        binding.recipeGrid.setAdapter(adapter);
        binding.retryButton.setOnClickListener(
                ignored -> viewModel.refresh()
        );

        viewModel.getRecipes().observe(
                getViewLifecycleOwner(),
                recipes -> {
                    currentRecipes = recipes == null
                            ? Collections.emptyList()
                            : recipes;
                    adapter.submitRecipes(currentRecipes);
                    render();
                }
        );

        viewModel.getUiState().observe(
                getViewLifecycleOwner(),
                state -> {
                    currentState = state == null
                            ? RecipesUiState.loading()
                            : state;
                    render();
                }
        );
    }

    private void render() {
        if (binding == null) {
            return;
        }

        boolean hasRecipes = !currentRecipes.isEmpty();
        boolean loadingWithoutCache =
                currentState.isLoading() && !hasRecipes;
        boolean failedWithoutCache =
                currentState.getErrorMessage() != null && !hasRecipes;
        boolean empty =
                !currentState.isLoading()
                        && currentState.getErrorMessage() == null
                        && !hasRecipes;

        binding.recipeCount.setText(
                getString(
                        R.string.recipes_count,
                        currentRecipes.size()
                )
        );
        binding.recipeGrid.setVisibility(
                hasRecipes ? View.VISIBLE : View.GONE
        );
        binding.loadingIndicator.setVisibility(
                loadingWithoutCache ? View.VISIBLE : View.GONE
        );
        binding.errorState.setVisibility(
                failedWithoutCache ? View.VISIBLE : View.GONE
        );
        binding.emptyState.setVisibility(
                empty ? View.VISIBLE : View.GONE
        );
        binding.cachedNotice.setVisibility(
                currentState.isShowingCachedData() && hasRecipes
                        ? View.VISIBLE
                        : View.GONE
        );

        if (failedWithoutCache) {
            binding.errorMessage.setText(
                    currentState.getErrorMessage()
            );
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
