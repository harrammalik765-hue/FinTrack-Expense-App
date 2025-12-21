package com.sachin.fintrack.views.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationBarView;
import com.sachin.fintrack.R;
import com.sachin.fintrack.databinding.ActivityMainBinding;
import com.sachin.fintrack.utils.Constants;
import com.sachin.fintrack.viewmodels.MainViewModel;
import com.sachin.fintrack.views.fragments.ProfileFragment;
import com.sachin.fintrack.views.fragments.StatsFragment;
import com.sachin.fintrack.views.fragments.TransactionsFragment;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        Constants.setCategories();
        calendar = Calendar.getInstance();

        // Pehla fragment load karein
        loadFragment(new TransactionsFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.transactions) {
                    Constants.SELECTED_TAB = 0;
                    loadFragment(new TransactionsFragment());
                } else if(id == R.id.stats){
                    Constants.SELECTED_TAB = 1;
                    loadFragment(new StatsFragment());
                } else if(id == R.id.accounts){
                    loadFragment(new ProfileFragment());
                } else if(id == R.id.more){
                    // Yahan humne message ko update kiya hai
                    String shareBody = "Hey, check out FinTrack! It's a professional Expense Manager app built with Java. \n\nCheck the source code here: https://github.com/harrammalik765-hue/FinTrack-Expense-App";

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "FinTrack App");
                    intent.putExtra(Intent.EXTRA_TEXT, shareBody);

                    // Is se user asani se app select kar sakega
                    startActivity(Intent.createChooser(intent, "Share via"));
                    return false;
                }
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    public void getTransactions() {
        viewModel.getTransactions(calendar);
    }
}