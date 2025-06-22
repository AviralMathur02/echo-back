package com.example.echobackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailedResponse extends UserResponse {
    private boolean isFollowing; // True if the authenticated user follows this profile

    // Constructor to copy fields from UserResponse and set isFollowing
    public UserDetailedResponse(UserResponse userResponse, boolean isFollowing) {
        super(userResponse.getId(), userResponse.getUsername(), userResponse.getEmail(),
              userResponse.getName(), userResponse.getProfilePic(), userResponse.getCoverPic(),
              userResponse.getCity(), userResponse.getWebsiteName(), userResponse.getWebsiteUrl());

        this.isFollowing = isFollowing;
    }

    // Full constructor for convenience, though typically the above will be used with mapUserToUserResponse
    public UserDetailedResponse(Long id, String username, String email, String name, String profilePic, String coverPic, String city, String websiteName, String websiteUrl, boolean isFollowing) {
        super(id, username, email, name, profilePic, coverPic, city, websiteName, websiteUrl);
        this.isFollowing = isFollowing;
    }
}