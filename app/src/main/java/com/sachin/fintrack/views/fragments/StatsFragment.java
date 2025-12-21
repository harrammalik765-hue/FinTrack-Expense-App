package com.sachin.fintrack.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.tabs.TabLayout;
import com.sachin.fintrack.R;
import com.sachin.fintrack.databinding.FragmentStatsBinding;
import com.sachin.fintrack.models.Transaction;
import com.sachin.fintrack.utils.Constants;
import com.sachin.fintrack.utils.Helper;
import com.sachin.fintrack.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StatsFragment extends Fragment {

    FragmentStatsBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;

    String SELECTED_STATS_TYPE = Constants.EXPENSE;

    public StatsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        calendar = Calendar.getInstance();
        updateDate();

        // Income/Expense Button Listeners
        binding.incomeBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.income_selector));
            binding.expenseBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.default_selector));
            binding.expenseBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.incomeBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.greenColor));
            SELECTED_STATS_TYPE = Constants.INCOME;
            updateDate();
        });

        binding.expenseBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.default_selector));
            binding.expenseBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.expenseBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.redColor));
            SELECTED_STATS_TYPE = Constants.EXPENSE;
            updateDate();
        });

        // Date Navigation
        binding.nextDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) calendar.add(Calendar.DATE, 1);
            else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) calendar.add(Calendar.MONTH, 1);
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) calendar.add(Calendar.DATE, -1);
            else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) calendar.add(Calendar.MONTH, -1);
            updateDate();
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().toString().equals("Monthly")) {
                    Constants.SELECTED_TAB_STATS = Constants.MONTHLY;
                } else {
                    Constants.SELECTED_TAB_STATS = Constants.DAILY;
                }
                updateDate();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Observer for PieChart Data
        viewModel.categoriesTransactions.observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                binding.emptyState.setVisibility(View.GONE);
                binding.pieChart.setVisibility(View.VISIBLE);

                ArrayList<PieEntry> entries = new ArrayList<>();
                Map<String, Double> categoryMap = new HashMap<>();

                for (Transaction transaction : transactions) {
                    String cat = transaction.getCategory();
                    double amount = Math.abs(transaction.getAmount());
                    categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + amount);
                }

                for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
                    entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
                }

                PieDataSet dataSet = new PieDataSet(entries, "");

                // NEW: Fixing Slice Overlap
                dataSet.setSliceSpace(3f);
                dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                dataSet.setValueLinePart1OffsetPercentage(80f);
                dataSet.setValueLinePart1Length(0.4f);
                dataSet.setValueLinePart2Length(0.4f);
                dataSet.setValueLineColor(ContextCompat.getColor(requireContext(), R.color.black));

                ArrayList<Integer> colors = new ArrayList<>();
                colors.add(ContextCompat.getColor(requireContext(), R.color.blue_color));
                colors.add(ContextCompat.getColor(requireContext(), R.color.light_blue));
                colors.add(ContextCompat.getColor(requireContext(), R.color.bank_color));
                colors.add(ContextCompat.getColor(requireContext(), R.color.redColor));

                dataSet.setColors(colors);
                dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                dataSet.setValueTextSize(11f);

                PieData data = new PieData(dataSet);
                binding.pieChart.setData(data);

                // Chart Aesthetics and Legend Fix
                binding.pieChart.setExtraOffsets(25, 25, 25, 25);
                binding.pieChart.setDrawEntryLabels(false); // Clean Look
                binding.pieChart.setMinAngleForSlices(20f);

                Legend l = binding.pieChart.getLegend();
                l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                l.setDrawInside(false);
                l.setTextSize(12f);
                l.setWordWrapEnabled(true);

                binding.pieChart.getDescription().setEnabled(false);
                binding.pieChart.animateXY(800, 800);
                binding.pieChart.invalidate();

            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.pieChart.setVisibility(View.GONE);
            }
        });

        return binding.getRoot();
    }

    void updateDate() {
        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        }

        if (viewModel != null) {
            viewModel.getTransactions(calendar, SELECTED_STATS_TYPE);
        }
    }
}