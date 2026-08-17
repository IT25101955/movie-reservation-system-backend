package com.cinemax.cinemax.dto;

import java.math.BigDecimal;

// Carry revenue row values between application layers
public class RevenueRow {

    private final String label;
    private int transactions;
    private BigDecimal revenue;

    public RevenueRow(String label) {
        this.label = label;
        this.transactions = 0;
        this.revenue = BigDecimal.ZERO;
    }

    // Add this sale's amount and increment the transaction count
    public void addSale(BigDecimal amount) {
        transactions++;
        revenue = revenue.add(amount);
    }

    // Getters read field values; setters update them
    public String getLabel() { return label; }
    public int getTransactions() { return transactions; }
    public BigDecimal getRevenue() { return revenue; }
}
