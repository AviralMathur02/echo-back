package com.example.echobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponse {
    private Long id;
    private String img;
    private Timestamp createdAt;
    private Long userId;
    private String name; // From User
}