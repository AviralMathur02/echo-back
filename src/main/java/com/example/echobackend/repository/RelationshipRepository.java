// com.example.echobackend.repository.RelationshipRepository.java

package com.example.echobackend.repository;

import com.example.echobackend.model.Relationship;
import com.example.echobackend.model.Relationship.RelationshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
 // Added for clarity in derived methods (though not strictly needed by JPA method names)

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, RelationshipId> {

    List<Relationship> findByFollowerUserId(Long followerUserId);

    Optional<Relationship> findByFollowerUserIdAndFollowedUserId(Long followerUserId, Long followedUserId);

    boolean existsByFollowerUserIdAndFollowedUserId(Long followerUserId, Long followedUserId);

    void deleteByFollowerUserIdAndFollowedUserId(Long followerUserId, Long followedUserId);

    List<Relationship> findByFollowedUserId(Long followedUserId);

    long countByFollowedUserId(Long followedUserId);

    long countByFollowerUserId(Long followerUserId);

    // REMOVED: The @Query method for findMutualFollowersByUserId

    // You could potentially add derived methods here if needed for direct fetching
    // Example (not strictly needed for mutual friends, as service will handle intersection):
}
