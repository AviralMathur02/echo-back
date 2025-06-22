package com.example.echobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String name;
    private String city;
    // private String website; // <--- REMOVE OR COMMENT OUT THIS LINE
    private String profilePic;
    private String coverPic;

    // --- NEW FIELDS ---
    private String websiteName; // Matches frontend 'websiteName'
    private String websiteUrl;  // Matches frontend 'websiteUrl'
}