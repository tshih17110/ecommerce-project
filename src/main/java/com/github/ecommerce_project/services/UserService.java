package com.github.ecommerce_project.services;

import com.github.ecommerce_project.dtos.user.UserRequestDto;
import com.github.ecommerce_project.dtos.user.UserResponseDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.UserMapper;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto registerUser(UserRequestDto newUserDto) {

        if (userRepository.existsByUsername(newUserDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(newUserDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User newUser = userMapper.toUser(newUserDto);
        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User ID not found."));

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        userMapper.updateUser(request, user);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new DataNotFoundException("User ID not found.");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new DataNotFoundException("User ID not found."));
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

}
