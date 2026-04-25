package com.contactpro.contactpro.controller;


import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.contactpro.contactpro.dto.UserRequest;
import com.contactpro.contactpro.dto.UserResponse;
import com.contactpro.contactpro.service.UserService;

/*
 * @RestController handles HTTP requests.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /*
     * Constructor injection
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /*
     * POST /api/users
     * Creates new user
     */
    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    /*
     * GET /api/users
     * Returns list of users
     */
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}
