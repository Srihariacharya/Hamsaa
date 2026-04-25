package com.contactpro.contactpro.dto;

/*
 * This DTO is used when client sends data to register.
 * We don't expose database entity directly.
 */
public class UserRequest {

    private String name;
    private String email;
    private String password;

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}