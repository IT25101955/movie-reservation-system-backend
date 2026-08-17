package com.cinemax.cinemax.service;

import com.cinemax.cinemax.designpattern.ShowTimePolicy;
import com.cinemax.cinemax.dto.ShowBatchRequest;
import com.cinemax.cinemax.dto.ShowUpdateRequest;
import com.cinemax.cinemax.dto.CustomerShowDto;
import com.cinemax.cinemax.dto.DateScheduleGroup;
import com.cinemax.cinemax.dto.MovieScheduleGroup;
import com.cinemax.cinemax.dto.TheatreScheduleGroup;
import com.cinemax.cinemax.model.ShowSchedule;
import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.model.Theatre;
import com.cinemax.cinemax.repository.BookingCancellationRepository;
import com.cinemax.cinemax.repository.BookingRepository;
import com.cinemax.cinemax.repository.BookingSeatRepository;
import com.cinemax.cinemax.repository.PaymentRepository;
import com.cinemax.cinemax.repository.RefundRepository;
import com.cinemax.cinemax.repository.TicketRepository;
import com.cinemax.cinemax.repository.SeatHoldRepository;
import com.cinemax.cinemax.repository.ShowScheduleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Spring creates this service to handle business rules
@Service
public class ShowScheduleService {

    private final ShowScheduleRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final BookingCancellationRepository bookingCancellationRepository;
    private final RefundRepository refundRepository;

    // DESIGN PATTERN: Singleton - one shared timing policy for all shows
    private final ShowTimePolicy showTimePolicy;

    // Constructor injection supplies the dependencies this class needs
    @Autowired
    public ShowScheduleService(
            ShowScheduleRepository showRepository,
            BookingRepository bookingRepository,
            SeatHoldRepository seatHoldRepository,
            BookingSeatRepository bookingSeatRepository,
            PaymentRepository paymentRepository,
            TicketRepository ticketRepository,
            BookingCancellationRepository bookingCancellationRepository,
            RefundRepository refundRepository) {

        this.showRepository = showRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.bookingCancellationRepository = bookingCancellationRepository;
        this.refundRepository = refundRepository;
        this.showTimePolicy = ShowTimePolicy.getInstance();
    }

    public ShowScheduleService(
            ShowScheduleRepository showRepository,
            BookingRepository bookingRepository,
            SeatHoldRepository seatHoldRepository) {
        this(showRepository, bookingRepository, seatHoldRepository,
                null, null, null, null, null);
    }

    // Load all records for admin management, including inactive records
    public List<ShowSchedule> getAllShows() {

        return showRepository
                .findAllByOrderByShowDateAscStartTimeAsc();
    }

            public List<MovieScheduleGroup> getGroupedSchedules() {
            List<ShowSchedule> schedules = showRepository
                .findAllByOrderByMovieTitleAscTheatreNameAscShowDateAscStartTimeAsc();

            Map<Long, List<ShowSchedule>> byMovie = schedules.stream()
                .collect(Collectors.groupingBy(
                    show -> show.getMovie().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()));

            List<MovieScheduleGroup> result = new ArrayList<>();
            for (List<ShowSchedule> movieShows : byMovie.values()) {
                ShowSchedule firstMovieShow = movieShows.get(0);
                Map<Long, List<ShowSchedule>> byTheatre = movieShows.stream()
                    .collect(Collectors.groupingBy(
                        show -> show.getTheatre().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

                List<TheatreScheduleGroup> theatreGroups = new ArrayList<>();
                for (List<ShowSchedule> theatreShows : byTheatre.values()) {
                ShowSchedule firstTheatreShow = theatreShows.get(0);
                Map<LocalDate, List<ShowSchedule>> byDate = theatreShows.stream()
                    .collect(Collectors.groupingBy(
                                show -> show.getShowDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));
                List<DateScheduleGroup> dateGroups = byDate.entrySet().stream()
                    .map(entry -> new DateScheduleGroup(entry.getKey(), entry.getValue()))
                    .toList();
                theatreGroups.add(new TheatreScheduleGroup(
                    firstTheatreShow.getTheatre().getId(),
                    firstTheatreShow.getTheatre().getName(),
                    firstTheatreShow.getAdultPrice(),
                    firstTheatreShow.getChildPrice(),
                    firstTheatreShow.getVipPrice(),
                    dateGroups));
                }
                result.add(new MovieScheduleGroup(
                    firstMovieShow.getMovie().getId(),
                    firstMovieShow.getMovie().getTitle(),
                    theatreGroups));
            }
            return result;
            }

    @Transactional
    public void deleteAllSchedulesAndBookingsForMovie(Long movieId) {

        List<ShowSchedule> shows = showRepository.findByMovieId(movieId);
        List<Long> showIds = shows.stream().map(ShowSchedule::getId).toList();

        if (showIds.isEmpty()) {
            return;
        }

        // Step 1: Remove seat holds (reference ShowSchedule)
        seatHoldRepository.deleteByShowSchedule_IdIn(showIds);

        List<Long> bookingIds = bookingRepository
                .findByShowSchedule_IdIn(showIds)
                .stream()
                .map(booking -> booking.getId())
                .toList();

        if (!bookingIds.isEmpty()) {


            // =====================================================
            // 1. DELETE REFUNDS FIRST
            // =====================================================
            //
            // Refund references Booking and BookingCancellation.
            //
            refundRepository
                    .deleteByBooking_IdIn(
                            bookingIds
                    );

            refundRepository.flush();



            // =====================================================
            // 2. DELETE CANCELLATION REQUESTS
            // =====================================================
            //
            // Cancellation requests reference Booking.
            //
            bookingCancellationRepository
                    .deleteByBooking_IdIn(
                            bookingIds
                    );

            bookingCancellationRepository.flush();



            // =====================================================
            // 3. DELETE BOOKING SEATS
            // =====================================================

            bookingSeatRepository
                    .deleteByBooking_IdIn(
                            bookingIds
                    );

            bookingSeatRepository.flush();



            // =====================================================
            // 4. DELETE TICKETS
            // =====================================================

            ticketRepository
                    .deleteByBooking_IdIn(
                            bookingIds
                    );

            ticketRepository.flush();



            // =====================================================
            // 5. DELETE PAYMENTS
            // =====================================================

            paymentRepository
                    .deleteByBooking_IdIn(
                            bookingIds
                    );

            paymentRepository.flush();



            // =====================================================
            // 6. DELETE BOOKINGS LAST
            // =====================================================

            bookingRepository
                    .deleteByShowSchedule_IdIn(
                            showIds
                    );

            bookingRepository.flush();
        }

        // Step 7: Delete the show schedules
        showRepository.deleteByMovieId(movieId);
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

    // Reject shows with an inactive movie, theatre or show
    public ShowSchedule getActiveShowById(Long id) {

        ShowSchedule show = getShowById(id);
        validateActiveForBooking(show);
        return show;
    }

    public List<LocalDate> getAvailableDates(Long movieId) {
        LocalDate today = LocalDate.now();
        return showRepository.findAvailableDates(movieId, today, LocalTime.now());
    }

    public List<CustomerShowDto> getAvailableShows(Long movieId, LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return showRepository.findAvailableShows(movieId, date).stream()
                .filter(show -> !date.equals(today) || show.getStartTime().isAfter(now))
                .map(show -> new CustomerShowDto(
                        show.getId(), show.getTheatre().getId(), show.getTheatre().getName(),
                        show.getShowDate(), show.getStartTime(), show.getEndTime(),
                        show.getAdultPrice(), show.getChildPrice(), show.getVipPrice()))
                .toList();
    }

    public List<ShowSchedule> getShowsForEdit(Long showId) {
        ShowSchedule selected = getShowById(showId);
        return showRepository.findByMovieIdAndTheatreIdAndShowDateAndActiveTrue(
                selected.getMovie().getId(),
                selected.getTheatre().getId(),
                selected.getShowDate());
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

    @Transactional
    public List<ShowSchedule> createBatch(
            ShowBatchRequest request,
            Movie movie,
            Theatre theatre) {

        validateBatchRequest(request);

        List<ShowSchedule> requestedShows = new ArrayList<>();
        for (LocalDate date = request.getStartDate();
             !date.isAfter(request.getEndDate());
             date = date.plusDays(1)) {
            for (LocalTime startTime : request.getStartTimes()) {
                ShowSchedule show = new ShowSchedule();
                show.setMovie(movie);
                show.setTheatre(theatre);
                show.setShowDate(date);
                show.setStartTime(startTime);
                show.setEndTime(calculateEndTime(movie, date, startTime).toLocalTime());
                show.setAdultPrice(request.getAdultPrice());
                show.setChildPrice(request.getChildPrice());
                show.setVipPrice(request.getVipPrice());
                requestedShows.add(show);
            }
        }

        validateCombinedSchedule(theatre.getId(), requestedShows);
        return showRepository.saveAll(requestedShows);
    }

    @Transactional
    public void updateDay(ShowUpdateRequest request) {
        if (request.getChildPrice().compareTo(request.getAdultPrice()) > 0) {
            throw new IllegalArgumentException("Child price cannot exceed adult price.");
        }
        List<ShowSchedule> existing = showRepository.findByMovieIdAndTheatreIdAndShowDateAndActiveTrue(
            request.getMovieId(), request.getTheatreId(), request.getShowDate());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("No active shows found for this day.");
        }
        Movie movie = existing.get(0).getMovie();
        Theatre theatre = existing.get(0).getTheatre();
        Set<Long> retainedIds = request.getShowIds() == null
                ? Set.of() : new HashSet<>(request.getShowIds());
        List<ShowSchedule> changed = new ArrayList<>();

        for (int index = 0; index < request.getStartTimes().size(); index++) {
            ShowSchedule show;
            Long id = request.getShowIds() != null && index < request.getShowIds().size()
                    ? request.getShowIds().get(index) : null;
            if (id == null) {
                show = new ShowSchedule();
            } else {
                show = getShowById(id);
                if (bookingRepository.existsByShowSchedule_Id(id)
                        && !show.getStartTime().equals(request.getStartTimes().get(index))) {
                    throw new IllegalArgumentException("Booked shows cannot change time.");
                }
            }
            show.setMovie(movie);
            show.setTheatre(theatre);
            show.setShowDate(request.getShowDate());
            show.setStartTime(request.getStartTimes().get(index));
            show.setEndTime(calculateEndTime(movie, request.getShowDate(), show.getStartTime()).toLocalTime());
            show.setAdultPrice(request.getAdultPrice());
            show.setChildPrice(request.getChildPrice());
            show.setVipPrice(request.getVipPrice());
            show.setActive(true);
            changed.add(show);
        }

        List<ShowSchedule> allTheatreShows = new ArrayList<>(
                showRepository.findByTheatreIdAndActiveTrue(theatre.getId()));
        allTheatreShows.removeIf(show -> existing.stream()
                .anyMatch(old -> old.getId().equals(show.getId())));
        allTheatreShows.addAll(changed);
        validateCombinedSchedule(allTheatreShows);
        for (ShowSchedule old : existing) {
            if (!retainedIds.contains(old.getId())) {
                if (bookingRepository.existsByShowSchedule_Id(old.getId())) {
                    throw new IllegalStateException(
                            "Booked shows cannot be removed.");
                }
                seatHoldRepository.deleteByShowSchedule_Id(old.getId());
                showRepository.delete(old);
            }
        }
        showRepository.saveAll(changed);
    }

    private void validateBatchRequest(ShowBatchRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be on or after start date.");
        }
        if (request.getChildPrice().compareTo(request.getAdultPrice()) > 0) {
            throw new IllegalArgumentException("Child price cannot exceed adult price.");
        }
        if (new HashSet<>(request.getStartTimes()).size() != request.getStartTimes().size()) {
            throw new IllegalArgumentException("Duplicate show times are not allowed.");
        }
    }

    private LocalDateTime calculateEndTime(
            Movie movie,
            LocalDate date,
            LocalTime startTime) {
        // Singleton Pattern: all schedule calculations use the same policy object.
        return showTimePolicy.calculateEndTime(movie, date, startTime);
    }

    private void validateCombinedSchedule(
            Long theatreId,
            List<ShowSchedule> requestedShows) {

        List<ShowSchedule> allShows = new ArrayList<>(
                showRepository.findByTheatreIdAndActiveTrue(theatreId));
        allShows.addAll(requestedShows);

        validateCombinedSchedule(allShows);
        }

        private void validateCombinedSchedule(List<ShowSchedule> allShows) {

        allShows.sort(Comparator.comparing(show ->
                LocalDateTime.of(show.getShowDate(), show.getStartTime())));

        for (int index = 0; index < allShows.size() - 1; index++) {
            ShowSchedule current = allShows.get(index);
            ShowSchedule next = allShows.get(index + 1);
            LocalDateTime currentEnd = calculateEndTime(
                    current.getMovie(), current.getShowDate(), current.getStartTime());
            LocalDateTime earliestNextStart =
                    showTimePolicy.earliestNextStart(currentEnd);

                if (LocalDateTime.of(next.getShowDate(), next.getStartTime())
                    .isBefore(earliestNextStart)) {
                throw new IllegalArgumentException(
                        "There must be at least a 1 hour gap between shows.");
            }
        }
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
        newShow.setEndTime(calculateEndTime(
            newShow.getMovie(),
            newShow.getShowDate(),
            newShow.getStartTime()).toLocalTime());

        validateActiveDependencies(newShow);
        
        validateTheatreAvailability(newShow);

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

        if (!existing.getTheatre().getId()
            .equals(updated.getTheatre().getId())) {
            return true;
        }

        if (!existing.getShowDate().equals(updated.getShowDate())) {
            return true;
        }

        if (!existing.getStartTime().equals(updated.getStartTime())) {
            return true;
        }

        return existing.getAdultPrice().compareTo(updated.getAdultPrice()) != 0
            || existing.getChildPrice().compareTo(updated.getChildPrice()) != 0
            || existing.getVipPrice().compareTo(updated.getVipPrice()) != 0;
    }

    // Require an active movie and theatre
    private void validateActiveDependencies(ShowSchedule show) {

        if (!show.getMovie().isActive()) {
            throw new IllegalArgumentException(
                    "A show can only use an active movie."
            );
        }

        if (!show.getTheatre().isActive()) {
            throw new IllegalArgumentException(
                "A show can only use an active theatre."
            );
        }
    }

    // Block new bookings when the show or its parents are inactive
    private void validateActiveForBooking(ShowSchedule show) {

        if (!show.isActive() ||
            !show.getMovie().isActive() ||
            !show.getTheatre().isActive()) {
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

    // Check the candidate against every active show in the theatre.
    private void validateTheatreAvailability(
            ShowSchedule newShow) {
        List<ShowSchedule> allShows = new ArrayList<>(
                showRepository.findByTheatreIdAndActiveTrue(
                        newShow.getTheatre().getId()));
        allShows.removeIf(existing -> newShow.getId() != null
                && newShow.getId().equals(existing.getId()));
        allShows.add(newShow);
        validateCombinedSchedule(allShows);
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Permanently remove a show when it has no bookings
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


        if (bookingRepository.existsByShowSchedule_Id(id)) {
            throw new IllegalStateException(
                    "This show has bookings and cannot be permanently deleted.");
        }

        seatHoldRepository.deleteByShowSchedule_Id(id);
        showRepository.delete(show);
        showRepository.flush();
    }

    // Run these database steps together; runtime failures roll them back


    // Keep future shows whose movie and theatre are active
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
                    && show.getTheatre().isActive();

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
