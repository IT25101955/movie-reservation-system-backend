package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface ScreenRepository
        extends JpaRepository<Screen, Long> {

    // Load the screens belonging to this theatre
    List<Screen> findByTheatreId(Long theatreId);

    // Load active screens in active theatres, sorted by theatre and screen name
    List<Screen> findByActiveTrueAndTheatre_ActiveTrueOrderByTheatre_NameAscNameAsc();

}
