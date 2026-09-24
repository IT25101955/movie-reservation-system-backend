package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.Theatre;
import com.cinemax.cinemax.repository.TheatreRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    // Constructor injection supplies the dependencies this class needs
    public TheatreService(TheatreRepository theatreRepository) {

        this.theatreRepository = theatreRepository;
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
    public Theatre saveTheatre(Theatre theatre) {

        if (theatre.getId() != null) {
            Theatre existing = getTheatreById(theatre.getId());
            theatre.setActive(existing.isActive());
        }

        return theatreRepository.save(theatre);
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Deactivate the record while preserving existing booking history
    public void deleteTheatre(Long id) {

        Theatre theatre = getTheatreById(id);
        // Deactivate this record without removing it from the database
        theatre.setActive(false);
        theatreRepository.save(theatre);
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Reactivate the saved record
    public void restoreTheatre(Long id) {

        Theatre theatre = getTheatreById(id);
        theatre.setActive(true);
        theatreRepository.save(theatre);
    }
}
