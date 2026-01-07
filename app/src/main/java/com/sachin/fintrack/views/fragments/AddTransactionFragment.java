package com.sachin.fintrack.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sachin.fintrack.AdmobAds.Admob;
import com.sachin.fintrack.R;
import com.sachin.fintrack.adapters.AccountsAdapter;
import com.sachin.fintrack.adapters.CategoryAdapter;
import com.sachin.fintrack.databinding.FragmentAddTransactionBinding;
import com.sachin.fintrack.databinding.ListDialogBinding;
import com.sachin.fintrack.models.Account;
import com.sachin.fintrack.models.Category;
import com.sachin.fintrack.models.Transaction;
import com.sachin.fintrack.utils.Constants;
import com.sachin.fintrack.utils.Helper;
import com.sachin.fintrack.views.activites.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    public AddTransactionFragment() {
        // Required empty public constructor
    }

    FragmentAddTransactionBinding binding;//ek aisi screen hai jo niche se oopar ki taraf slide hokar aati hai
    Transaction transaction;
    //-------------------------------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAddTransactionBinding.inflate(inflater);
        transaction = new Transaction();

        // Default values (Starting Settings): Inhein null nahi chhorna taake crash na ho
        transaction.setType(Constants.EXPENSE);
        transaction.setDate(Calendar.getInstance().getTime());
        transaction.setAccount("Cash");
        // Category ko shuru mein null rakhein taake validation check ho sakay
        transaction.setCategory(null);

        binding.incomeBtn.setOnClickListener(view -> {//Button dabne par kya action lena hai.
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.income_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.greenColor));
            transaction.setType(Constants.INCOME);
        });

        binding.expenseBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.expense_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.redColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.textColor));
            transaction.setType(Constants.EXPENSE);
        });
//------------------------------------------------------------------------------------------------------------------
        binding.date.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
            datePickerDialog.setOnDateSetListener((datePicker, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);

                String dateToShow = Helper.formatDate(calendar.getTime());
                binding.date.setText(dateToShow);

                transaction.setDate(calendar.getTime());
                transaction.setId(String.valueOf(calendar.getTime().getTime()));
            });
            datePickerDialog.show();
        });
//------------------------------------------------------------------------------------------------------------------------------------
        //Category Selection Dialog
        binding.category.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();// alert dialog:ek popup dabba banane
            categoryDialog.setView(dialogBinding.getRoot());

            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), Constants.categories, category -> {
                binding.category.setText(category.getCategoryName());
                transaction.setCategory(category.getCategoryName());//Database ke liye user ki choice ko yaad rakhna.
                categoryDialog.dismiss();
            });
            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);//Woh container jahan saari categories ki list (icons + names) fit hoti hai.
            categoryDialog.show();
        });
//---------------------------------------------------------------------------------------------------------------------------------------------
        binding.account.setOnClickListener(c->{
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog accountsDialog = new AlertDialog.Builder(getContext()).create();
            accountsDialog.setView(dialogBinding.getRoot());

            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Bank"));
            accounts.add(new Account(0, "PayTm"));
            accounts.add(new Account(0, "EasyPaisa"));
            accounts.add(new Account(0, "Other"));

            AccountsAdapter adapter = new AccountsAdapter(getContext(), accounts, account -> {
                binding.account.setText(account.getAccountName());
                transaction.setAccount(account.getAccountName());
                accountsDialog.dismiss();
            });
            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            dialogBinding.recyclerView.setAdapter(adapter);
            accountsDialog.show();
        });

        binding.saveTransactionBtn.setOnClickListener(c-> {
            String amountStr = binding.amount.getText().toString();

            // 1. Amount Check
            if (amountStr.isEmpty()) {
                binding.amount.setError("Please enter amount");
                return;
            }

            // 2. Category Check (Ye crash fix karega)
            if (transaction.getCategory() == null || transaction.getCategory().isEmpty()) {
                Toast.makeText(getContext(), "Please select a category first", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String note = binding.note.getText().toString();

            // Type check
            if (transaction.getType().equals(Constants.EXPENSE)){
                transaction.setAmount(amount * -1);
            } else {
                transaction.setAmount(amount);
            }

            transaction.setNote(note);

            // Show Ad
            Admob.showInterstitial(requireActivity(), true);

            // Fragment safety check & Add Transaction
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).viewModel.addTransaction(transaction);//Add transaction:Data Database mein chala jata hai.
                ((MainActivity) getActivity()).getTransactions();// get transaction :Main screen ki list update ho jati hai.
                dismiss(); // Sirf tab band karein jab data save ho jaye
            }
        });

        return binding.getRoot();//Poore design ka "Main Handle" pakarna.
    }
}