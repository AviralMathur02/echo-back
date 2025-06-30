package com.example.echobackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Like.LikeId.class) // IMPORTANT: Reference the nested class correctly
public class Like {

    @Id
    @Column(nullable = false)
    private Long userId;

    @Id
    @Column(nullable = false)
    private Long postId;

    // --- NESTED COMPOSITE PRIMARY KEY CLASS ---
    @Embeddable // Mark as embeddable for composite keys
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeId implements Serializable { // IMPORTANT: Add 'public static'
        private Long userId;
        private Long postId;
    }
}