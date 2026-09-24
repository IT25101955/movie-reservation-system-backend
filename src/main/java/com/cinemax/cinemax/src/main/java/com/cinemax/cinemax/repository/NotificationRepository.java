package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Load this user's records with the newest first
    List<Notification>
        findByUser_IdOrderByCreatedAtDesc(
            Long userId
        );

    // Count unread notifications for this user
    long countByUser_IdAndReadFalse(
            Long userId
    );
}
