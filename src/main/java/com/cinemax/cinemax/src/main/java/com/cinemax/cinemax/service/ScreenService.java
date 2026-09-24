package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.Screen;
import com.cinemax.cinemax.model.Seat;
import com.cinemax.cinemax.repository.ScreenRepository;
import com.cinemax.cinemax.repository.SeatRepository;
import com.cinemax.cinemax.repository.BookingSeatRepository;
import com.cinemax.cinemax.repository.SeatHoldRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatHoldRepository seatHoldRepository;

    // Constructor injection supplies the dependencies this class needs
    public ScreenService(
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatHoldRepository seatHoldRepository) {

        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatHoldRepository = seatHoldRepository;
    }


    // Load all records for admin management, including inactive records
    public List<Screen> getAllScreens() {
        return screenRepository.findAll();
    }


    // Load active screens belonging to active theatres
    public List<Screen> getActiveScreens() {
        return screenRepository
                .findByActiveTrueAndTheatre_ActiveTrueOrderByTheatre_NameAscNameAsc();
    }


    // Load the screens belonging to the selected theatre
    public List<Screen> getScreensByTheatre(Long theatreId) {
        return screenRepository.findByTheatreId(theatreId);
    }


    // Search the database and report when the record is missing
    public Screen getScreenById(Long id) {

        // Optional holds the screen when the database finds a match
        Optional<Screen> screen = screenRepository.findById(id);

        // Check whether the database search found a record
        if (screen.isPresent()) {
            return screen.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException(
                "Screen not found with id: " + id
        );
    }


    // Load the record and reject it if it is unavailable
    public Screen getActiveScreenById(Long id) {

        Screen screen = getScreenById(id);

        if (!screen.isActive() || !screen.getTheatre().isActive()) {
            throw new IllegalArgumentException(
                    "The selected screen is not active."
            );
        }

        return screen;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Save the screen and rebuild seats only when its layout changes
    public Screen saveScreen(Screen screen) {

        boolean newScreen = screen.getId() == null;

        boolean layoutChanged = false;

        if (!newScreen) {

            // Get the saved screen before checking for layout changes
            Screen existing = getScreenById(screen.getId());

            screen.setActive(existing.isActive());

            layoutChanged =
                    !existing.getRowCount()
                            .equals(screen.getRowCount())
                    ||
                    !existing.getSeatsPerRow()
                            .equals(screen.getSeatsPerRow());

            if (layoutChanged &&
                (bookingSeatRepository.existsBySeat_Screen_Id(screen.getId()) ||
                 seatHoldRepository.existsBySeat_Screen_Id(screen.getId()))) {

                throw new IllegalArgumentException(
                        "The seat layout cannot be changed because seats are already booked or held."
                );
            }
        }


        Screen savedScreen =
                screenRepository.save(screen);


        if (newScreen) {

            generateSeats(savedScreen);

        } else if (layoutChanged) {

            seatRepository.deleteByScreenId(
                    savedScreen.getId()
            );

            // Send pending database changes now so constraint errors occur here
            seatRepository.flush();

            generateSeats(savedScreen);
        }


        return savedScreen;
    }


    // Create labelled seats from the screen's rows and seats per row
    private void generateSeats(Screen screen) {

        for (int row = 0;
             row < screen.getRowCount();
             row++) {

            String rowLabel =
                    String.valueOf(
                            (char) ('A' + row)
                    );


            for (int number = 1;
                 number <= screen.getSeatsPerRow();
                 number++) {

                String seatCode =
                        rowLabel + number;


                Seat seat = new Seat(
                        rowLabel,
                        number,
                        seatCode,
                        screen
                );


                seatRepository.save(seat);
            }
        }
    }


    // Load the screen's seats in row and seat-number order
    public List<Seat> getSeats(Long screenId) {

        return seatRepository
                .findByScreenIdOrderByRowLabelAscSeatNumberAsc(
                        screenId
                );
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Deactivate the record while preserving existing booking history
    public void deleteScreen(Long id) {

        Screen screen = getScreenById(id);
        // Deactivate this record without removing it from the database
        screen.setActive(false);
        screenRepository.save(screen);
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Reactivate the saved record
    public void restoreScreen(Long id) {

        Screen screen = getScreenById(id);
        screen.setActive(true);
        screenRepository.save(screen);
    }
}
