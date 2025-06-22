package com.example.echobackend.repository;

import com.example.echobackend.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds);
    void deleteByIdAndUserId(Long id, Long userId);
}