package com.example.echobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String coverPic;
    private String profilePic;
    private String city;
    // --- MODIFIED: Replaced 'website' with 'websiteName' and 'websiteUrl' ---
    private String websiteName;
    private String websiteUrl;
    // Password is intentionally excluded
}