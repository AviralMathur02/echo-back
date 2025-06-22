package com.example.echobackend.service;

import com.example.echobackend.dto.LikeRequest;
import com.example.echobackend.model.Like;
import com.example.echobackend.model.User;
import com.example.echobackend.repository.LikeRepository;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public List<Long> getLikesForPost(Long postId) {
        return likeRepository.findByPostId(postId)
                .stream()
                .map(Like::getUserId)
                .collect(Collectors.toList());
    }

    public String addLike(LikeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Long currentUserId = currentUser.getId();

        if (likeRepository.findByUserIdAndPostId(currentUserId, request.getPostId()).isPresent()) {
            return "Post already liked."; // Or throw error
        }

        Like newLike = new Like(currentUserId, request.getPostId());
        likeRepository.save(newLike);
        return "Post has been liked.";
    }

    @Transactional
    public String deleteLike(Long postId) { // postId of the post to unlike
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Long currentUserId = currentUser.getId();

        if (!likeRepository.findByUserIdAndPostId(currentUserId, postId).isPresent()) {
            throw new RuntimeException("You have not liked this post.");
        }

        likeRepository.deleteByUserIdAndPostId(currentUserId, postId);
        return "Post has been disliked.";
    }
}