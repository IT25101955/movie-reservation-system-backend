package com.cinemax.cinemax.service;

import com.cinemax.cinemax.designpattern.ReviewValidationContext;
import com.cinemax.cinemax.dto.ReviewRequest;
import com.cinemax.cinemax.model.Booking;
import com.cinemax.cinemax.model.BookingStatus;
import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.model.Review;
import com.cinemax.cinemax.model.User;
import com.cinemax.cinemax.repository.BookingRepository;
import com.cinemax.cinemax.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final MovieService movieService;
    // DESIGN PATTERN: Decorator chain for review-content validation
    private final ReviewValidationContext reviewValidationContext;

    @Autowired
    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            MovieService movieService,
            ReviewValidationContext reviewValidationContext) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.movieService = movieService;
        this.reviewValidationContext = reviewValidationContext;
    }

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            MovieService movieService) {
        this(
                reviewRepository,
                bookingRepository,
                movieService,
                new ReviewValidationContext(new ReviewContentValidator())
        );
    }

    public boolean hasBookedMovie(Long userId, Long movieId) {
        List<Booking> bookings = bookingRepository
                .findByUser_IdAndShowSchedule_Movie_IdAndStatus(
                        userId, movieId, BookingStatus.CONFIRMED);
        return !bookings.isEmpty();
    }

    public boolean canReview(Long userId, Long movieId) {
        return hasBookedMovie(userId, movieId);
    }

    @Transactional
    public Review submitReview(
            User user,
            Long movieId,
            ReviewRequest request) {

        if (user == null) {
            throw new IllegalArgumentException("You must be logged in to submit a review.");
        }
        if (!hasBookedMovie(user.getId(), movieId)) {
            throw new IllegalArgumentException(
                    "You must have a confirmed booking before reviewing this movie.");
        }
        if (request == null || request.getRating() == null
                || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        String comment = request.getComment();

        // Decorator Pattern: base validation is wrapped with length and
        // profanity-validation decorators.
        reviewValidationContext.validate(comment);

        Movie movie = movieService.getMovieById(movieId);
        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setRating(request.getRating());
        review.setComment(comment.trim());

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForMovie(Long movieId) {
        return reviewRepository.findByMovie_IdOrderByCreatedAtDesc(movieId);
    }

    public List<Review> getApprovedReviews(Long movieId) {
        return getReviewsForMovie(movieId);
    }

    public double getAverageRating(Long movieId) {
        List<Review> reviews = getReviewsForMovie(movieId);
        if (reviews.isEmpty()) {
            return 0.0;
        }

        int totalRating = 0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }

        return (double) totalRating / reviews.size();
    }

    public int getApprovedReviewCount(Long movieId) {
        return getReviewsForMovie(movieId).size();
    }

    @Transactional
    public void deleteOwnReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        if (review.getUser() == null
                || !review.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException(
                    "You are not allowed to delete this review.");
        }

        reviewRepository.delete(review);
    }
}
