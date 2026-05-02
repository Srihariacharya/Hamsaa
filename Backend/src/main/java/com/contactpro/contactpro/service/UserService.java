package com.contactpro.contactpro.service;

import com.contactpro.contactpro.exception.InvalidCredentialsException;
import com.contactpro.contactpro.security.JwtUtils;
import org.springframework.stereotype.Service;
import com.contactpro.contactpro.repository.UserRepository;
import com.contactpro.contactpro.model.User;
import com.contactpro.contactpro.dto.UserRequest;
import com.contactpro.contactpro.dto.UserResponse;
import com.contactpro.contactpro.dto.ProfileRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.stream.Collectors;
import com.contactpro.contactpro.dto.LoginRequest;
import com.contactpro.contactpro.dto.LoginResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCompany()
        );
    }

    public UserResponse createUser(UserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getId());

        return new LoginResponse(
                "Login successful",
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getCompany()
        );
    }

    public UserResponse updateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getCompany() != null) {
            user.setCompany(request.getCompany());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Current password does not match");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}