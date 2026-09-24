package com.cinemax.cinemax.service;

import com.cinemax.cinemax.dto.ReviewRequest;

import com.cinemax.cinemax.model.Booking;
import com.cinemax.cinemax.model.BookingStatus;
import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.model.Review;
import com.cinemax.cinemax.model.ReviewStatus;
import com.cinemax.cinemax.model.User;

import com.cinemax.cinemax.repository.BookingRepository;
import com.cinemax.cinemax.repository.ReviewRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class ReviewService {

    private final ReviewRepository
            reviewRepository;

    private final BookingRepository
            bookingRepository;

    private final MovieService
            movieService;


    // Constructor injection supplies the dependencies this class needs
    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            MovieService movieService) {

        this.reviewRepository =
                reviewRepository;

        this.bookingRepository =
                bookingRepository;

        this.movieService =
                movieService;
    }

    // A confirmed booking is the evidence used for review eligibility
    public boolean hasBookedMovie(
            Long userId,
            Long movieId) {

        List<Booking> bookings =
                bookingRepository
                    .findByUser_IdAndShowSchedule_Movie_IdAndStatus(
                        userId,
                        movieId,
                        BookingStatus.CONFIRMED
                    );

        if (bookings.isEmpty()) {
            return false;
        }

        return true;
    }


    // Preserve the existing service API while using booking-only eligibility
    public boolean canReview(
            Long userId,
            Long movieId) {

        return hasBookedMovie(userId, movieId);
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Validate eligibility and save the review for moderation
    public Review submitReview(
            User user,
            Long movieId,
            ReviewRequest request) {


        if (!hasBookedMovie(
                user.getId(),
                movieId)) {

            throw new IllegalArgumentException(
                "You must have a confirmed booking before reviewing this movie."
            );
        }


        Movie movie =
                movieService
                    .getMovieById(
                        movieId
                    );


        Review review =
                new Review();


        review.setUser(user);

        review.setMovie(movie);

        review.setRating(
            request.getRating()
        );

        review.setComment(
            request.getComment().trim()
        );

        review.setStatus(
            ReviewStatus.PENDING
        );


        return reviewRepository
                .save(review);
    }


    // Load the reviews that customers are allowed to see
    public List<Review>
    getApprovedReviews(
            Long movieId) {

        // Get only approved reviews for this movie
        List<Review> reviews =
                reviewRepository
                        .findByMovie_IdAndStatusOrderByCreatedAtDesc(
                                movieId,
                                ReviewStatus.APPROVED
                        );

        return reviews;
    }


    // Calculate the average rating using approved reviews
    public double getAverageRating(
            Long movieId) {

        List<Review> reviews =
                getApprovedReviews(
                    movieId
                );


        if (reviews.isEmpty()) {
            return 0.0;
        }

        int totalRating = 0;

        for (Review review : reviews) {
            totalRating = totalRating + review.getRating();
        }

        return (double) totalRating / reviews.size();
    }


    // Count the approved reviews for this movie
    public int getApprovedReviewCount(
            Long movieId) {

        return getApprovedReviews(
                movieId
        ).size();
    }


    // Load all reviews for admin moderation
    public List<Review>
    getAllReviews() {

        return reviewRepository
                .findAllByOrderByCreatedAtDesc();
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Approve the review and record the moderation time
    public void approveReview(
            Long reviewId) {

        Review review =
                getReviewById(reviewId);


        review.setStatus(
            ReviewStatus.APPROVED
        );

        review.setModeratedAt(
            LocalDateTime.now()
        );


        reviewRepository.save(
            review
        );
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Reject the review and record the moderation time
    public void rejectReview(
            Long reviewId) {

        Review review =
                getReviewById(reviewId);


        review.setStatus(
            ReviewStatus.REJECTED
        );

        review.setModeratedAt(
            LocalDateTime.now()
        );


        reviewRepository.save(
            review
        );
    }


    // Search the database and report when the record is missing
    public Review getReviewById(
            Long reviewId) {

        // Optional holds the review when the database finds a match
        Optional<Review> review = reviewRepository.findById(reviewId);

        // Check whether the database search found a record
        if (review.isPresent()) {
            return review.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException("Review not found");
    }
}
