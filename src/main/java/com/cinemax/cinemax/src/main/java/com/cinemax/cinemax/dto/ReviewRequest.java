package com.cinemax.cinemax.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Carry review request values between application layers
public class ReviewRequest {

    @NotNull(
        message = "Please select a rating"
    )
    @Min(
        value = 1,
        message = "Rating must be at least 1"
    )
    @Max(
        value = 5,
        message = "Rating cannot be greater than 5"
    )
    private Integer rating;


    @NotBlank(
        message = "Review comment is required"
    )
    @Size(
        min = 5,
        max = 1000,
        message =
            "Review must contain between 5 and 1000 characters"
    )
    private String comment;


    public ReviewRequest() {
    }


    // Getters read field values; setters update them
    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
