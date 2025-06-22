// src/main/java/com/example/echobackend/dto/RelationshipRequest.java
package com.example.echobackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipRequest {
    private Long userId; // The ID of the user to follow/unfollow
}