package com.github.ecommerce_project.services;

import com.github.ecommerce_project.dtos.user.UserRegistrationDto;
import com.github.ecommerce_project.dtos.user.UserResponseDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.UserMapper;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.repositories.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;

    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto newUserDto) {

        if (userRepository.existsByUsername(newUserDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(newUserDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = userMapper.toUser(newUserDto);
        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new DataNotFoundException("User ID not found"));
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

}
