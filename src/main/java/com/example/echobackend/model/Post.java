package com.example.echobackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // Import this annotation

import java.time.LocalDateTime; // Use LocalDateTime for modern date/time handling

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "img")
    private String img;

    @CreationTimestamp // This will automatically set the creation timestamp
    @Column(name = "created_at", nullable = false, updatable = false) // Ensures non-null and not updatable after creation
    private LocalDateTime createdAt; // Changed from Timestamp to LocalDateTime

    // Correct Many-to-one relationship to User
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is good for performance
    @JoinColumn(name = "user_id", nullable = false) // This maps the 'user_id' foreign key column
    private User user; // Reference to the User entity

    // The 'private Long userId;' field is removed as it's managed by the 'user' object now.
}