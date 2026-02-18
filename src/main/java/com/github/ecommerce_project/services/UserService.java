package com.github.ecommerce_project.services;

import com.github.ecommerce_project.dtos.UserRegistrationDTO;
import com.github.ecommerce_project.dtos.UserResponseDTO;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.repositories.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private UserRepository userRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO registerUser(UserRegistrationDTO newUserDTO) {
        if (userRepository.existsByUsername(newUserDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(newUserDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        String hashed = passwordEncoder.encode(newUserDTO.getPassword());
        User newUser = User.builder()
                .username(newUserDTO.getUsername())
                .email(newUserDTO.getEmail())
                .firstname(newUserDTO.getFirstname())
                .lastname(newUserDTO.getLastname())
                .password(hashed)
                .build();

        userRepository.save(newUser);

        return UserResponseDTO.builder()
                .id(newUser.getId())
                .username(newUser.getUsername())
                .email(newUser.getEmail())
                .firstname(newUser.getFirstname())
                .lastname(newUser.getLastname())
                .build();
    }

}
