package com.example.tasteebackend.dto;

import com.example.tasteebackend.model.enums.Role;
import lombok.Data;

@Data
public class RegistrationRequest {
    private String username;
    private String email;
    private String password;
    private String repeatPassword;
    private String name;
    private String surname;
    private Role role;
}