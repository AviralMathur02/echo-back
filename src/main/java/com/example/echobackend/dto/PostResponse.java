package com.example.echobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Change this from java.sql.Timestamp

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String description;
    private String img;
    private LocalDateTime createdAt; // Changed to LocalDateTime
    private Long userId;          // This is fine, will be mapped from post.getUser().getId()
    private String name;          // From User
    private String profilePic;    // From User
    // private int likeCount;    // Keep as is if commented out
    // private int commentCount; // Keep as is if commented out
}