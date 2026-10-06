package com.cookpilot.university.features.cooklist.presentation.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.cookpilot.university.databinding.DialogShoppingItemBinding;
import com.cookpilot.university.databinding.FragmentCookListBinding;
import com.cookpilot.university.features.cooklist.domain.model.ShoppingItem;
import com.cookpilot.university.features.cooklist.presentation.adapter.ShoppingItemAdapter;
import com.cookpilot.university.features.cooklist.presentation.viewmodel.CookListViewModel;
import com.cookpilot.university.features.cooklist.presentation.viewmodel.CookListViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CookListFragment extends Fragment
        implements ShoppingItemAdapter.Listener {

    private FragmentCookListBinding binding;
    private CookListViewModel viewModel;
    private ShoppingItemAdapter adapter;
    private List<ShoppingItem> currentItems =
            Collections.emptyList();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentCookListBinding.inflate(
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
                (CookPilotApplication) requireActivity()
                        .getApplication();

        viewModel = new ViewModelProvider(
                this,
                new CookListViewModelFactory(
                        application.getShoppingRepository(),
                        application.getCookPlanRepository()
                )
        ).get(CookListViewModel.class);

        configureSearch();
        configureList();
        observeState();

        binding.addItemButton.setImageResource(
                CookIcons.addCircle()
        );
        binding.estimatedPriceIcon.setImageResource(
                CookIcons.money()
        );
        binding.addItemButton.setOnClickListener(
                ignored -> showItemDialog(null)
        );
        binding.clearPurchasedButton.setOnClickListener(
                ignored -> viewModel.clearPurchased()
        );
    }

    private void configureSearch() {
        binding.searchIcon.setImageResource(CookIcons.search());

        binding.searchInput.addTextChangedListener(
                new TextWatcher() {
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
                        viewModel.setQuery(text.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable
                    ) {
                    }
                }
        );
    }

    private void configureList() {
        adapter = new ShoppingItemAdapter(this);
        binding.shoppingList.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.shoppingList.setAdapter(adapter);

        new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT
                ) {
                    @Override
                    public boolean onMove(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            @NonNull RecyclerView.ViewHolder target
                    ) {
                        return false;
                    }

                    @Override
                    public void onSwiped(
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            int direction
                    ) {
                        int position =
                                viewHolder.getBindingAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) {
                            return;
                        }

                        viewModel.delete(
                                adapter.getItem(position)
                        );
                    }
                }
        ).attachToRecyclerView(binding.shoppingList);
    }

    private void observeState() {
        viewModel.getItems().observe(
                getViewLifecycleOwner(),
                items -> {
                    currentItems = items == null
                            ? Collections.emptyList()
                            : items;
                    adapter.submitItems(currentItems);
                    renderEmptyState();
                    renderClearAction();
                }
        );

        viewModel.getEstimatedTotal().observe(
                getViewLifecycleOwner(),
                total -> binding.estimatedTotal.setText(
                        String.format(
                                Locale.US,
                                "S/ %.2f",
                                total == null ? 0.0 : total
                        )
                )
        );
    }

    private void renderEmptyState() {
        boolean empty = currentItems.isEmpty();
        binding.emptyState.setVisibility(
                empty ? View.VISIBLE : View.GONE
        );
        binding.shoppingList.setVisibility(
                empty ? View.GONE : View.VISIBLE
        );
    }

    private void renderClearAction() {
        boolean hasPurchased = false;
        for (ShoppingItem item : currentItems) {
            if (item.isPurchased()) {
                hasPurchased = true;
                break;
            }
        }

        binding.clearPurchasedButton.setEnabled(hasPurchased);
        binding.clearPurchasedButton.setAlpha(
                hasPurchased ? 1f : 0.45f
        );
    }

    @Override
    public void onToggle(@NonNull ShoppingItem item) {
        viewModel.togglePurchased(item);
    }

    @Override
    public void onEdit(@NonNull ShoppingItem item) {
        showItemDialog(item);
    }

    private void showItemDialog(
            @Nullable ShoppingItem item
    ) {
        DialogShoppingItemBinding dialogBinding =
                DialogShoppingItemBinding.inflate(
                        getLayoutInflater()
                );

        String[] units = {"g", "ml", "unit"};
        dialogBinding.unitInput.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        units
                )
        );

        if (item != null) {
            dialogBinding.nameInput.setText(item.getName());
            dialogBinding.quantityInput.setText(
                    formatNumber(item.getQuantity())
            );
            dialogBinding.unitInput.setText(
                    item.getUnit(),
                    false
            );
        } else {
            dialogBinding.unitInput.setText("g", false);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(
                        item == null
                                ? R.string.cooklist_add_item
                                : R.string.cooklist_edit_item
                )
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.auth_cancel, null)
                .setPositiveButton(
                        R.string.cooklist_save,
                        (dialog, which) -> {
                            String name = dialogBinding.nameInput
                                    .getText()
                                    .toString()
                                    .trim();
                            String unit = dialogBinding.unitInput
                                    .getText()
                                    .toString()
                                    .trim();
                            double quantity = parseQuantity(
                                    dialogBinding.quantityInput
                                            .getText()
                                            .toString()
                            );

                            if (name.isEmpty()
                                    || quantity <= 0
                                    || !isValidUnit(unit)) {
                                return;
                            }

                            if (item == null) {
                                viewModel.addManual(
                                        name,
                                        quantity,
                                        unit
                                );
                            } else {
                                viewModel.updateItem(
                                        item,
                                        name,
                                        quantity,
                                        unit
                                );
                            }
                        }
                )
                .show();
    }

    private boolean isValidUnit(@NonNull String unit) {
        return "g".equals(unit)
                || "ml".equals(unit)
                || "unit".equals(unit);
    }

    private double parseQuantity(@NonNull String value) {
        try {
            return Double.parseDouble(
                    value.replace(',', '.')
            );
        } catch (Exception ignored) {
            return 0;
        }
    }

    @NonNull
    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.01) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}