package com.example.echobackend.service;

import com.example.echobackend.dto.AddCommentRequest;
import com.example.echobackend.dto.CommentResponse;
import com.example.echobackend.model.Comment;
import com.example.echobackend.model.User;
import com.example.echobackend.repository.CommentRepository;
import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        // Fetch all users involved in these comments to avoid N+1 queries
        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Map Comment entities to CommentResponse DTOs, including user details
        return comments.stream().map(comment -> {
            User commentUser = usersMap.get(comment.getUserId());
            return new CommentResponse(
                comment.getId(),
                comment.getDescription(),
                comment.getCreatedAt(),
                comment.getUserId(),
                comment.getPostId(),
                commentUser != null ? commentUser.getName() : null,
                commentUser != null ? commentUser.getProfilePic() : null
            );
        }).collect(Collectors.toList());
    }

    public String addComment(AddCommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not logged in!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Long currentUserId = currentUser.getId();

        Comment newComment = new Comment();
        newComment.setDescription(request.getDescription());
        newComment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        newComment.setUserId(currentUserId);
        newComment.setPostId(request.getPostId());

        commentRepository.save(newComment);
        return "Comment has been created.";
    }

    @Transactional // Required for deleteByIdAndUserId
    public String deleteComment(Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not authenticated!");
        }

        String authenticatedUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        Long currentUserId = currentUser.getId();

        // Check if the comment exists and belongs to the user
        boolean exists = commentRepository.findById(commentId)
                .map(comment -> comment.getUserId().equals(currentUserId))
                .orElse(false);

        if (!exists) {
            throw new RuntimeException("You can delete only your comment or comment not found!");
        }

        commentRepository.deleteByIdAndUserId(commentId, currentUserId);
        return "Comment has been deleted!";
    }
}