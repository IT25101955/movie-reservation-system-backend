package com.cinemax.cinemax.dto;

import java.math.BigDecimal;

// Carry dashboard stats values between application layers
public class DashboardStats {

    private final long totalUsers;
    private final long activeUsers;
    private final long totalMovies;
    private final long activeShows;
    private final long confirmedBookings;
    private final long ticketsSold;
    private final long pendingPayments;
    private final BigDecimal totalRevenue;

    public DashboardStats(
            long totalUsers,
            long activeUsers,
            long totalMovies,
            long activeShows,
            long confirmedBookings,
            long ticketsSold,
            long pendingPayments,
            BigDecimal totalRevenue) {

        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalMovies = totalMovies;
        this.activeShows = activeShows;
        this.confirmedBookings = confirmedBookings;
        this.ticketsSold = ticketsSold;
        this.pendingPayments = pendingPayments;
        this.totalRevenue = totalRevenue;
    }

    // Getters read field values; setters update them
    public long getTotalUsers() { return totalUsers; }
    public long getActiveUsers() { return activeUsers; }
    public long getTotalMovies() { return totalMovies; }
    public long getActiveShows() { return activeShows; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public long getTicketsSold() { return ticketsSold; }
    public long getPendingPayments() { return pendingPayments; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}
