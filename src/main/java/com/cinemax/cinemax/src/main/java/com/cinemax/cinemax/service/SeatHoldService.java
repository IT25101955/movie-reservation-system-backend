package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.*;

// Spring creates this service to handle business rules
@Service
public class SeatHoldService {

    private static final int HOLD_MINUTES = 5;

    private static final int MAX_SEATS = 6;


    private final SeatHoldRepository
            seatHoldRepository;

    private final SeatRepository
            seatRepository;

    private final BookingSeatRepository
            bookingSeatRepository;

    private final ShowScheduleService
            showScheduleService;


    // Constructor injection supplies the dependencies this class needs
    public SeatHoldService(
            SeatHoldRepository seatHoldRepository,
            SeatRepository seatRepository,
            BookingSeatRepository bookingSeatRepository,
            ShowScheduleService showScheduleService) {

        this.seatHoldRepository =
                seatHoldRepository;

        this.seatRepository =
                seatRepository;

        this.bookingSeatRepository =
                bookingSeatRepository;

        this.showScheduleService =
                showScheduleService;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Remove temporary seat holds whose time has expired
    public void cleanupExpiredHolds() {

        seatHoldRepository
                .deleteByHeldUntilBefore(
                        LocalDateTime.now()
                );
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Validate the selection and temporarily hold available seats
    public LocalDateTime holdSeats(
            Long showId,
            List<Long> selectedSeatIds,
            User user) {

        cleanupExpiredHolds();


        if (selectedSeatIds == null ||
            selectedSeatIds.isEmpty()) {

            throw new IllegalArgumentException(
                "Please select at least one seat."
            );
        }


        Set<Long> uniqueSeatIds =
                new LinkedHashSet<>(
                        selectedSeatIds
                );


        if (uniqueSeatIds.size() > MAX_SEATS) {

            throw new IllegalArgumentException(
                "You can select a maximum of "
                + MAX_SEATS
                + " seats."
            );
        }


        ShowSchedule show =
                showScheduleService
                    .getActiveShowById(showId);


        LocalDateTime showTime =
                LocalDateTime.of(
                        show.getShowDate(),
                        show.getStartTime()
                );


        if (!showTime.isAfter(
                LocalDateTime.now())) {

            throw new IllegalArgumentException(
                "This show has already started."
            );
        }


        List<Seat> seats =
                seatRepository
                    .findAllById(
                        uniqueSeatIds
                    );


        if (seats.size() !=
            uniqueSeatIds.size()) {

            throw new IllegalArgumentException(
                "One or more selected seats are invalid."
            );
        }


        for (Seat seat : seats) {

            if (!Objects.equals(
                    seat.getScreen().getId(),
                    show.getScreen().getId())) {

                throw new IllegalArgumentException(
                    "Invalid seat selected."
                );
            }
        }


        Set<Long> bookedSeatIds =
                new HashSet<>();


        for (BookingSeat bookingSeat :
                bookingSeatRepository
                    .findByShowSchedule_Id(
                        showId
                    )) {

            bookedSeatIds.add(
                bookingSeat
                    .getSeat()
                    .getId()
            );
        }


        List<SeatHold> activeHolds =
                seatHoldRepository
                    .findByShowSchedule_IdAndHeldUntilAfter(
                        showId,
                        LocalDateTime.now()
                    );


        for (Seat seat : seats) {

            if (bookedSeatIds.contains(
                    seat.getId())) {

                throw new IllegalArgumentException(
                    "Seat "
                    + seat.getSeatCode()
                    + " is already booked."
                );
            }


            for (SeatHold hold :
                    activeHolds) {

                if (hold.getSeat()
                        .getId()
                        .equals(seat.getId())
                    &&
                    !hold.getUser()
                        .getId()
                        .equals(user.getId())) {

                    throw new IllegalArgumentException(
                        "Seat "
                        + seat.getSeatCode()
                        + " is currently held by another customer."
                    );
                }
            }
        }


        // Replace this customer's old selection
        seatHoldRepository
                .deleteByUser_IdAndShowSchedule_Id(
                    user.getId(),
                    showId
                );

        // Send pending database changes now so constraint errors occur here
        seatHoldRepository.flush();


        LocalDateTime expiry =
                LocalDateTime.now()
                    .plusMinutes(
                        HOLD_MINUTES
                    );


        try {

            for (Seat seat : seats) {

                SeatHold hold =
                        new SeatHold();

                hold.setShowSchedule(show);

                hold.setSeat(seat);

                hold.setUser(user);

                hold.setHeldUntil(
                    expiry
                );

                seatHoldRepository
                        .save(hold);
            }

            // Send pending database changes now so constraint errors occur here
            seatHoldRepository.flush();

        } catch (
                DataIntegrityViolationException e) {

            throw new IllegalArgumentException(
                "One of the selected seats was just selected by another customer. Please choose again."
            );
        }


        return expiry;
    }


    // Load this customer's unexpired holds in seat order
    public List<SeatHold>
    getUserActiveHolds(
            Long userId,
            Long showId) {

        cleanupExpiredHolds();

        List<SeatHold> holds =
                seatHoldRepository
                    .findByUser_IdAndShowSchedule_IdAndHeldUntilAfter(
                        userId,
                        showId,
                        LocalDateTime.now()
                    );

        // Sort seat holds by seat code
        for (int first = 0; first < holds.size(); first++) {
            for (int second = first + 1;
                 second < holds.size();
                 second++) {

                SeatHold firstHold = holds.get(first);
                SeatHold secondHold = holds.get(second);

                String firstCode = firstHold.getSeat().getSeatCode();
                String secondCode = secondHold.getSeat().getSeatCode();

                if (secondCode.compareTo(firstCode) < 0) {
                    holds.set(first, secondHold);
                    holds.set(second, firstHold);
                }
            }
        }

        return holds;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Release this customer's temporary holds for the show
    public void cancelHolds(
            Long userId,
            Long showId) {

        seatHoldRepository
                .deleteByUser_IdAndShowSchedule_Id(
                    userId,
                    showId
                );
    }
}
