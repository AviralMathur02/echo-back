package com.example.echobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String description;
    private Timestamp createdAt;
    private Long userId;
    private Long postId;
    private String name; // From User
    private String profilePic; // From User
}