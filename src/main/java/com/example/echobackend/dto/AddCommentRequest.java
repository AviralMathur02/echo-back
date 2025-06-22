package com.example.echobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    private String description; // Matches the 'desc' in your Node.js code
    private Long postId;
}