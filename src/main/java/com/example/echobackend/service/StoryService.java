package com.example.echobackend.service;

import com.example.echobackend.dto.AddStoryRequest;
import com.example.echobackend.dto.StoryResponse;
import com.example.echobackend.model.Story;
import com.example.echobackend.model.User;
import com.example.echobackend.repository.StoryRepository;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final RelationshipService relationshipService; // Inject RelationshipService

    public List<StoryResponse> getStories() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Long currentUserId = currentUser.getId();

        // Get followed user IDs
        List<Long> followedUserIds = relationshipService.getFollowedUserIds(currentUserId);
        
        // Also include the current user's ID to fetch their own stories
        List<Long> userIdsToFetch = new ArrayList<>(followedUserIds);
        userIdsToFetch.add(currentUserId);

        // Fetch stories from current user and followed users, limit to 4
        // Note: JPA find methods don't directly support LIMIT. We fetch all and then limit in Java.
        List<Story> stories = storyRepository.findByUserIdInOrderByCreatedAtDesc(userIdsToFetch);
        // Apply limit 4 as per Node.js logic
        List<Story> limitedStories = stories.stream().limit(4).collect(Collectors.toList());

        // Fetch user details for the stories
        Set<Long> storyUserIds = limitedStories.stream().map(Story::getUserId).collect(Collectors.toSet());
        Map<Long, User> usersMap = userRepository.findAllById(storyUserIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Map Story entities to StoryResponse DTOs, including user name
        return limitedStories.stream().map(story -> {
            User storyUser = usersMap.get(story.getUserId());
            return new StoryResponse(
                story.getId(),
                story.getImg(),
                story.getCreatedAt(),
                story.getUserId(),
                storyUser != null ? storyUser.getName() : null
            );
        }).collect(Collectors.toList());
    }

    public String addStory(AddStoryRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Long currentUserId = currentUser.getId();

        Story newStory = new Story();
        newStory.setImg(request.getImg());
        newStory.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        newStory.setUserId(currentUserId);

        storyRepository.save(newStory);
        return "Story has been created.";
    }

    @Transactional
    public String deleteStory(Long storyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Long currentUserId = currentUser.getId();

        // Find the story and verify ownership
        boolean existsAndOwned = storyRepository.findById(storyId)
                .map(story -> story.getUserId().equals(currentUserId))
                .orElse(false);

        if (!existsAndOwned) {
            throw new RuntimeException("You can delete only your story or story not found!");
        }

        storyRepository.deleteByIdAndUserId(storyId, currentUserId);
        return "Story has been deleted.";
    }
}