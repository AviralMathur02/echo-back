// com.example.echobackend.service.RelationshipService.java

package com.example.echobackend.service;

import com.example.echobackend.model.Relationship;
import com.example.echobackend.model.User;
import com.example.echobackend.repository.RelationshipRepository;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet; // NEW: Import HashSet for set operations

// Import for UserDTO to return a list of UserDTOs
import com.example.echobackend.dto.UserDTO;

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

    /**
     * Gets a list of User objects who are following a given user.
     * @param userId The ID of the user whose followers are to be listed.
     * @return A list of User objects (followers).
     */
    public List<User> getFollowersList(Long userId) {
        List<Relationship> relationships = relationshipRepository.findByFollowedUserId(userId);
        Set<Long> followerUserIds = relationships.stream()
                .map(Relationship::getFollowerUserId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(followerUserIds);
    }

    /**
     * Gets a list of User objects that a given user is following.
     * @param userId The ID of the user whose following list is to be retrieved.
     * @return A list of User objects (users being followed).
     */
    public List<User> getFollowingList(Long userId) {
        List<Relationship> relationships = relationshipRepository.findByFollowerUserId(userId);
        Set<Long> followedUserIds = relationships.stream()
                .map(Relationship::getFollowedUserId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(followedUserIds);
    }

    // MODIFIED: Method to get a list of mutual friends without @Query
    /**
     * Gets a list of UserDTOs who are mutual followers (friends) with the given user.
     * This method retrieves followers and following separately and finds their intersection.
     * @param currentUserId The ID of the user for whom to find mutual friends.
     * @return A list of UserDTOs representing mutual friends.
     */
    public List<UserDTO> getMutualFriendsList(Long currentUserId) {
        if (currentUserId == null) {
            throw new IllegalArgumentException("User ID cannot be null for finding mutual friends.");
        }

        // 1. Get IDs of users the current user is following
        Set<Long> currentUserFollowingIds = relationshipRepository.findByFollowerUserId(currentUserId)
                .stream()
                .map(Relationship::getFollowedUserId)
                .collect(Collectors.toSet());

        // 2. Get IDs of users who are following the current user
        Set<Long> currentUserFollowerIds = relationshipRepository.findByFollowedUserId(currentUserId)
                .stream()
                .map(Relationship::getFollowerUserId)
                .collect(Collectors.toSet());

        // 3. Find the intersection of these two sets (mutual friends)
        Set<Long> mutualFriendIds = new HashSet<>(currentUserFollowingIds); // Start with one set
        mutualFriendIds.retainAll(currentUserFollowerIds); // Retain only elements also in the other set

        // 4. Fetch the User entities for the mutual friend IDs
        List<User> mutualFriends = userRepository.findAllById(mutualFriendIds);

        // 5. Convert User entities to UserDTOs
        return mutualFriends.stream()
                            .map(user -> {
                                // Re-use existing service methods for counts if needed in DTO
                                long followerCount = getFollowerCount(user.getId());
                                long followingCount = getFollowingCount(user.getId());
                                return new UserDTO(user, followerCount, followingCount);
                            })
                            .collect(Collectors.toList());
    }
    public long getFollowerCount(Long userId) {
        return relationshipRepository.countByFollowedUserId(userId);
    }

    public long getFollowingCount(Long userId) {
        return relationshipRepository.countByFollowerUserId(userId);
    }
}
