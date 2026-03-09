package com.arcotcabs.arcotcabs_backend.model;

import com.arcotcabs.arcotcabs_backend.model.enums.ExpenseType;

public class Expense {

    private String expenseId;
    private String tripId;

    private ExpenseType type;
    private double amount;
    private String description;

    private String addedBy;   // driverId
    private long createdAt;

    /* ================= GETTERS & SETTERS ================= */

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public ExpenseType getType() {
        return type;
    }

    public void setType(ExpenseType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
