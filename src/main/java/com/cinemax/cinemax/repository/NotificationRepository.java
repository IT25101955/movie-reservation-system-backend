package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Notification;
import com.cinemax.cinemax.model.NotificationType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Repository responsible for notification database operations
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Get one customer's notifications.
    // Newest notifications appear first.
    List<Notification>
    findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );


    // Count unread notifications for a particular customer
    long countByUser_IdAndReadFalse(
            Long userId
    );


    // Get unread notifications.
    // Used by the "Mark All as Read" function.
    List<Notification>
    findByUser_IdAndReadFalse(
            Long userId
    );


    // Admin page only displays the newest 50 records.
    // This prevents a very large notification table
    // from making the page difficult to load.
    List<Notification>
    findTop50ByOrderByCreatedAtDesc();


    // Count all unread notifications in the system
    long countByReadFalse();


    // Delete every customer copy belonging to one broadcast.
    //
    // Example:
    // Admin sent CINEMAX20 to 30 users.
    // One operation can remove all 30 copies.
    @Modifying
    @Query(
        "DELETE FROM Notification n " +
        "WHERE n.broadcastId = :broadcastId"
    )
    int deleteByBroadcastId(
            @Param("broadcastId")
            String broadcastId
    );


    // Fallback for older promotions that may not
    // contain a broadcastId.
    @Modifying
    @Query(
        "DELETE FROM Notification n " +
        "WHERE n.title = :title " +
        "AND n.message = :message " +
        "AND n.type = :type"
    )
    int deleteByTitleAndMessageAndType(

            @Param("title")
            String title,

            @Param("message")
            String message,

            @Param("type")
            NotificationType type
    );


    // Permanently delete every notification
    // belonging to one customer.
    @Modifying
    @Query(
        "DELETE FROM Notification n " +
        "WHERE n.user.id = :userId"
    )
    int deleteByUser_Id(
            @Param("userId")
            Long userId
    );
}
