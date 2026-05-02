package com.contactpro.contactpro.dto;

public class LoginResponse {

    private String message;
    private String token;
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private String company;

    public LoginResponse(String message, String token, Long userId, String email, String name, String phone, String company) {
        this.message = message;
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.company = company;
    }

    public String getMessage() { return message; }
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCompany() { return company; }
}