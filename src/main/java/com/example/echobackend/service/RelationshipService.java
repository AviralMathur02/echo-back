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
import java.util.HashSet;

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

    public List<User> getFollowersList(Long userId) {
        List<Relationship> relationships = relationshipRepository.findByFollowedUserId(userId);
        Set<Long> followerUserIds = relationships.stream()
                .map(Relationship::getFollowerUserId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(followerUserIds);
    }

    public List<User> getFollowingList(Long userId) {
        List<Relationship> relationships = relationshipRepository.findByFollowerUserId(userId);
        Set<Long> followedUserIds = relationships.stream()
                .map(Relationship::getFollowedUserId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(followedUserIds);
    }

    public List<UserDTO> getMutualFriendsList(Long currentUserId) {
        if (currentUserId == null) {
            throw new IllegalArgumentException("User ID cannot be null for finding mutual friends.");
        }

        Set<Long> currentUserFollowingIds = relationshipRepository.findByFollowerUserId(currentUserId)
                .stream()
                .map(Relationship::getFollowedUserId)
                .collect(Collectors.toSet());

        Set<Long> currentUserFollowerIds = relationshipRepository.findByFollowedUserId(currentUserId)
                .stream()
                .map(Relationship::getFollowerUserId)
                .collect(Collectors.toSet());

        Set<Long> mutualFriendIds = new HashSet<>(currentUserFollowingIds);
        mutualFriendIds.retainAll(currentUserFollowerIds);

        List<User> mutualFriends = userRepository.findAllById(mutualFriendIds);

        return mutualFriends.stream()
                .map(user -> {
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