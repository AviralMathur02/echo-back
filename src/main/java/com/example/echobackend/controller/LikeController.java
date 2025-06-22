package com.example.echobackend.controller;

import com.example.echobackend.dto.LikeRequest;
import com.example.echobackend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes") // Maps to /api/likes
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping
    public ResponseEntity<?> getLikes(@RequestParam Long postId) {
        try {
            List<Long> likedUserIds = likeService.getLikesForPost(postId);
            return ResponseEntity.ok(likedUserIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching likes: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> addLike(@RequestBody LikeRequest request) {
        try {
            String message = likeService.addLike(request);
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding like.");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteLike(@RequestParam Long postId) { // postId to unlike
        try {
            String message = likeService.deleteLike(postId);
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting like.");
        }
    }
}