package com.example.echobackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailedResponse extends UserResponse {
    private boolean isFollowing;

    public UserDetailedResponse(UserResponse userResponse, boolean isFollowing) {
        super(userResponse.getId(), userResponse.getUsername(), userResponse.getEmail(),
              userResponse.getName(), userResponse.getProfilePic(), userResponse.getCoverPic(),
              userResponse.getCity(), userResponse.getWebsiteName(), userResponse.getWebsiteUrl());
        this.isFollowing = isFollowing;
    }

    public UserDetailedResponse(Long id, String username, String email, String name, String profilePic, String coverPic, String city, String websiteName, String websiteUrl, boolean isFollowing) {
        super(id, username, email, name, profilePic, coverPic, city, websiteName, websiteUrl);
        this.isFollowing = isFollowing;
    }
}