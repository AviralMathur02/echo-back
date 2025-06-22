package com.example.echobackend.service;

import com.example.echobackend.model.Relationship;
import com.example.echobackend.model.User;
import com.example.echobackend.repository.RelationshipRepository;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set; // Import Set for collecting unique IDs
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;

    public List<Long> getFollowerUserIdsForUser(Long followedUserId) {
        List<Relationship> relationships = relationshipRepository.findByFollowedUserId(followedUserId);
        return relationships.stream()
                .map(Relationship::getFollowerUserId)
                .collect(Collectors.toList());
    }

    public List<Long> getFollowedUserIds(Long followerUserId) {
        return relationshipRepository.findByFollowerUserId(followerUserId)
                .stream()
                .map(Relationship::getFollowedUserId)
                .collect(Collectors.toList());
    }

    @Transactional
    public String addRelationship(Long followerUserId, Long followedUserId) {
        if (followerUserId == null) {
            throw new RuntimeException("Follower ID cannot be null.");
        }

        if (followerUserId.equals(followedUserId)) {
            throw new RuntimeException("Cannot follow yourself!");
        }

        if (relationshipRepository.existsByFollowerUserIdAndFollowedUserId(followerUserId, followedUserId)) {
            return "Already following.";
        }

        Relationship newRelationship = new Relationship(followerUserId, followedUserId);
        relationshipRepository.save(newRelationship);
        return "Following";
    }

    @Transactional
    public String deleteRelationship(Long followerUserId, Long followedUserId) {
        if (followerUserId == null) {
            throw new RuntimeException("Follower ID cannot be null.");
        }

        if (!relationshipRepository.existsByFollowerUserIdAndFollowedUserId(followerUserId, followedUserId)) {
            throw new RuntimeException("Not following this user.");
        }

        relationshipRepository.deleteByFollowerUserIdAndFollowedUserId(followerUserId, followedUserId);
        return "Unfollow";
    }

    public boolean isFollowing(Long currentUserId, Long followedUserId) {
        if (currentUserId == null) {
            return false;
        }
        return relationshipRepository.existsByFollowerUserIdAndFollowedUserId(currentUserId, followedUserId);
    }

    // --- MODIFIED SERVICE METHODS TO AVOID @Query IN REPOSITORY ---

    /**
     * Gets a list of User objects who are following a given user.
     * Uses derived query methods in two steps: get relationship IDs, then fetch users.
     * @param userId The ID of the user whose followers are to be listed.
     * @return A list of User objects (followers).
     */
    public List<User> getFollowersList(Long userId) {
        // Step 1: Get all relationships where the given userId is being followed
        List<Relationship> relationships = relationshipRepository.findByFollowedUserId(userId);

        // Step 2: Extract the follower user IDs
        Set<Long> followerUserIds = relationships.stream()
                .map(Relationship::getFollowerUserId)
                .collect(Collectors.toSet()); // Use Set to avoid duplicate user IDs if any

        // Step 3: Fetch the actual User objects using the collected IDs
        // Note: If followerUserIds is empty, findAllById will return an empty list, which is desired.
        return userRepository.findAllById(followerUserIds);
    }

    /**
     * Gets a list of User objects that a given user is following.
     * Uses derived query methods in two steps: get relationship IDs, then fetch users.
     * @param userId The ID of the user whose following list is to be retrieved.
     * @return A list of User objects (users being followed).
     */
    public List<User> getFollowingList(Long userId) {
        // Step 1: Get all relationships where the given userId is the follower
        List<Relationship> relationships = relationshipRepository.findByFollowerUserId(userId);

        // Step 2: Extract the followed user IDs
        Set<Long> followedUserIds = relationships.stream()
                .map(Relationship::getFollowedUserId)
                .collect(Collectors.toSet()); // Use Set to avoid duplicate user IDs

        // Step 3: Fetch the actual User objects using the collected IDs
        return userRepository.findAllById(followedUserIds);
    }

    // Existing count methods (no change)
    public long getFollowerCount(Long userId) {
        return relationshipRepository.countByFollowedUserId(userId);
    }

    public long getFollowingCount(Long userId) {
        return relationshipRepository.countByFollowerUserId(userId);
    }
}
