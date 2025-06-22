package com.example.echobackend.service;

import com.example.echobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // Mark this as a Spring service
@RequiredArgsConstructor // Lombok: Injects final fields via constructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository; // Inject UserRepository here

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username from the repository
        // If not found, throw UsernameNotFoundException
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}