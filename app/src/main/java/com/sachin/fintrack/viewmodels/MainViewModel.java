package com.sachin.fintrack.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sachin.fintrack.models.Transaction;
import com.sachin.fintrack.utils.Constants;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    public MutableLiveData<List<Transaction>> categoriesTransactions = new MutableLiveData<>();

    public MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    public MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    public MutableLiveData<Double> totalAmount = new MutableLiveData<>();

    FirebaseFirestore firestore;
    FirebaseAuth auth;
    Calendar calendar;

    public MainViewModel(@NonNull Application application) {
        super(application);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private CollectionReference getUserTransactionsRef() {
        if (auth.getCurrentUser() == null) return null;
        String userId = auth.getCurrentUser().getUid();
        return firestore.collection("users").document(userId).collection("transactions");
    }

    // Stats page ke liye (Summary tab bottom bar wala)
    public void getTransactions(Calendar calendar, String type) {
        if (getUserTransactionsRef() == null) return;

        Calendar queryCalendar = (Calendar) calendar.clone();
        queryCalendar.set(Calendar.HOUR_OF_DAY, 0);
        queryCalendar.set(Calendar.MINUTE, 0);
        queryCalendar.set(Calendar.SECOND, 0);
        queryCalendar.set(Calendar.MILLISECOND, 0);

        Date startTime;
        Date endTime;

        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            startTime = queryCalendar.getTime();
            queryCalendar.add(Calendar.DATE, 1);
            endTime = queryCalendar.getTime();
        } else {
            queryCalendar.set(Calendar.DAY_OF_MONTH, 1);
            startTime = queryCalendar.getTime();
            queryCalendar.add(Calendar.MONTH, 1);
            endTime = queryCalendar.getTime();
        }

        getUserTransactionsRef()
                .whereEqualTo("type", type)
                .whereGreaterThanOrEqualTo("date", startTime)
                .whereLessThan("date", endTime)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Transaction> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) list.add(t);
                    }
                    categoriesTransactions.setValue(list);
                });
    }

    // Main List ke liye (Daily, Monthly, Calendar, Summary, Notes)
    public void getTransactions(Calendar calendar) {
        if (getUserTransactionsRef() == null) return;
        this.calendar = (Calendar) calendar.clone();

        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tempCalendar.set(Calendar.MINUTE, 0);
        tempCalendar.set(Calendar.SECOND, 0);
        tempCalendar.set(Calendar.MILLISECOND, 0);

        Date startTime;
        Date endTime;

        // 1. DAILY (0) ya CALENDAR (2) - Sirf 1 Din ka data
        if (Constants.SELECTED_TAB == 0 || Constants.SELECTED_TAB == 2) {
            startTime = tempCalendar.getTime();
            tempCalendar.add(Calendar.DATE, 1);
            endTime = tempCalendar.getTime();
        }
        // 2. SUMMARY (3) - AUTOMATICALLY POORA SAAL (Full Year)
        else if (Constants.SELECTED_TAB == 3) {
            tempCalendar.set(Calendar.MONTH, Calendar.JANUARY); // January se shuru
            tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
            startTime = tempCalendar.getTime();

            tempCalendar.set(Calendar.MONTH, Calendar.DECEMBER); // December tak
            tempCalendar.set(Calendar.DAY_OF_MONTH, 31);
            tempCalendar.add(Calendar.DATE, 1); // 1 January agle saal ki limit
            endTime = tempCalendar.getTime();
        }
        // 3. MONTHLY (1) ya NOTES (4) - Poore Mahine ka data
        else {
            tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
            startTime = tempCalendar.getTime();
            tempCalendar.add(Calendar.MONTH, 1);
            endTime = tempCalendar.getTime();
        }

        getUserTransactionsRef()
                .whereGreaterThanOrEqualTo("date", startTime)
                .whereLessThan("date", endTime)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Transaction> list = new ArrayList<>();
                    double income = 0, expense = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t == null) continue;

                        double amount = Math.abs(t.getAmount());
                        if (Constants.INCOME.equals(t.getType())) {
                            income += amount;
                        } else {
                            expense += amount;
                        }

                        // Filter for Notes Tab
                        if (Constants.SELECTED_TAB == 4) {
                            if (t.getNote() != null && !t.getNote().trim().isEmpty()) {
                                list.add(t);
                            }
                        } else {
                            list.add(t);
                        }
                    }

                    totalIncome.setValue(income);
                    totalExpense.setValue(expense);
                    totalAmount.setValue(income - expense);
                    transactions.setValue(list);
                });
    }

    public void addTransaction(Transaction transaction) {
        if (getUserTransactionsRef() == null || transaction == null) return;
        getUserTransactionsRef().add(transaction)
                .addOnSuccessListener(ref -> {
                    transaction.setId(ref.getId());
                    ref.update("id", ref.getId());
                    getTransactions(this.calendar != null ? this.calendar : Calendar.getInstance());
                });
    }

    public void deleteTransaction(Transaction transaction) {
        if (transaction != null && transaction.getId() != null && getUserTransactionsRef() != null) {
            getUserTransactionsRef().document(transaction.getId()).delete()
                    .addOnSuccessListener(unused -> getTransactions(this.calendar));
        }
    }
}