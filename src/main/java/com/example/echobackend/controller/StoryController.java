package com.example.echobackend.controller;

import com.example.echobackend.dto.AddStoryRequest;
import com.example.echobackend.dto.StoryResponse;
import com.example.echobackend.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories") // Maps to /api/stories
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @GetMapping
    public ResponseEntity<?> getStories() {
        try {
            List<StoryResponse> stories = storyService.getStories();
            return ResponseEntity.ok(stories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching stories: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> addStory(@RequestBody AddStoryRequest request) {
        try {
            String message = storyService.addStory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding story.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStory(@PathVariable("id") Long storyId) {
        try {
            String message = storyService.deleteStory(storyId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting story.");
        }
    }
}