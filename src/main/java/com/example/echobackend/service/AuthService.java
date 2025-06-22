package com.example.echobackend.service;

import com.example.echobackend.model.User;
import com.example.echobackend.repository.UserRepository;
import com.example.echobackend.dto.RegisterRequest; // Assuming you have a RegisterRequest DTO
import com.example.echobackend.dto.LoginRequest;   // <--- CRITICAL FIX: ADD THIS IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Needed for login method
import org.springframework.security.core.AuthenticationException; // Needed for login method
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public String register(RegisterRequest request) {
        boolean usernameExists = userRepository.findByUsername(request.getUsername()).isPresent();
        boolean emailExists = userRepository.findByEmail(request.getEmail()).isPresent();

        if (usernameExists && emailExists) {
            throw new RuntimeException("Username and email already exists!");
        } else if (usernameExists) {
            throw new RuntimeException("Username already exists!");
        } else if (emailExists) {
            throw new RuntimeException("Email already exists!"); // Individual email message
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setCoverPic("defaultCover.jpg");
        user.setProfilePic("defaultProfile.jpg");
        user.setCity("Unknown");
        user.setWebsiteName("N/A");
        user.setWebsiteUrl("");
        userRepository.save(user);
        return "User registered successfully!";
    }
    public User login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            return userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found after successful authentication."));
        } catch (AuthenticationException e) {
            throw e;
        }
    }
}
