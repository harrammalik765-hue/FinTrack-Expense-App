package com.sachin.fintrack.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sachin.fintrack.R;
import com.sachin.fintrack.adapters.TransactionAdapter;
import com.sachin.fintrack.databinding.FragmentTransactionsBinding;
import com.sachin.fintrack.models.Transaction;
import com.sachin.fintrack.models.UserModel;
import com.sachin.fintrack.utils.Constants;
import com.sachin.fintrack.utils.Helper;
import com.sachin.fintrack.viewmodels.MainViewModel;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class TransactionsFragment extends Fragment {

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentTransactionsBinding binding;
    FirebaseFirestore firestore;

    Calendar calendar;

    /*
    0 = Daily
    1 = Monthly
    2 = Calender
    3 = Summary
    4 = Notes
     */

    public MainViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        firestore = FirebaseFirestore.getInstance();

        loadUserData();

        calendar = Calendar.getInstance();
        updateDate();
        binding.nextDateBtn.setOnClickListener(c-> {
            if (Constants.SELECTED_TAB == Constants.DAILY){
                calendar.add(Calendar.DATE, 1);
            } else if (Constants.SELECTED_TAB ==Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            }
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c-> {
            if (Constants.SELECTED_TAB == Constants.DAILY){
                calendar.add(Calendar.DATE, -1);
            } else if (Constants.SELECTED_TAB ==Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            }
            updateDate();
        });

        binding.floatingActionButton.setOnClickListener(c-> {
            new AddTransactionFragment().show(getParentFragmentManager(), null);
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("Monthly")){
                    Constants.SELECTED_TAB = 1;
                    updateDate();
                } else if (tab.getText().equals("Daily")) {
                    Constants.SELECTED_TAB = 0;
                    updateDate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.transactionsList.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.transactions.observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                TransactionAdapter transactionAdapter = new TransactionAdapter(getActivity(), transactions);
                binding.transactionsList.setAdapter(transactionAdapter);
                if(transactions.size() > 0) {
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.totalIncome.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.incomeLbl.setText(String.valueOf(aDouble));
            }
        });

        viewModel.totalExpense.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.expenseLbl.setText(String.valueOf(aDouble));
            }
        });

        viewModel.totalAmount.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.totalLbl.setText(String.valueOf(aDouble));
            }
        });

        viewModel.getTransactions(calendar);
        return binding.getRoot();
    }

    void updateDate() {
        if(Constants.SELECTED_TAB == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else if(Constants.SELECTED_TAB == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        }
        viewModel.getTransactions(calendar);
    }

    private void loadUserData() {

        firestore.collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                UserModel model = documentSnapshot.toObject(UserModel.class);

                if (documentSnapshot.exists()){
                    assert model != null;
                    binding.Username.setText(model.getName());

                    Picasso.get()
                            .load(model.getProfile())
                            .placeholder(R.drawable.friend_2)
                            .into(binding.profileImage);

                }
            }
        });
    }
}