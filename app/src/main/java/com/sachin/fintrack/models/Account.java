package com.sachin.fintrack.models;

public class Account {
    private double accountAmount;//Paise store karne ke liye
    private String accountName;// Naam store karne ke liye


    //Empty Constructor: Firebase ko data load karne ke liye iski zaroorat hoti hai
    public Account() {


    }
    // Constructor: Naya account banate waqt naam aur paise set karne ke liye
    public Account(double accountAmount, String accountName) {
        this.accountAmount = accountAmount;
        this.accountName = accountName;
    }
     // Getters: Data ko lyna karny kk liye
     // Setters: Data ko set/update karne ke liye
    public double getAccountAmount() {
        return accountAmount;
    }

    public void setAccountAmount(double accountAmount) {
        this.accountAmount = accountAmount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
