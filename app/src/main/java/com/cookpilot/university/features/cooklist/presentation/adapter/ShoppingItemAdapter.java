package com.cookpilot.university.features.cooklist.presentation.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.cookpilot.university.databinding.ItemShoppingItemBinding;
import com.cookpilot.university.features.cooklist.domain.model.ShoppingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ShoppingItemAdapter
        extends RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder> {

    public interface Listener {
        void onToggle(@NonNull ShoppingItem item);
        void onEdit(@NonNull ShoppingItem item);
    }

    private final Listener listener;
    private final List<ShoppingItem> items = new ArrayList<>();

    public ShoppingItemAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submitItems(@NonNull List<ShoppingItem> values) {
        items.clear();
        items.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull
    public ShoppingItem getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        return new ViewHolder(
                ItemShoppingItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                ),
                listener
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemShoppingItemBinding binding;
        private final Listener listener;

        ViewHolder(
                @NonNull ItemShoppingItemBinding binding,
                @NonNull Listener listener
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(@NonNull ShoppingItem item) {
            binding.itemName.setText(item.getName());
            binding.itemQuantity.setText(
                    formatQuantity(
                            item.getQuantity(),
                            item.getUnit()
                    )
            );

            int sources = item.getSourceRecipeIds().size();
            if (item.isManual()) {
                binding.itemSource.setText(
                        R.string.cooklist_manual_item
                );
            } else {
                binding.itemSource.setText(
                        itemView.getContext().getResources()
                                .getQuantityString(
                                        R.plurals.cooklist_recipe_sources,
                                        sources,
                                        sources
                                )
                );
            }

            binding.toggleIcon.setImageResource(
                    item.isPurchased()
                            ? CookIcons.selectedFilled()
                            : 0
            );
            binding.toggleIcon.setBackgroundResource(
                    item.isPurchased()
                            ? R.drawable.bg_cooklist_toggle_selected
                            : R.drawable.bg_cooklist_toggle
            );
            binding.toggleIcon.setColorFilter(
                    ContextCompat.getColor(
                            itemView.getContext(),
                            R.color.cook_dark
                    )
            );

            binding.getRoot().setAlpha(
                    item.isPurchased() ? 0.55f : 1f
            );
            binding.itemName.setPaintFlags(
                    item.isPurchased()
                            ? binding.itemName.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
                            : binding.itemName.getPaintFlags()
                            & ~Paint.STRIKE_THRU_TEXT_FLAG
            );

            binding.toggleIcon.setOnClickListener(
                    view -> listener.onToggle(item)
            );
            binding.getRoot().setOnClickListener(
                    view -> listener.onEdit(item)
            );
        }

        @NonNull
        private String formatQuantity(
                double value,
                @NonNull String unit
        ) {
            String amount =
                    Math.abs(value - Math.rint(value)) < 0.01
                            ? String.valueOf(
                                    (int) Math.rint(value)
                            )
                            : String.format(
                                    Locale.US,
                                    "%.1f",
                                    value
                            );
            return amount
                    + " "
                    + ("unit".equals(unit) ? "u" : unit);
        }
    }
}
