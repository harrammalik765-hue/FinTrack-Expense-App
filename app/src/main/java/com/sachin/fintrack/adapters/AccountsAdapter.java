package com.sachin.fintrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sachin.fintrack.R;
import com.sachin.fintrack.databinding.RowAccountBinding;
import com.sachin.fintrack.models.Account;

import java.util.ArrayList;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountViewHolder> {

    Context context;
    ArrayList<Account> accountArrayList;

    public interface AccountClickListener{
        void onAccountClicked(Account account);
    }

    AccountClickListener accountClickListener;

    public AccountsAdapter(Context context, ArrayList<Account> accountArrayList, AccountClickListener accountClickListener) {
        this.context = context;
        this.accountArrayList = accountArrayList;
        this.accountClickListener = accountClickListener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountViewHolder(LayoutInflater.from(context).inflate(R.layout.row_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountsAdapter.AccountViewHolder holder, int position) {
        Account account = accountArrayList.get(position);
        holder.binding.accountName.setText(account.getAccountName());
        holder.itemView.setOnClickListener(c-> {
            accountClickListener.onAccountClicked(account);
        });
    }

    @Override
    public int getItemCount() {
        return accountArrayList.size();
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {

        RowAccountBinding binding;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowAccountBinding.bind(itemView);
        }
    }
}
