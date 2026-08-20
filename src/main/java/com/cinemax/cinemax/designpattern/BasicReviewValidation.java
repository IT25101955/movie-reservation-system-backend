package com.cinemax.cinemax.designpattern;

// This is the Concrete Component that performs the basic review validation.
public class BasicReviewValidation implements ReviewValidation {

    // This method checks that the review comment is not empty.
    @Override
    public void validate(String comment) {
        // Stop when the comment is null or contains only spaces.
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty.");
        }
    }
}
