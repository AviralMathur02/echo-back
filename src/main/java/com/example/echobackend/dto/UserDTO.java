package com.example.echobackend.dto;

import com.example.echobackend.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
// Remove lombok.EqualsAndHashCode and lombok.experimental.SuperBuilder imports
// since we are no longer extending User directly or using @SuperBuilder.

@Data // Generates getters, setters, toString, equals, hashCode for UserDTO's own fields
@NoArgsConstructor // Generates a no-argument constructor for UserDTO
public class UserDTO { // NO LONGER extends User directly

    // Include fields from User that you want to expose/transfer
    private Long id;
    private String username;
    private String email;
    // private String password; // Recommended: DO NOT expose password, even hash, in DTOs for security
    private String name;
    private String coverPic;
    private String profilePic;
    private String city;
    private String websiteName;
    private String websiteUrl;

    // Additional fields for counts
    private long followerCount;
    private long followingCount;

    // Constructor to convert from User entity and add counts
    public UserDTO(User user, long followerCount, long followingCount) {
        // Copy properties from the User entity
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        // this.password = user.getPassword(); // REMOVE THIS LINE IF YOU DON'T WANT TO EXPOSE PASSWORD
        this.name = user.getName();
        this.coverPic = user.getCoverPic();
        this.profilePic = user.getProfilePic();
        this.city = user.getCity();
        this.websiteName = user.getWebsiteName();
        this.websiteUrl = user.getWebsiteUrl();

        // Set the additional counts
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }
}