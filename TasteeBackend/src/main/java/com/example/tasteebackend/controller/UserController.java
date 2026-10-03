package com.example.tasteebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.multipart.MultipartFile;
import com.example.tasteebackend.dto.UpdateUserRequest;
import com.example.tasteebackend.dto.UserDto;
import com.example.tasteebackend.model.User;
import com.example.tasteebackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getProfilePicture(),
                user.getRole().name()
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            @RequestBody UpdateUserRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setSurname(request.surname());

        User savedUser = userRepository.save(user);

        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                savedUser,
                null,
                savedUser.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
        }

        return ResponseEntity.ok(new UserDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getSurname(),
                savedUser.getProfilePicture(),
                savedUser.getRole().name()
        ));
    }

    @PutMapping("/me/profile-picture")
    public ResponseEntity<UserDto> updateProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;
            Path uploadPath = Paths.get("uploads/profile-pictures");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/images/profile-pictures/" + fileName;
            user.setProfilePicture(imageUrl);
            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(new UserDto(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getName(),
                    savedUser.getSurname(),
                    savedUser.getProfilePicture(),
                    savedUser.getRole().name()
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}