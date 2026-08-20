package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Movie;

import java.util.List;

// DESIGN PATTERN: Factory - Sathsarani - Movie Management.
// This is the Product interface used by all movie filter products.
public interface MovieFilter {

    // This method receives movies and returns the movies that match the filter.
    List<Movie> filter(List<Movie> movies);
}
