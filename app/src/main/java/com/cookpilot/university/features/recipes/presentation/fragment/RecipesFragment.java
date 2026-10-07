package com.cookpilot.university.features.recipes.presentation.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.R;
import com.cookpilot.university.databinding.FragmentRecipesBinding;
import com.cookpilot.university.features.recipes.data.repository.SavedRecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.cookpilot.university.features.recipes.presentation.activity.RecipeDetailActivity;
import com.cookpilot.university.features.recipes.presentation.adapter.RecipeAdapter;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipesUiState;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipesViewModel;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipesViewModelFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private RecipesViewModel viewModel;
    private SavedRecipeRepository savedRecipeRepository;
    private RecipeAdapter adapter;

    private List<Recipe> currentRecipes = Collections.emptyList();
    private Set<String> savedRecipeIds = Collections.emptySet();
    private RecipesUiState currentState = RecipesUiState.loading();
    private boolean showingSaved;

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

        savedRecipeRepository =
                application.getSavedRecipeRepository();

        viewModel = new ViewModelProvider(
                this,
                new RecipesViewModelFactory(
                        application.getRecipeRepository()
                )
        ).get(RecipesViewModel.class);

        configureSearch();
        configureGallerySelector();
        configureFilters();
        configureGrid();
        observeState();

        binding.retryButton.setOnClickListener(
                ignored -> viewModel.refresh()
        );
    }

    private void configureSearch() {
        binding.searchBar.setHint(
                getString(R.string.recipes_search_hint)
        );
        binding.searchBar.setOnQueryChangedListener(
                viewModel::setQuery
        );
        binding.searchBar.setOnFilterClickListener(
                view -> {
                    boolean showing =
                            binding.filterPanel.getVisibility()
                                    == View.VISIBLE;
                    binding.filterPanel.setVisibility(
                            showing ? View.GONE : View.VISIBLE
                    );
                }
        );
    }

    private void configureGallerySelector() {
        binding.gallerySelector.configure(
                getString(R.string.recipes_segment_gallery),
                getString(R.string.recipes_segment_saved),
                index -> {
                    showingSaved = index == 1;
                    render();
                }
        );
    }

    private void configureFilters() {
        binding.difficultyAll.setChecked(true);
        binding.timeAny.setChecked(true);

        binding.difficultyAll.setOnClickListener(
                view -> setDifficulty(
                        RecipesViewModel.DIFFICULTY_ALL
                )
        );
        binding.difficultyEasy.setOnClickListener(
                view -> setDifficulty(
                        RecipesViewModel.DIFFICULTY_EASY
                )
        );
        binding.difficultyMedium.setOnClickListener(
                view -> setDifficulty(
                        RecipesViewModel.DIFFICULTY_MEDIUM
                )
        );
        binding.difficultyHard.setOnClickListener(
                view -> setDifficulty(
                        RecipesViewModel.DIFFICULTY_HARD
                )
        );

        binding.timeAny.setOnClickListener(
                view -> setMaxMinutes(0)
        );
        binding.timeThirty.setOnClickListener(
                view -> setMaxMinutes(30)
        );
        binding.timeSixty.setOnClickListener(
                view -> setMaxMinutes(60)
        );

        updateFilterIndicator();
    }

    private void setDifficulty(@NonNull String difficulty) {
        viewModel.setDifficulty(difficulty);
        updateFilterIndicator();
    }

    private void setMaxMinutes(int minutes) {
        viewModel.setMaxMinutes(minutes);
        updateFilterIndicator();
    }

    private void updateFilterIndicator() {
        binding.searchBar.setFilterActive(
                viewModel.hasActiveFilters()
        );
    }

    private void configureGrid() {
        adapter = new RecipeAdapter(
                recipe -> startActivity(
                        RecipeDetailActivity.createIntent(
                                requireContext(),
                                recipe.getId(),
                                recipe.getBaseServings()
                        )
                ),
                recipe -> savedRecipeRepository.toggle(
                        recipe.getId()
                )
        );

        binding.recipeGrid.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );
        binding.recipeGrid.addItemDecoration(
                new RecipeGridSpacingDecoration(
                        getResources().getDimensionPixelSize(
                                R.dimen.cook_spacing_20
                        ),
                        getResources().getDimensionPixelSize(
                                R.dimen.cook_spacing_24
                        )
                )
        );
        binding.recipeGrid.setAdapter(adapter);
    }

    private void observeState() {
        viewModel.getRecipes().observe(
                getViewLifecycleOwner(),
                recipes -> {
                    currentRecipes = recipes == null
                            ? Collections.emptyList()
                            : recipes;
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

        savedRecipeRepository.observeSavedRecipeIds().observe(
                getViewLifecycleOwner(),
                ids -> {
                    savedRecipeIds = ids == null
                            ? Collections.emptySet()
                            : Collections.unmodifiableSet(
                                    new HashSet<>(ids)
                            );
                    render();
                }
        );
    }

    private void render() {
        if (binding == null || adapter == null) {
            return;
        }

        List<Recipe> visibleRecipes = visibleRecipes();
        boolean hasRecipes = !visibleRecipes.isEmpty();
        boolean loadingWithoutCache =
                currentState.isLoading()
                        && currentRecipes.isEmpty();
        boolean failedWithoutCache =
                currentState.getErrorMessage() != null
                        && currentRecipes.isEmpty();
        boolean empty =
                !currentState.isLoading()
                        && currentState.getErrorMessage() == null
                        && !hasRecipes;

        adapter.submitSavedRecipeIds(savedRecipeIds);
        adapter.submitRecipes(visibleRecipes);

        binding.recipeCount.setText(
                getString(
                        showingSaved
                                ? R.string.recipes_saved_count
                                : R.string.recipes_count,
                        visibleRecipes.size()
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
        binding.emptyState.setText(
                showingSaved
                        ? R.string.recipes_saved_empty
                        : R.string.recipes_empty_search
        );
        binding.cachedNotice.setVisibility(
                currentState.isShowingCachedData()
                        && !currentRecipes.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        if (failedWithoutCache) {
            binding.errorMessage.setText(
                    currentState.getErrorMessage()
            );
        }
    }

    @NonNull
    private List<Recipe> visibleRecipes() {
        if (!showingSaved) {
            return currentRecipes;
        }

        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : currentRecipes) {
            if (savedRecipeIds.contains(recipe.getId())) {
                result.add(recipe);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private static final class RecipeGridSpacingDecoration
            extends RecyclerView.ItemDecoration {

        private final int horizontalSpacing;
        private final int verticalSpacing;

        RecipeGridSpacingDecoration(
                int horizontalSpacing,
                int verticalSpacing
        ) {
            this.horizontalSpacing = horizontalSpacing;
            this.verticalSpacing = verticalSpacing;
        }

        @Override
        public void getItemOffsets(
                @NonNull Rect outRect,
                @NonNull View view,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state
        ) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % 2;

            outRect.left = column == 0
                    ? 0
                    : horizontalSpacing / 2;
            outRect.right = column == 0
                    ? horizontalSpacing / 2
                    : 0;
            outRect.bottom = verticalSpacing;
        }
    }
}
