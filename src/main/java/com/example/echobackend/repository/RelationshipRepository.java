package com.example.echobackend.repository;

import com.example.echobackend.model.Relationship;
import com.example.echobackend.model.Relationship.RelationshipId;
import com.example.echobackend.model.User; // Keep if User is used for other methods in this repo, or remove if not
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query; // Removed @Query import
// import org.springframework.data.repository.query.Param; // Removed @Param import
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, RelationshipId> {

    List<Relationship> findByFollowerUserId(Long followerUserId);

    Optional<Relationship> findByFollowerUserIdAndFollowedUserId(Long followerUserId, Long followedUserId);

    boolean existsByFollowerUserIdAndFollowedUserId(Long followerUserId, Long followedUserId);

    void deleteByFollowerUserIdAndFollowedUserId(Long followerUserId, Long followedUserId);

    List<Relationship> findByFollowedUserId(Long followedUserId);

    long countByFollowedUserId(Long followedUserId);

    long countByFollowerUserId(Long followerUserId);

}
