package com.example.echobackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor; // Keep AllArgsConstructor for other uses
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "relationships",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"follower_user_id", "followed_user_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor // This will generate a constructor with all fields (followerUserId, followedUserId, followerUser, followedUser)
@IdClass(Relationship.RelationshipId.class)
public class Relationship {

    @Id
    @Column(name = "follower_user_id", nullable = false)
    private Long followerUserId;

    @Id
    @Column(name = "followed_user_id", nullable = false)
    private Long followedUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User followerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User followedUser;

    // --- NEW: Custom constructor for creating relationships with just IDs ---
    public Relationship(Long followerUserId, Long followedUserId) {
        this.followerUserId = followerUserId;
        this.followedUserId = followedUserId;
        // followerUser and followedUser will be null initially, which is fine as JPA will manage them via IDs
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipId implements Serializable {
        private Long followerUserId;
        private Long followedUserId;
    }
}