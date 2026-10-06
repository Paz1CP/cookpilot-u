package com.cookpilot.university.features.recipes.presentation.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cookpilot.university.R;
import com.cookpilot.university.databinding.ItemRecipeCardBinding;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public final class RecipeAdapter
        extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(@NonNull Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(@NonNull OnRecipeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitRecipes(@NonNull List<Recipe> values) {
        recipes.clear();
        recipes.addAll(values);
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
        return new RecipeViewHolder(binding, clickListener);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecipeViewHolder holder,
            int position
    ) {
        holder.bind(recipes.get(position));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static final class RecipeViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemRecipeCardBinding binding;
        private final OnRecipeClickListener clickListener;

        RecipeViewHolder(
                @NonNull ItemRecipeCardBinding binding,
                @NonNull OnRecipeClickListener clickListener
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
        }

        void bind(@NonNull Recipe recipe) {
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

            binding.getRoot().setContentDescription(
                    recipe.getTitle()
            );
            binding.getRoot().setOnClickListener(
                    view -> clickListener.onRecipeClick(recipe)
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