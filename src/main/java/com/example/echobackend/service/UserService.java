package com.example.echobackend.service;

import com.example.echobackend.model.User;
import com.example.echobackend.model.Relationship;
import com.example.echobackend.repository.RelationshipRepository;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import com.example.echobackend.dto.UserDTO;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final RelationshipService relationshipService;

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        long followerCount = relationshipService.getFollowerCount(userId);
        long followingCount = relationshipService.getFollowingCount(userId);

        return new UserDTO(user, followerCount, followingCount);
    }

    @Transactional
    public void updateUser(User userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        if (!currentUser.getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not authorized to update this user's profile.");
        }

        currentUser.setName(userDetails.getName());
        currentUser.setCity(userDetails.getCity());
        currentUser.setProfilePic(userDetails.getProfilePic());
        currentUser.setCoverPic(userDetails.getCoverPic());
        currentUser.setWebsiteName(userDetails.getWebsiteName());
        currentUser.setWebsiteUrl(userDetails.getWebsiteUrl());

        userRepository.save(currentUser);
    }

    @Transactional
    public void deleteUser(Long userIdToDelete) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        if (!currentUser.getId().equals(userIdToDelete)) {
            throw new RuntimeException("You are not authorized to delete this user's profile.");
        }

        if (!userRepository.existsById(userIdToDelete)) {
            throw new RuntimeException("User with ID " + userIdToDelete + " not found.");
        }

        userRepository.deleteById(userIdToDelete);
    }

    public List<User> getSuggestions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return Collections.emptyList();
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Set<Long> followingIds = relationshipRepository.findByFollowerUserId(currentUser.getId())
                .stream()
                .map(Relationship::getFollowedUserId)
                .collect(Collectors.toSet());

        followingIds.add(currentUser.getId());

        return userRepository.findAllByIdNotIn(followingIds);
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}