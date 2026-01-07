package com.sachin.fintrack.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
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

        // Default set to Monthly
        Constants.SELECTED_TAB_STATS = Constants.MONTHLY;
        updateDate();

        // --- Income/Expense Toggle ---
        binding.incomeBtn.setOnClickListener(view -> {
            updateButtonUI(true);
            SELECTED_STATS_TYPE = Constants.INCOME;
            updateDate();
        });

        binding.expenseBtn.setOnClickListener(view -> {
            updateButtonUI(false);
            SELECTED_STATS_TYPE = Constants.EXPENSE;
            updateDate();
        });

        // --- Date Navigation ---
        binding.nextDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) calendar.add(Calendar.DATE, 1);
            else calendar.add(Calendar.MONTH, 1);
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) calendar.add(Calendar.DATE, -1);
            else calendar.add(Calendar.MONTH, -1);
            updateDate();
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {//Jab bhi user kisi tab par click karega, yeh code active ho jayega.
                if (tab.getText().toString().equalsIgnoreCase("Daily")) {
                    Constants.SELECTED_TAB_STATS = Constants.DAILY;
                } else {
                    Constants.SELECTED_TAB_STATS = Constants.MONTHLY;
                }
                updateDate();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // --- Observer ---//LiveData hai) par nazar rakhti hai.
        viewModel.categoriesTransactions.observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                setupPieChart(transactions);
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.pieChart.setVisibility(View.GONE);//screen se bilkul khatam kar dena.
            }
        });

        return binding.getRoot();
    }

    private void setupPieChart(java.util.List<Transaction> transactions) {
        //Screen ko Taiyar Karna
        binding.emptyState.setVisibility(View.GONE);//Kisi cheez ko gayab kar dena
        binding.pieChart.setVisibility(View.VISIBLE);

        ArrayList<PieEntry> entries = new ArrayList<>();//Pie Chart kk slice  ko sambhalegi
        Map<String, Double> categoryMap = new HashMap<>();//Hisaab Kitaab" ka register hai.

        // Kharchon ko Category ke mutabiq jama karna
        for (Transaction transaction : transactions) {
            String cat = transaction.getCategory();
            double amount = Math.abs(transaction.getAmount());
            categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + amount);
        }
        //Map se Graph ki Entries banana
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            // Label mein Category Name dila rahe hain
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Colors array with safe handling
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(requireContext(), R.color.blue_color));
        colors.add(ContextCompat.getColor(requireContext(), R.color.redColor));
        colors.add(ContextCompat.getColor(requireContext(), R.color.greenColor));
        colors.add(ContextCompat.getColor(requireContext(), R.color.light_blue));
        colors.add(ContextCompat.getColor(requireContext(), R.color.bank_color));
        dataSet.setColors(colors);

        dataSet.setSliceSpace(3f);

        // --- Labels Position Fix (Outside Slices) ---
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineColor(ContextCompat.getColor(requireContext(), R.color.black));

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        // Optional: Amount ko % mein dikhane ke liye
        // binding.pieChart.setUsePercentValues(true);

        binding.pieChart.setData(data);
        binding.pieChart.setCenterText(SELECTED_STATS_TYPE);
        binding.pieChart.setCenterTextSize(20f);
        binding.pieChart.setCenterTextColor(SELECTED_STATS_TYPE.equals(Constants.INCOME) ?
                ContextCompat.getColor(requireContext(), R.color.greenColor) :
                ContextCompat.getColor(requireContext(), R.color.redColor));

        binding.pieChart.setExtraOffsets(30, 30, 30, 30); // Mazeed space labels ke liye
        binding.pieChart.setDrawEntryLabels(true); // Category Names show karne ke liye
        binding.pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.black));
        binding.pieChart.setEntryLabelTextSize(11f);

        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.animateXY(800, 800);
        binding.pieChart.invalidate();
    }

    void updateDate() {
        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        }
        viewModel.getTransactions(calendar, SELECTED_STATS_TYPE);
    }

    private void updateButtonUI(boolean isIncome) {
        if (isIncome) {
            binding.incomeBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.income_selector));
            binding.expenseBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.default_selector));
            binding.incomeBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.greenColor));
            binding.expenseBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        } else {
            binding.incomeBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.default_selector));
            binding.expenseBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.expenseBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.redColor));
        }
    }
}