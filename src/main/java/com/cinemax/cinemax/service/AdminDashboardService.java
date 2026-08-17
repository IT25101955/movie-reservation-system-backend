package com.cinemax.cinemax.service;

import com.cinemax.cinemax.dto.DashboardStats;
import com.cinemax.cinemax.model.BookingStatus;
import com.cinemax.cinemax.model.PaymentStatus;
import com.cinemax.cinemax.repository.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// Spring creates this service to handle business rules
@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ShowScheduleRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    // Constructor injection supplies the dependencies this class needs
    public AdminDashboardService(
            UserRepository userRepository,
            MovieRepository movieRepository,
            ShowScheduleRepository showRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            PaymentRepository paymentRepository,
            RefundRepository refundRepository) {

        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
    }

    // Collect the counts and revenue shown on the admin dashboard
    public DashboardStats getStats() {

        // Count revenue only from successful payments on confirmed bookings
        BigDecimal revenue =
                paymentRepository.calculateRevenue(
                        PaymentStatus.SUCCESS,
                        BookingStatus.CONFIRMED
                );

        // Gross revenue = all successful payments
        BigDecimal grossRevenue =
                paymentRepository.sumAmountByStatus(
                        PaymentStatus.SUCCESS
                );
        if (grossRevenue == null) {
            grossRevenue = BigDecimal.ZERO;
        }

        // Total refunds = sum of all refund records
        BigDecimal totalRefunds =
                refundRepository.getTotalRefundAmount();
        if (totalRefunds == null) {
            totalRefunds = BigDecimal.ZERO;
        }

        // Net revenue = gross minus refunds
        BigDecimal netRevenue =
                grossRevenue.subtract(totalRefunds);

        return new DashboardStats(
                userRepository.count(),
                userRepository.countByEnabledTrue(),
                movieRepository.countByActiveTrue(),
                showRepository.countByActiveTrue(),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED),
                bookingSeatRepository.countByBooking_Status(BookingStatus.CONFIRMED),
                paymentRepository.countByStatus(PaymentStatus.PENDING),
                revenue,
                grossRevenue,
                totalRefunds,
                netRevenue
        );
    }
}

