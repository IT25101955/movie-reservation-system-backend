package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.ShowSchedule;
import com.cinemax.cinemax.repository.BookingRepository;
import com.cinemax.cinemax.repository.SeatHoldRepository;
import com.cinemax.cinemax.repository.ShowScheduleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class ShowScheduleService {

    private final ShowScheduleRepository showRepository;
    private final BookingRepository bookingRepository;
    private final SeatHoldRepository seatHoldRepository;

    // Constructor injection supplies the dependencies this class needs
    public ShowScheduleService(
            ShowScheduleRepository showRepository,
            BookingRepository bookingRepository,
            SeatHoldRepository seatHoldRepository) {

        this.showRepository = showRepository;
        this.bookingRepository = bookingRepository;
        this.seatHoldRepository = seatHoldRepository;
    }

    // Load all records for admin management, including inactive records
    public List<ShowSchedule> getAllShows() {

        return showRepository
                .findAllByOrderByShowDateAscStartTimeAsc();
    }

    // Search the database and report when the record is missing
    public ShowSchedule getShowById(Long id) {

        // Optional holds the showschedule when the database finds a match
        Optional<ShowSchedule> show = showRepository.findById(id);

        // Check whether the database search found a record
        if (show.isPresent()) {
            return show.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException("Show schedule not found");
    }

    // Reject shows with an inactive movie, screen, theatre or show
    public ShowSchedule getActiveShowById(Long id) {

        ShowSchedule show = getShowById(id);
        validateActiveForBooking(show);
        return show;
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Lock and validate the show before creating a booking
    public ShowSchedule getShowForBooking(Long id) {

        // Optional holds the showschedule when the database finds a match
        Optional<ShowSchedule> optionalShow =
                showRepository.findByIdForUpdate(id);

        // Stop when the database search found no record
        if (optionalShow.isEmpty()) {
            throw new IllegalArgumentException(
                    "This show is no longer available."
            );
        }

        ShowSchedule show = optionalShow.get();

        validateActiveForBooking(show);
        return show;
    }

    // Validate the schedule and protect details of existing bookings
    public ShowSchedule saveShow(
            ShowSchedule newShow) {

        if (newShow.getId() != null) {

            ShowSchedule existing = getShowById(newShow.getId());

            newShow.setActive(existing.isActive());

            if (bookingRepository.existsByShowSchedule_Id(newShow.getId())) {

                boolean scheduleChanged =
                        hasScheduleChanged(existing, newShow);

                if (scheduleChanged) {
                    throw new IllegalArgumentException(
                            "This show cannot be changed because customer bookings already exist."
                    );
                }
            }
        }

        validateShowDateTime(newShow);

        validateActiveDependencies(newShow);
        
        validateScreenAvailability(newShow);

        return showRepository.save(newShow);
    }

    // Check whether the edited show changes booked details
    private boolean hasScheduleChanged(
            ShowSchedule existing,
            ShowSchedule updated) {

        if (!existing.getMovie().getId()
                .equals(updated.getMovie().getId())) {
            return true;
        }

        if (!existing.getScreen().getId()
                .equals(updated.getScreen().getId())) {
            return true;
        }

        if (!existing.getShowDate().equals(updated.getShowDate())) {
            return true;
        }

        if (!existing.getStartTime().equals(updated.getStartTime())) {
            return true;
        }

        return existing.getTicketPrice()
                .compareTo(updated.getTicketPrice()) != 0;
    }

    // Require an active movie, screen and theatre
    private void validateActiveDependencies(ShowSchedule show) {

        if (!show.getMovie().isActive()) {
            throw new IllegalArgumentException(
                    "A show can only use an active movie."
            );
        }

        if (!show.getScreen().isActive() ||
            !show.getScreen().getTheatre().isActive()) {
            throw new IllegalArgumentException(
                    "A show can only use an active screen and theatre."
            );
        }
    }

    // Block new bookings when the show or its parents are inactive
    private void validateActiveForBooking(ShowSchedule show) {

        if (!show.isActive() ||
            !show.getMovie().isActive() ||
            !show.getScreen().isActive() ||
            !show.getScreen().getTheatre().isActive()) {
            throw new IllegalArgumentException(
                    "This show is no longer available for booking."
            );
        }
    }

    // Reject show dates and times in the past
    private void validateShowDateTime(
        ShowSchedule show) {

        LocalDateTime showDateTime =
                LocalDateTime.of(
                    show.getShowDate(),
                    show.getStartTime()
                );

        if (showDateTime.isBefore(
                LocalDateTime.now())) {

            throw new IllegalArgumentException(
                "Show date and time cannot be in the past."
            );
        }
    }

    // Check for overlapping active shows on the same screen
    private void validateScreenAvailability(
            ShowSchedule newShow) {

        List<ShowSchedule> existingShows =
                showRepository
                    .findByScreenIdAndShowDateAndActiveTrue(
                        newShow.getScreen().getId(),
                        newShow.getShowDate()
                    );

        LocalTime newStart =
                newShow.getStartTime();

        LocalTime newEnd =
                newStart.plusMinutes(
                    newShow
                        .getMovie()
                        .getDurationMinutes()
                );

        for (ShowSchedule existing :
                existingShows) {

            // Ignore itself during update
            if (newShow.getId() != null &&
                newShow.getId().equals(
                    existing.getId())) {

                continue;
            }

            LocalTime existingStart =
                    existing.getStartTime();

            LocalTime existingEnd =
                    existingStart.plusMinutes(
                        existing
                            .getMovie()
                            .getDurationMinutes()
                    );

            boolean overlaps =
                    newStart.isBefore(existingEnd)
                    &&
                    newEnd.isAfter(existingStart);

            if (overlaps) {

                throw new IllegalArgumentException(
                    "This screen already has another show during the selected time."
                );
            }
        }
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Deactivate the record while preserving existing booking history
    public void deleteShow(Long id) {

        // Optional holds the showschedule when the database finds a match
        Optional<ShowSchedule> optionalShow =
                showRepository.findByIdForUpdate(id);

        // Stop when the database search found no record
        if (optionalShow.isEmpty()) {
            throw new IllegalArgumentException(
                    "Show schedule does not exist."
            );
        }

        ShowSchedule show = optionalShow.get();


        seatHoldRepository
                .deleteByShowSchedule_Id(id);

        // Deactivate this record without removing it from the database
        show.setActive(false);
        showRepository.save(show);
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Reactivate the show after checking dependencies and time conflicts
    public void restoreShow(Long id) {

        // Optional holds the showschedule when the database finds a match
        Optional<ShowSchedule> optionalShow =
                showRepository.findByIdForUpdate(id);

        // Stop when the database search found no record
        if (optionalShow.isEmpty()) {
            throw new IllegalArgumentException(
                    "Show schedule does not exist."
            );
        }

        ShowSchedule show = optionalShow.get();

        show.setActive(true);

        validateActiveDependencies(show);
        validateScreenAvailability(show);

        showRepository.save(show);
    }

    // Keep future shows whose movie, screen and theatre are active
    public List<ShowSchedule>
    getFutureShowsForMovie(Long movieId) {

        LocalDateTime now =
                LocalDateTime.now();

        List<ShowSchedule> savedShows =
                showRepository
                        .findByMovieIdAndActiveTrueOrderByShowDateAscStartTimeAsc(
                                movieId
                        );

        List<ShowSchedule> availableShows = new ArrayList<>();

        for (ShowSchedule show : savedShows) {

            boolean dependenciesActive =
                    show.getMovie().isActive()
                    && show.getScreen().isActive()
                    && show.getScreen().getTheatre().isActive();

            LocalDateTime showDateTime =
                    LocalDateTime.of(
                            show.getShowDate(),
                            show.getStartTime()
                    );

            if (dependenciesActive && showDateTime.isAfter(now)) {
                availableShows.add(show);
            }
        }

        return availableShows;
    }
}
