package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.Theatre;
import com.cinemax.cinemax.model.Seat;
import com.cinemax.cinemax.model.Facility;
import com.cinemax.cinemax.model.SeatCategory;
import com.cinemax.cinemax.repository.TheatreRepository;
import com.cinemax.cinemax.repository.SeatRepository;
import com.cinemax.cinemax.repository.BookingSeatRepository;
import com.cinemax.cinemax.repository.SeatHoldRepository;
import com.cinemax.cinemax.repository.FacilityRepository;
import com.cinemax.cinemax.repository.ShowScheduleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

// Spring creates this service to handle business rules
@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final FacilityRepository facilityRepository;
    private final ShowScheduleRepository showScheduleRepository;

    // Constructor injection supplies the dependencies this class needs
    public TheatreService(
            TheatreRepository theatreRepository,
            SeatRepository seatRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatHoldRepository seatHoldRepository,
            FacilityRepository facilityRepository,
            ShowScheduleRepository showScheduleRepository) {

        this.theatreRepository = theatreRepository;
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.facilityRepository = facilityRepository;
        this.showScheduleRepository = showScheduleRepository;
    }

    // Load all records for admin management, including inactive records
    public List<Theatre> getAllTheatres() {
        return theatreRepository.findAll();
    }

    // Load only active records
    public List<Theatre> getActiveTheatres() {
        return theatreRepository.findByActiveTrueOrderByNameAsc();
    }

    // Search the database and report when the record is missing
    public Theatre getTheatreById(Long id) {

        // Optional holds the theatre when the database finds a match
        Optional<Theatre> theatre = theatreRepository.findById(id);

        // Check whether the database search found a record
        if (theatre.isPresent()) {
            return theatre.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException(
                "Theatre not found with id: " + id
        );
    }

    // Load the record and reject it if it is unavailable
    public Theatre getActiveTheatreById(Long id) {

        Theatre theatre = getTheatreById(id);

        if (!theatre.isActive()) {
            throw new IllegalArgumentException(
                    "The selected theatre is not active."
            );
        }

        return theatre;
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Save changes while preserving the record's active status
    public Theatre saveTheatre(
            Theatre theatre,
            List<Long> facilityIds) {
        Theatre existing = null;

        Set<Facility> selectedFacilities = facilityIds == null
                ? new HashSet<>()
                : new HashSet<>(facilityRepository.findAllById(facilityIds));
        theatre.setFacilities(selectedFacilities);

        if (theatre.getId() != null) {
            existing = getTheatreById(theatre.getId());
            theatre.setActive(existing.isActive());
        }

        Theatre saved = theatreRepository.save(theatre);

        if (saved.getId() == null) {
            return saved;
        }

        List<Seat> existingSeats = getSeats(saved.getId());
        boolean layoutChanged = existingSeats.isEmpty()
            || (existing != null
            && (!existing.getRowCount().equals(saved.getRowCount())
            || !existing.getSeatsPerRow().equals(saved.getSeatsPerRow())));

        if (layoutChanged && (!existingSeats.isEmpty()
                && (bookingSeatRepository.existsBySeat_Theatre_Id(saved.getId())
                || seatHoldRepository.existsBySeat_Theatre_Id(saved.getId())))) {
            throw new IllegalArgumentException(
                    "The seat layout cannot be changed because seats are already booked or held.");
        }

        if (layoutChanged) {
            seatRepository.deleteByTheatreId(saved.getId());
            seatRepository.flush();
            generateSeats(saved);
        }

        return saved;
    }

    public List<Seat> getSeats(Long theatreId) {
        return seatRepository.findByTheatreIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(theatreId);
    }

    @Transactional
    public void updateVipRows(Long theatreId, List<String> vipRows) {
        List<Seat> seats = seatRepository
                .findByTheatreIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(theatreId);
        for (Seat seat : seats) {
            boolean vip = vipRows != null && vipRows.contains(seat.getRowLabel());
            seat.setSeatCategory(vip ? SeatCategory.VIP : SeatCategory.STANDARD);
        }
        seatRepository.saveAll(seats);
    }

    public List<String> getVipRows(Long theatreId) {
        return seatRepository
                .findByTheatreIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(theatreId)
                .stream()
                .filter(seat -> seat.getSeatCategory() == SeatCategory.VIP)
                .map(seat -> seat.getRowLabel())
                .distinct()
                .sorted()
                .toList();
    }

    private void generateSeats(Theatre theatre) {
        for (int row = 0; row < theatre.getRowCount(); row++) {
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int number = 1; number <= theatre.getSeatsPerRow(); number++) {
                seatRepository.save(new Seat(
                        rowLabel,
                        number,
                        rowLabel + number,
                        theatre));
            }
        }
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Permanently remove a theatre when it has no scheduled shows
    public void deleteTheatre(Long id) {

        Theatre theatre = getTheatreById(id);
        if (showScheduleRepository.existsByTheatreId(id)) {
            throw new IllegalStateException(
                "This theatre has show schedules and cannot be permanently deleted.");
        }
        seatRepository.deleteByTheatreId(id);
        theatre.getFacilities().clear();
        theatreRepository.saveAndFlush(theatre);
        theatreRepository.delete(theatre);
        theatreRepository.flush();
    }

}
