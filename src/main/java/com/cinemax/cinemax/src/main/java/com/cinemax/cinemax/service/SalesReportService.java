package com.cinemax.cinemax.service;

import com.cinemax.cinemax.dto.RevenueRow;
import com.cinemax.cinemax.model.BookingStatus;
import com.cinemax.cinemax.model.Payment;
import com.cinemax.cinemax.model.PaymentStatus;
import com.cinemax.cinemax.model.ShowSchedule;
import com.cinemax.cinemax.repository.BookingSeatRepository;
import com.cinemax.cinemax.repository.PaymentRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

// Spring creates this service to handle business rules
@Service
public class SalesReportService {

    private final PaymentRepository paymentRepository;
    private final BookingSeatRepository bookingSeatRepository;

    // Constructor injection supplies the dependencies this class needs
    public SalesReportService(
            PaymentRepository paymentRepository,
            BookingSeatRepository bookingSeatRepository) {

        this.paymentRepository = paymentRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    // Calculate revenue and ticket totals for the selected dates
    public Map<String, Object> generateReport(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate != null &&
            endDate != null &&
            startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }

        List<Payment> savedPayments =
                paymentRepository
                        .findByStatusOrderByCreatedAtDesc(
                                PaymentStatus.SUCCESS
                        );

        // Keep only payments inside the selected date range
        List<Payment> successful = new ArrayList<>();

        for (Payment payment : savedPayments) {
            if (isWithinRange(payment, startDate, endDate)) {
                successful.add(payment);
            }
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long ticketsSold = 0;

        Map<String, RevenueRow> movieMap = new HashMap<>();
        Map<String, RevenueRow> theatreMap = new HashMap<>();

        for (Payment payment : successful) {

            totalRevenue = totalRevenue.add(payment.getAmount());

            ticketsSold += bookingSeatRepository
                    .findByBooking_Id(payment.getBooking().getId())
                    .size();

            ShowSchedule show =
                    payment.getBooking().getShowSchedule();

            String movieName;
            String theatreName;

            if (show == null) {
                movieName = "Unavailable movie";
                theatreName = "Unavailable theatre";
            } else {
                movieName = show.getMovie().getTitle();
                theatreName = show.getScreen().getTheatre().getName();
            }

            addRevenue(movieMap, movieName, payment.getAmount());
            addRevenue(theatreMap, theatreName, payment.getAmount());
        }

        List<RevenueRow> movieRows =
                sortedRows(movieMap);

        List<RevenueRow> theatreRows =
                sortedRows(theatreMap);

        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("transactions", successful.size());
        report.put("ticketsSold", ticketsSold);
        report.put("movieRevenue", movieRows);
        report.put("theatreRevenue", theatreRows);

        return report;
    }

    // Check whether the payment date falls inside the selected range
    private boolean isWithinRange(
            Payment payment,
            LocalDate startDate,
            LocalDate endDate) {

        // Do not count payments for cancelled bookings as revenue
        if (payment.getBooking().getStatus()
                != BookingStatus.CONFIRMED) {

            return false;
        }

        if (payment.getPaidAt() == null) {
            return false;
        }

        LocalDate paymentDate =
                payment.getPaidAt().toLocalDate();

        if (startDate != null && paymentDate.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && paymentDate.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    // Create or update the revenue total for one movie or theatre
    private void addRevenue(
            Map<String, RevenueRow> rows,
            String name,
            BigDecimal amount) {

        RevenueRow row = rows.get(name);

        if (row == null) {
            row = new RevenueRow(name);
            rows.put(name, row);
        }

        row.addSale(amount);
    }

    // Arrange report rows from highest revenue to lowest
    private List<RevenueRow> sortedRows(
            Map<String, RevenueRow> rows) {

        List<RevenueRow> result =
                new ArrayList<>(rows.values());

        // Sort from highest revenue to lowest revenue
        for (int first = 0; first < result.size(); first++) {
            for (int second = first + 1;
                 second < result.size();
                 second++) {

                RevenueRow firstRow = result.get(first);
                RevenueRow secondRow = result.get(second);

                if (secondRow.getRevenue()
                        .compareTo(firstRow.getRevenue()) > 0) {

                    result.set(first, secondRow);
                    result.set(second, firstRow);
                }
            }
        }

        return result;
    }
}
