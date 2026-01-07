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

    Context context;// App ki screen ki maloomat
    ArrayList<Account> accountArrayList;// Woh list jis mein saare accounts ka data hai

    public interface AccountClickListener{//Interface ek signal hai jo batata hai ke "User ne kisi account ko touch kiya hai"
        void onAccountClicked(Account account);// Jab user kisi account par click kare
    }
//---------------------------------------------------------------------------------------------------------------------------------------
    AccountClickListener accountClickListener;
    //Constructure
    public AccountsAdapter(Context context, ArrayList<Account> accountArrayList, AccountClickListener accountClickListener) {
        this.context = context;
        this.accountArrayList = accountArrayList;
        this.accountClickListener = accountClickListener;
    }
//------------------------------------------------------------------------------------------------------------------------------
    @NonNull
    @Override
    //onCreateViewHolder (Design Chunna)Iska matlab hai XML design file ko asli view mein badalna.
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountViewHolder(LayoutInflater.from(context).inflate(R.layout.row_account, parent, false));
    }
            //LayoutInflater:Yeh XML  file ko Java view mein badalne wali machine hai.
            // AccountViewHolder:jis mein design fit hai

//------------------------------------------------------------------------------------------------------------------------------------------

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
    }//"List kitni lambi hai?".

    public class AccountViewHolder extends RecyclerView.ViewHolder {//Android ise pehchan sake.

        RowAccountBinding binding;//(TextView, ImageView) ka direct address apne paas rakhta hai.

        public AccountViewHolder(@NonNull View itemView) {//itemView woh poora design hai jo onCreateViewHolder mein bana tha.
            super(itemView);        // itemview (super class) ke hawale kar deta hai.
            binding = RowAccountBinding.bind(itemView);   //design (itemView) aur  code (binding) ko aapas mein jor deti hai
        }
    }
}
