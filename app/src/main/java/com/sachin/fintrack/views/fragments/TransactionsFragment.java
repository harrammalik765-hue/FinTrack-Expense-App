package com.sachin.fintrack.views.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sachin.fintrack.R;
import com.sachin.fintrack.adapters.TransactionAdapter;
import com.sachin.fintrack.databinding.FragmentTransactionsBinding;
import com.sachin.fintrack.models.UserModel;
import com.sachin.fintrack.utils.Constants;
import com.sachin.fintrack.utils.Helper;
import com.sachin.fintrack.viewmodels.MainViewModel;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;

public class TransactionsFragment extends Fragment {

    FragmentTransactionsBinding binding;
    FirebaseFirestore firestore;
    Calendar calendar;
    public MainViewModel viewModel;

    public TransactionsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        firestore = FirebaseFirestore.getInstance();

        loadUserData();
        calendar = Calendar.getInstance();
        updateDate();

        // --- ARROWS LOGIC ---
        binding.nextDateBtn.setOnClickListener(c-> {
            if (Constants.SELECTED_TAB == Constants.DAILY){
                calendar.add(Calendar.DATE, 1);
            } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            }
            // Note: Summary (3) mein arrows block hain kyunki wo "All Year" hai
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c-> {
            if (Constants.SELECTED_TAB == Constants.DAILY){
                calendar.add(Calendar.DATE, -1);
            } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            }
            updateDate();
        });

        binding.floatingActionButton.setOnClickListener(c-> {
            new AddTransactionFragment().show(getParentFragmentManager(), null);
        });

        // --- TABS LOGIC FIXED FOR FULL YEAR SUMMARY ---
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabName = tab.getText().toString();

                if (tabName.equals("Daily")) {
                    Constants.SELECTED_TAB = 0;
                } else if (tabName.equals("Monthly")) {
                    Constants.SELECTED_TAB = 1;
                } else if (tabName.equals("Calendar")) {
                    Constants.SELECTED_TAB = 2;
                    showCalendar();
                    return;
                } else if (tabName.equals("Summary")) {
                    Constants.SELECTED_TAB = 3; // Now Full Year History
                } else if (tabName.equals("Notes")) {
                    Constants.SELECTED_TAB = 4;
                }
                updateDate();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.transactionsList.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.transactions.observe(getViewLifecycleOwner(), transactions -> {
            TransactionAdapter transactionAdapter = new TransactionAdapter(getActivity(), transactions);
            binding.transactionsList.setAdapter(transactionAdapter);
            binding.emptyState.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.totalIncome.observe(getViewLifecycleOwner(), aDouble -> binding.incomeLbl.setText(String.valueOf(aDouble)));
        viewModel.totalExpense.observe(getViewLifecycleOwner(), aDouble -> binding.expenseLbl.setText(String.valueOf(aDouble)));
        viewModel.totalAmount.observe(getViewLifecycleOwner(), aDouble -> binding.totalLbl.setText(String.valueOf(aDouble)));

        return binding.getRoot();
    }

    // --- UPDATED DATE LOGIC ---
    void updateDate() {
        if(Constants.SELECTED_TAB == Constants.DAILY || Constants.SELECTED_TAB == 2) {
            binding.previousDateBtn.setVisibility(View.VISIBLE);
            binding.nextDateBtn.setVisibility(View.VISIBLE);
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else if (Constants.SELECTED_TAB == 3) {
            // SUMMARY TAB: Arrows hide kar dein aur Full Year likhein
            binding.previousDateBtn.setVisibility(View.GONE);
            binding.nextDateBtn.setVisibility(View.GONE);
            binding.currentDate.setText("All Transactions " + calendar.get(Calendar.YEAR));
        } else {
            // MONTHLY & NOTES
            binding.previousDateBtn.setVisibility(View.VISIBLE);
            binding.nextDateBtn.setVisibility(View.VISIBLE);
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        }

        // Backend ko request bhejta hai
        viewModel.getTransactions(calendar);
    }

    private void showCalendar() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void loadUserData() {
        firestore.collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                UserModel model = documentSnapshot.toObject(UserModel.class);
                if (model != null) {
                    binding.Username.setText(model.getName());
                    Picasso.get().load(model.getProfile()).placeholder(R.drawable.friend_2).into(binding.profileImage);
                }
            }
        });
    }
}