package com.contactpro.contactpro.dto;

public class ContactRequest {
    private String name;
    private String phone;
    private String email;
    private String category;
    private String notes;
    private String gender;
    private String dob;
    private int followUpFrequency;
    private Long userId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public int getFollowUpFrequency() { return followUpFrequency; }
    public void setFollowUpFrequency(int followUpFrequency) { this.followUpFrequency = followUpFrequency; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}