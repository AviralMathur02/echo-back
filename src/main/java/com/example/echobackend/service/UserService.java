// server/src/main/java/com/example/echobackend/service/UserService.java
package com.example.echobackend.service;

import com.example.echobackend.model.User;
import com.example.echobackend.model.Relationship; // Explicitly ensure this import is present
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
import java.util.Collections; // Import for Collections.emptyList()
import com.example.echobackend.dto.UserDTO; // Import the UserDTO

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final RelationshipService relationshipService; // Inject RelationshipService for count methods

    // MODIFIED: Get User by ID to return UserDTO with counts
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        long followerCount = relationshipService.getFollowerCount(userId);
        long followingCount = relationshipService.getFollowingCount(userId);

        return new UserDTO(user, followerCount, followingCount);
    }

    // Existing: Update User
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

    // Existing: Delete User Method
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

        // --- IMPORTANT: Implement Cascading Deletion of Dependent Data ---
        // As discussed, ensure all related data is deleted first.
        // For example, delete relationships where this user is follower or followed:
        // relationshipRepository.deleteByFollowerUserId(userIdToDelete);
        // relationshipRepository.deleteByFollowedUserId(userIdToDelete);
        // And posts, likes, comments etc. related to this user.
        // ------------------------------------------------------------------

        userRepository.deleteById(userIdToDelete);
    }

    // Existing: Get Suggestions Method (Now uses Collections.emptyList() for Java 8 compatibility)
    public List<User> getSuggestions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return Collections.emptyList(); // FIX: Changed List.of() to Collections.emptyList()
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Set<Long> followingIds = relationshipRepository.findByFollowerUserId(currentUser.getId())
                .stream()
                .map(Relationship::getFollowedUserId) // Uses the getter generated by Lombok on Relationship.java
                .collect(Collectors.toSet());

        followingIds.add(currentUser.getId()); // Exclude the current user themselves

        return userRepository.findAllByIdNotIn(followingIds);
    }

    /**
     * Searches for users by matching a query against their username or name (case-insensitive).
     *
     * @param query The search string.
     * @return A list of matching User objects.
     */
    // NEW: Search Users Method (Uses Collections.emptyList() for Java 8 compatibility)
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList(); // FIX: Changed List.of() to Collections.emptyList()
        }
        // Use the existing UserRepository method for case-insensitive search
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    // --- NEW: Methods for checking username and email existence ---

    /**
     * Checks if a username is already taken in the database.
     * @param username The username to check.
     * @return true if the username is taken, false otherwise.
     */
    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Checks if an email is already taken in the database.
     * @param email The email to check.
     * @return true if the email is taken, false otherwise.
     */
    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    // --- END NEW ---
}
