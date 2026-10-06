package com.cookpilot.university.features.home.presentation.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cookpilot.university.R;
import com.cookpilot.university.databinding.ItemWeekDayBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WeekDayAdapter
        extends RecyclerView.Adapter<WeekDayAdapter.DayViewHolder> {

    public interface OnDaySelectedListener {
        void onDaySelected(@NonNull LocalDate date);
    }

    private final List<LocalDate> days = new ArrayList<>();
    private final OnDaySelectedListener listener;
    private final DateTimeFormatter dayFormatter =
            DateTimeFormatter.ofPattern("EEE", new Locale("es", "PE"));

    private LocalDate selectedDate = LocalDate.now();

    public WeekDayAdapter(@NonNull OnDaySelectedListener listener) {
        this.listener = listener;
        rebuildWeek(selectedDate);
    }

    public void setSelectedDate(@NonNull LocalDate date) {
        if (!sameWeek(selectedDate, date)) {
            rebuildWeek(date);
        }
        selectedDate = date;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemWeekDayBinding binding = ItemWeekDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DayViewHolder holder,
            int position
    ) {
        LocalDate date = days.get(position);
        holder.bind(date, date.equals(selectedDate));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    private void rebuildWeek(@NonNull LocalDate anchor) {
        days.clear();
        LocalDate monday = startOfWeek(anchor);
        for (int index = 0; index < 7; index++) {
            days.add(monday.plusDays(index));
        }
    }

    private boolean sameWeek(
            @NonNull LocalDate left,
            @NonNull LocalDate right
    ) {
        return startOfWeek(left).equals(startOfWeek(right));
    }

    @NonNull
    private LocalDate startOfWeek(@NonNull LocalDate date) {
        return date.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );
    }

    final class DayViewHolder extends RecyclerView.ViewHolder {

        private final ItemWeekDayBinding binding;

        DayViewHolder(ItemWeekDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LocalDate date, boolean selected) {
            String shortDay = dayFormatter.format(date).replace(".", "");
            shortDay = Character.toUpperCase(shortDay.charAt(0))
                    + shortDay.substring(1);

            binding.dayName.setText(shortDay);
            binding.dayNumber.setText(String.valueOf(date.getDayOfMonth()));

            int primary = ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    R.color.cook_primary
            );
            int transparent = ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    android.R.color.transparent
            );

            binding.dayCard.setStrokeWidth(
                    selected
                            ? binding.getRoot().getResources()
                            .getDimensionPixelSize(R.dimen.cook_border_2)
                            : 0
            );
            binding.dayCard.setStrokeColor(
                    selected ? primary : transparent
            );

            binding.getRoot().setOnClickListener(
                    view -> listener.onDaySelected(date)
            );
        }
    }
}
