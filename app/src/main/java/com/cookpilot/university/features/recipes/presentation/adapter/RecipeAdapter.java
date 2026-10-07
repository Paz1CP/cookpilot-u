package com.cookpilot.university.features.recipes.presentation.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.cookpilot.university.databinding.ItemRecipeCardBinding;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RecipeAdapter
        extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(@NonNull Recipe recipe);
    }

    public interface OnSavedClickListener {
        void onSavedClick(@NonNull Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final Set<String> savedRecipeIds = new HashSet<>();
    private final OnRecipeClickListener clickListener;
    private final OnSavedClickListener savedClickListener;

    public RecipeAdapter(
            @NonNull OnRecipeClickListener clickListener,
            @NonNull OnSavedClickListener savedClickListener
    ) {
        this.clickListener = clickListener;
        this.savedClickListener = savedClickListener;
    }

    public void submitRecipes(@NonNull List<Recipe> values) {
        recipes.clear();
        recipes.addAll(values);
        notifyDataSetChanged();
    }

    public void submitSavedRecipeIds(@NonNull Set<String> values) {
        savedRecipeIds.clear();
        savedRecipeIds.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemRecipeCardBinding binding = ItemRecipeCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RecipeViewHolder(
                binding,
                clickListener,
                savedClickListener
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecipeViewHolder holder,
            int position
    ) {
        Recipe recipe = recipes.get(position);
        holder.bind(
                recipe,
                savedRecipeIds.contains(recipe.getId())
        );
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static final class RecipeViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemRecipeCardBinding binding;
        private final OnRecipeClickListener clickListener;
        private final OnSavedClickListener savedClickListener;

        RecipeViewHolder(
                @NonNull ItemRecipeCardBinding binding,
                @NonNull OnRecipeClickListener clickListener,
                @NonNull OnSavedClickListener savedClickListener
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
            this.savedClickListener = savedClickListener;
        }

        void bind(@NonNull Recipe recipe, boolean saved) {
            binding.recipeTitle.setText(recipe.getTitle());
            binding.recipeMeta.setText(
                    itemView.getContext().getString(
                            R.string.recipes_card_meta,
                            recipe.getTotalMinutes(),
                            difficultyLabel(recipe.getDifficulty())
                    )
            );

            Glide.with(binding.recipeImage)
                    .load(recipe.getImageUrl())
                    .centerCrop()
                    .into(binding.recipeImage);

            binding.saveRecipeButton.setImageResource(
                    saved
                            ? CookIcons.heartFilled()
                            : CookIcons.heart()
            );
            binding.saveRecipeButton.setImageTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    itemView.getContext(),
                                    R.color.cook_accent
                            )
                    )
            );

            binding.getRoot().setContentDescription(
                    recipe.getTitle()
            );
            binding.getRoot().setOnClickListener(
                    view -> clickListener.onRecipeClick(recipe)
            );
            binding.saveRecipeButton.setOnClickListener(
                    view -> savedClickListener.onSavedClick(recipe)
            );
        }

        private String difficultyLabel(String difficulty) {
            switch (difficulty) {
                case "easy":
                    return itemView.getContext().getString(
                            R.string.recipe_difficulty_easy
                    );
                case "hard":
                    return itemView.getContext().getString(
                            R.string.recipe_difficulty_hard
                    );
                case "medium":
                default:
                    return itemView.getContext().getString(
                            R.string.recipe_difficulty_medium
                    );
            }
        }
    }
}
