package com.contactpro.contactpro.controller;

import org.springframework.web.bind.annotation.*;

import com.contactpro.contactpro.service.UserService;
import com.contactpro.contactpro.dto.LoginRequest;
import com.contactpro.contactpro.dto.LoginResponse;
import com.contactpro.contactpro.dto.UserRequest;
import com.contactpro.contactpro.dto.UserResponse;
import com.contactpro.contactpro.dto.ProfileRequest;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/profile/{userId}")
    public UserResponse getProfile(@PathVariable Long userId) {
        return userService.getUserProfile(userId);
    }

    @PutMapping("/profile/{userId}")
    public UserResponse updateProfile(@PathVariable Long userId, @RequestBody ProfileRequest request) {
        return userService.updateProfile(userId, request);
    }
}