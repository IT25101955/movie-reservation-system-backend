package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.service.ReviewContentValidator;
import org.springframework.stereotype.Component;

// Spring creates this class so ReviewService can use the Decorator chain.
@Component
// This class creates the review validation Decorator chain in one place.
public class ReviewValidationContext {

    // This existing validator checks inappropriate review content.
    private final ReviewContentValidator contentValidator;

    // Spring gives the existing ReviewContentValidator to this constructor.
    public ReviewValidationContext(ReviewContentValidator contentValidator) {
        // Save the validator so it can be added to the Decorator chain.
        this.contentValidator = contentValidator;
    }

    // This method builds the decorators and validates one review comment.
    public void validate(String comment) {
        // Start with the basic validation component.
        ReviewValidation validation = new BasicReviewValidation();

        // Wrap the basic validation with the maximum-length Decorator.
        validation = new LengthValidationDecorator(validation, 500);

        // Wrap it again with the inappropriate-content Decorator.
        validation = new ProfanityValidationDecorator(validation, contentValidator);

        // Run the complete validation chain.
        validation.validate(comment);
    }
}
