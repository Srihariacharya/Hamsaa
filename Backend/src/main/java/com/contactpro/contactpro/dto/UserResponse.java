package com.contactpro.contactpro.dto;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;

    public UserResponse(Long id, String name, String email, String phone, String company) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCompany() { return company; }
}