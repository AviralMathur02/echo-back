package com.example.echobackend.service;

import com.example.echobackend.dto.AddPostRequest;
import com.example.echobackend.dto.PostResponse;
import com.example.echobackend.model.Post;
import com.example.echobackend.model.User;
import com.example.echobackend.repository.PostRepository;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Keep this import
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RelationshipService relationshipService; // Assuming this service exists and is correctly implemented

    public List<PostResponse> getPosts(Long targetUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not authenticated!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        List<Post> posts;
        if (targetUserId != null) {
            // Find posts by target User ID using the updated repository method
            posts = postRepository.findByUser_IdOrderByCreatedAtDesc(targetUserId);
        } else {
            List<Long> followedUserIds = relationshipService.getFollowedUserIds(currentUser.getId());

            // Include current user's ID in the list to also fetch their own posts
            followedUserIds.add(currentUser.getId());

            // Find posts by a list of User IDs using the updated repository method
            posts = postRepository.findByUser_IdInOrderByCreatedAtDesc(followedUserIds);
        }

        // Fetch all users involved in these posts to avoid N+1 queries
        Set<Long> userIdsInPosts = posts.stream()
                                    .map(post -> post.getUser().getId()) // Access ID via the User object
                                    .collect(Collectors.toSet());
        Map<Long, User> usersMap = userRepository.findAllById(userIdsInPosts)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Map Post entities to PostResponse DTOs, including user details
        return posts.stream().map(post -> {
            User postUser = usersMap.get(post.getUser().getId()); // Access ID via the User object
            return new PostResponse(
                post.getId(),
                post.getDescription(),
                post.getImg(),
                post.getCreatedAt(), // createdAt is now LocalDateTime, matches PostResponse
                post.getUser().getId(), // Access ID via the User object
                postUser != null ? postUser.getName() : null,
                postUser != null ? postUser.getProfilePic() : null
            );
        }).collect(Collectors.toList());
    }

    public String addPost(AddPostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Post newPost = new Post();
        newPost.setDescription(request.getDescription());
        newPost.setImg(request.getImg());
        // No need to set createdAt manually; @CreationTimestamp handles it automatically.

        newPost.setUser(currentUser); // Correctly set the User object (critical for foreign key)

        postRepository.save(newPost);
        return "Post has been created.";
    }

    public String deletePost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Post postToDelete = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found!"));

        // Compare user IDs using the User object from the Post
        if (!postToDelete.getUser().getId().equals(currentUser.getId())) { // Access ID via the User object
            throw new RuntimeException("You can delete only your post!");
        }

        postRepository.delete(postToDelete);
        return "Post has been deleted.";
    }
}