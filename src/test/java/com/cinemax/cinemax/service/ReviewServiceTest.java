package com.cinemax.cinemax.service;

import com.cinemax.cinemax.dto.ReviewRequest;
import com.cinemax.cinemax.model.*;
import com.cinemax.cinemax.repository.BookingRepository;
import com.cinemax.cinemax.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private BookingRepository bookingRepository;
    private ReviewRepository reviewRepository;
    private MovieService movieService;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        // Use mock dependencies to test the review rules directly
        bookingRepository = mock(BookingRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        movieService = mock(MovieService.class);
        reviewService = new ReviewService(reviewRepository, bookingRepository, movieService);
    }

    @Test
    void confirmedBookingAllowsReviewWithoutShowDates() {
        // No show dates are provided because eligibility must never inspect them
        when(bookingRepository.findByUser_IdAndShowSchedule_Movie_IdAndStatus(
                1L, 2L, BookingStatus.CONFIRMED)).thenReturn(List.of(new Booking()));

        assertTrue(reviewService.canReview(1L, 2L));
    }

    @Test
    void customerWithoutConfirmedBookingCannotReview() {
        when(bookingRepository.findByUser_IdAndShowSchedule_Movie_IdAndStatus(
                1L, 2L, BookingStatus.CONFIRMED)).thenReturn(List.of());

        assertFalse(reviewService.canReview(1L, 2L));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void confirmedCustomerCanSubmitMultipleReviews() {
        when(bookingRepository.findByUser_IdAndShowSchedule_Movie_IdAndStatus(
                1L, 2L, BookingStatus.CONFIRMED)).thenReturn(List.of(new Booking()));
        when(movieService.getMovieById(2L)).thenReturn(new Movie());
        User customer = new User();
        customer.setId(1L);

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Another review");

        reviewService.submitReview(customer, 2L, request);
        reviewService.submitReview(customer, 2L, request);

        assertTrue(reviewService.canReview(1L, 2L));
        org.mockito.ArgumentCaptor<Review> saved =
                org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository, times(2)).save(saved.capture());
        assertNotSame(saved.getAllValues().get(0), saved.getAllValues().get(1));
    }

    @Test
    void inactiveMovieCanReceiveReviewFromConfirmedCustomer() {
        when(bookingRepository.findByUser_IdAndShowSchedule_Movie_IdAndStatus(
                1L, 2L, BookingStatus.CONFIRMED)).thenReturn(List.of(new Booking()));
        Movie movie = new Movie();
        movie.setActive(false);
        when(movieService.getMovieById(2L)).thenReturn(movie);
        User customer = new User();
        customer.setId(1L);
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment(" Great movie ");

        reviewService.submitReview(customer, 2L, request);

        // Inspect the saved review to confirm ownership and immediate persistence.
        org.mockito.ArgumentCaptor<Review> saved =
                org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(saved.capture());
        assertSame(customer, saved.getValue().getUser());
        assertSame(movie, saved.getValue().getMovie());
        assertEquals("Great movie", saved.getValue().getComment());
        verify(movieService, never()).getActiveMovieById(anyLong());
    }
}
