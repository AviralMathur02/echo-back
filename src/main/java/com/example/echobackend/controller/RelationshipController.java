package com.example.echobackend.controller;

import com.example.echobackend.model.User;
import com.example.echobackend.service.RelationshipService;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.echobackend.dto.UserDTO;
import com.example.echobackend.dto.RelationshipRequest;

import java.util.List;

@RestController
@RequestMapping("/api/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Boolean> checkFollowing(@RequestParam Long followedUserId) {
        try {
            Long currentUserId = getCurrentAuthenticatedUserId();
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }
            boolean isFollowing = relationshipService.isFollowing(currentUserId, followedUserId);
            return ResponseEntity.ok(isFollowing);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping
    public ResponseEntity<String> addRelationship(@RequestBody RelationshipRequest request) {
        try {
            Long followerUserId = getCurrentAuthenticatedUserId();
            if (followerUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }
            String message = relationshipService.addRelationship(followerUserId, request.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding relationship.");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteRelationship(@RequestBody RelationshipRequest request) {
        try {
            Long followerUserId = getCurrentAuthenticatedUserId();
            if (followerUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }
            String message = relationshipService.deleteRelationship(followerUserId, request.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting relationship.");
        }
    }

    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }

    @GetMapping("/followers/count")
    public ResponseEntity<Long> getFollowerCount(@RequestParam Long userId) {
        try {
            long count = relationshipService.getFollowerCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    @GetMapping("/following/count")
    public ResponseEntity<Long> getFollowingCount(@RequestParam Long userId) {
        try {
            long count = relationshipService.getFollowingCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    @GetMapping("/followers/list")
    public ResponseEntity<List<User>> getFollowersList(@RequestParam Long userId) {
        try {
            List<User> followers = relationshipService.getFollowersList(userId);
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/following/list")
    public ResponseEntity<List<User>> getFollowingList(@RequestParam Long userId) {
        try {
            List<User> following = relationshipService.getFollowingList(userId);
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/friends/list")
    public ResponseEntity<List<UserDTO>> getMutualFriendsList(@RequestParam Long userId) {
        try {
            Long currentUserId = getCurrentAuthenticatedUserId();
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
            }
            List<UserDTO> mutualFriends = relationshipService.getMutualFriendsList(userId);
            return ResponseEntity.ok(mutualFriends);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
}