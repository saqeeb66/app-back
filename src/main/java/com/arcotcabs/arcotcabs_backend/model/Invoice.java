package com.arcotcabs.arcotcabs_backend.model;


import com.arcotcabs.arcotcabs_backend.model.enums.InvoiceStatus;
import lombok.Data;

import java.util.List;

@Data
public class Invoice {

    private String invoiceId;
    private String tripId;
    private String userId;
    private String driverId;

    private List<Expense> expenses;

    private double baseFare;
    private double taxAmount;
    private double discountAmount;
    private double extraCharges;

    private double totalAmount;
    private String invoicePdfUrl;

    private InvoiceStatus status;
    private long generatedAt;

    public Invoice() {
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getExtraCharges() {
        return extraCharges;
    }

    public void setExtraCharges(double extraCharges) {
        this.extraCharges = extraCharges;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getInvoicePdfUrl() {
        return invoicePdfUrl;
    }

    public void setInvoicePdfUrl(String invoicePdfUrl) {
        this.invoicePdfUrl = invoicePdfUrl;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }
}
