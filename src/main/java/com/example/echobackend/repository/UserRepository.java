package com.example.echobackend.repository;

import com.example.echobackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set; // Import Set for the new method

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String usernameQuery, String nameQuery);
    List<User> findAllByIdNotIn(Set<Long> userIdsToExclude);
}