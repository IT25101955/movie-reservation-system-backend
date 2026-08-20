package com.cinemax.cinemax.designpattern;

// DESIGN PATTERN: Decorator - Wijesekara - Review and Recommendation Management.
// This is the Component interface for every review validation object.
public interface ReviewValidation {

    // This method checks whether a review comment is valid.
    void validate(String comment);
}
