package com.cinemax.cinemax.designpattern;

// This is the base Decorator class used to wrap another validation object.
public abstract class ReviewValidationDecorator implements ReviewValidation {

    // This variable stores the validation object that is being wrapped.
    protected final ReviewValidation wrappedValidation;

    // The constructor receives the validation object that should run first.
    protected ReviewValidationDecorator(ReviewValidation wrappedValidation) {
        // Save the wrapped validation object.
        this.wrappedValidation = wrappedValidation;
    }

    // This method first runs the validation inside the wrapped object.
    @Override
    public void validate(String comment) {
        // Pass the review comment to the wrapped validation object.
        wrappedValidation.validate(comment);
    }
}
