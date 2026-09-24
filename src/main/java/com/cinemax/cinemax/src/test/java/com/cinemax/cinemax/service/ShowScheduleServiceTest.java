package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.ShowSchedule;
import com.cinemax.cinemax.repository.BookingRepository;
import com.cinemax.cinemax.repository.SeatHoldRepository;
import com.cinemax.cinemax.repository.ShowScheduleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

// Use mock repositories so these tests do not need a real database
@ExtendWith(MockitoExtension.class)
class ShowScheduleServiceTest {

    @Mock
    private ShowScheduleRepository showRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatHoldRepository seatHoldRepository;

    private ShowScheduleService showService;

    @BeforeEach
    void setUp() {
        // Create a fresh service using mock dependencies before each test
        showService = new ShowScheduleService(
                showRepository,
                bookingRepository,
                seatHoldRepository
        );
    }

    @Test
    void bookedShowIsSoftDeletedWithoutRemovingHistory() {

        Long showId = 12L;
        ShowSchedule show = new ShowSchedule();
        show.setId(showId);

        when(showRepository.findByIdForUpdate(showId))
                .thenReturn(Optional.of(show));

        // Deactivate the show and verify that its database record is preserved
        showService.deleteShow(showId);

        assertFalse(show.isActive());
        verify(seatHoldRepository).deleteByShowSchedule_Id(showId);
        verify(showRepository).save(show);
        verify(showRepository, never()).delete(any());
    }

    @Test
    void deletingShowNeverPhysicallyDeletesIt() {

        // Prepare a show that the mock repository can return
        Long showId = 13L;
        ShowSchedule show = new ShowSchedule();
        show.setId(showId);

        when(showRepository.findByIdForUpdate(showId))
                .thenReturn(Optional.of(show));

        showService.deleteShow(showId);

        assertFalse(show.isActive());
        verify(showRepository).save(show);
        verify(showRepository, never()).deleteById(anyLong());
        verify(showRepository, never()).delete(any());
    }
}
