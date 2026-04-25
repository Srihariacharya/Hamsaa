package com.contactpro.contactpro.dto;

public class GoogleContactDto {

    private String displayName;
    private String email;
    private String phone;

    public GoogleContactDto() {
    }

    public GoogleContactDto(String displayName, String email, String phone) {
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
