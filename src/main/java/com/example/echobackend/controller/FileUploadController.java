package com.example.echobackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.OPTIONS}, allowCredentials = "true") // NEW: Add this annotation for CORS
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("Please select a file to upload.", HttpStatus.BAD_REQUEST);
            }

            // Generate a unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename).toAbsolutePath().normalize();

            // Create directories if they don't exist
            Files.createDirectories(Paths.get(uploadDir));

            // Copy the file to the specified path
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return new ResponseEntity<>("File uploaded successfully. File path: " + filename, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Added error message
        } catch (Exception e) { // Catch any other unexpected exceptions
            e.printStackTrace();
            return new ResponseEntity<>("An unexpected error occurred during file upload: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}