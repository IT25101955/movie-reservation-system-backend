package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface TheatreRepository
        extends JpaRepository<Theatre, Long> {

    // Load active theatres sorted by name
    List<Theatre> findByActiveTrueOrderByNameAsc();
}
