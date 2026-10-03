package com.example.tasteebackend.services;

import com.example.tasteebackend.model.User;
import com.example.tasteebackend.model.enums.Role;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User register(String username, String email, String password, String repeatPassword, String firstName, String lastName, Role role);
}