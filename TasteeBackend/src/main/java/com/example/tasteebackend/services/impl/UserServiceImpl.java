package com.example.tasteebackend.services.impl;

import com.example.tasteebackend.model.User;
import com.example.tasteebackend.model.enums.Role;
import com.example.tasteebackend.model.exceptions.InvalidArgumentsException;
import com.example.tasteebackend.model.exceptions.InvalidPasswordException;
import com.example.tasteebackend.model.exceptions.PasswordsDoNotMatchException;
import com.example.tasteebackend.model.exceptions.UserNotFoundException;
import com.example.tasteebackend.repository.UserRepository;
import com.example.tasteebackend.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(String username, String email, String password, String repeatPassword, String name, String surname, Role role) {
        if (username == null || password == null || email == null || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            throw new InvalidArgumentsException();
        }
        if (!password.equals(repeatPassword)) {
            throw new PasswordsDoNotMatchException();
        }
        if (!isValidPassword(password)) {
            throw new InvalidPasswordException("Password must be at least 8 characters long and contain: " + "at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        Role roleEnum = role;
        User user = new User(username, email, passwordEncoder.encode(password), name, surname, roleEnum);
        User savedUser = userRepository.save(user);
        System.out.println("Saved user with ID: " + savedUser.getId());
        System.out.println("Saved username: " + savedUser.getUsername());
        return savedUser;
    }

    private boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}