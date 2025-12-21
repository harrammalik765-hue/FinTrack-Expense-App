package com.sachin.fintrack.models;

import java.util.Date;

public class Transaction {

    private String type, category, account, note;
    private Date date;
    private double amount;
    private String id;


    public Transaction(){

    }

    public Transaction(String id, Date date, double amount, String note, String account, String category, String type) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.note = note;
        this.account = account;
        this.category = category;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
