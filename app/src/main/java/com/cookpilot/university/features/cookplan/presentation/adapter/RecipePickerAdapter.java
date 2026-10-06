package com.cookpilot.university.features.cookplan.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cookpilot.university.R;
import com.cookpilot.university.databinding.ItemRecipePickerBinding;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RecipePickerAdapter
        extends RecyclerView.Adapter<RecipePickerAdapter.RecipeViewHolder> {

    private final List<Recipe> recipes;
    private final Set<String> selectedIds = new HashSet<>();

    public RecipePickerAdapter(@NonNull List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    public List<String> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemRecipePickerBinding binding = ItemRecipePickerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RecipeViewHolder(binding);
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

    final class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecipePickerBinding binding;

        RecipeViewHolder(ItemRecipePickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Recipe recipe) {
            boolean selected = selectedIds.contains(recipe.getId());

            binding.recipeTitle.setText(recipe.getTitle());
            binding.recipeMeta.setText(
                    itemView.getContext().getString(
                            R.string.recipe_picker_meta,
                            recipe.getTotalMinutes(),
                            recipe.getBaseServings()
                    )
            );
            binding.selectedBadge.setVisibility(
                    selected ? View.VISIBLE : View.GONE
            );

            binding.recipeCard.setStrokeWidth(
                    selected
                            ? itemView.getResources().getDimensionPixelSize(
                            R.dimen.cook_border_2
                    )
                            : 0
            );
            binding.recipeCard.setStrokeColor(
                    ContextCompat.getColor(
                            itemView.getContext(),
                            selected
                                    ? R.color.cook_primary
                                    : android.R.color.transparent
                    )
            );

            Glide.with(binding.recipeImage)
                    .load(recipe.getImageUrl())
                    .centerCrop()
                    .into(binding.recipeImage);

            binding.getRoot().setOnClickListener(view -> {
                if (!selectedIds.add(recipe.getId())) {
                    selectedIds.remove(recipe.getId());
                }
                int adapterPosition = getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(adapterPosition);
                }
            });
        }
    }
}
