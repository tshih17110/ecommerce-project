package com.github.ecommerce_project.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import com.github.ecommerce_project.dtos.auth.AuthResponseDto;
import com.github.ecommerce_project.dtos.auth.LoginRequestDto;
import com.github.ecommerce_project.dtos.user.UserRequestDto;
import com.github.ecommerce_project.dtos.user.UserResponseDto;
import com.github.ecommerce_project.exceptions.DataNotFoundException;
import com.github.ecommerce_project.mapper.UserMapper;
import com.github.ecommerce_project.models.User;
import com.github.ecommerce_project.repositories.UserRepository;
import com.github.ecommerce_project.utils.JwtUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDto request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        request = UserRequestDto.builder()
                .username("testuser")
                .password("password")
                .firstname("John")
                .lastname("Doe")
                .email("test@example.com")
                .build();

    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("Throws when username already exists")
        void registerUser_shouldThrow_whenUsernameExists() {
            // when(userRepository.findById(1L)).thenReturn(Optional.empty());
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws when email already exists")
        void registerUser_shouldThrow_whenEmailExists() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Saves and returns DTO when success")
        void registerUser_shouldReturnDto_whenSuccess() {

            UserResponseDto expectedDto = UserResponseDto.builder()
                    .id(1L)
                    .username("testuser")
                    .email("test@example.com")
                    .build();

            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userMapper.toUser(request)).thenReturn(user);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserResponseDto result = userService.registerUser(request);

            assertEquals(expectedDto.getUsername(), result.getUsername());
            assertEquals(expectedDto.getEmail(), result.getEmail());
            verify(userRepository).save(any(User.class));

        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Returns token and user DTO when valid")
        void login_shouldReturnTokenAndDto_whenValid() {

            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .username("testuser")
                    .password("password")
                    .build();

            UserResponseDto userResponseDto = UserResponseDto.builder()
                    .id(1L)
                    .username("testuser")
                    .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(jwtUtils.generateToken(user)).thenReturn("mockToken");
            when(userMapper.toDto(user)).thenReturn(userResponseDto);

            AuthResponseDto result = userService.login(loginRequest);

            assertEquals("mockToken", result.getToken());
            assertEquals("testuser", result.getUser().getUsername());
            verify(authenticationManager).authenticate(any());
        }

    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("Throws when user ID not found")
        void updateUser_shouldThrow_whenUserIdNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(DataNotFoundException.class, () -> userService.updateUser(1L, request));
        }

        @Test
        @DisplayName("Throws when new username is already taken")
        void updateUser_shouldThrow_whenNewUsernameAlreadyTaken() {
            user = User.builder()
                    .id(1L)
                    .username("oldusername")
                    .build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws when new email is already taken")
        void updateUser_shouldThrow_whenNewEmailAlreadyTaken() {
            user = User.builder()
                    .id(1L)
                    .username("oldusername")
                    .email("oldemail@example.com")
                    .build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Success when username and email is unchanged")
        void updateUser_shouldSucceed_whenUsernameAndEmailUnchanged() {
            user = User.builder()
                    .id(1L)
                    .username("testuser")
                    .email("test@example.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(new UserResponseDto());

            assertDoesNotThrow(() -> userService.updateUser(1L, request));
            verify(userRepository, never()).existsByUsername(any());
            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository).save(any());
        }

        @Test
        @DisplayName("Saves and returns updated DTO on successful update")
        void updateUser_shouldReturnDto_whenSuccessfulUpdate() {
            UserRequestDto updateRequest = UserRequestDto.builder()
                    .username("newusername")
                    .password("password")
                    .firstname("John")
                    .lastname("Doe")
                    .email("newemail@example.com")
                    .build();

            UserResponseDto expectedDto = UserResponseDto.builder()
                    .id(1L)
                    .username("newusername")
                    .email("newemail@example.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByUsername("newusername")).thenReturn(false);
            when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserResponseDto result = userService.updateUser(1L, updateRequest);

            assertEquals("newusername", result.getUsername());
            assertEquals("newemail@example.com", result.getEmail());
            verify(userRepository).save(any());
        }

    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

    }

}
