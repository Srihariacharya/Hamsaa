package com.contactpro.contactpro.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String error;
    private int status;
    private LocalDateTime timestamp;

    public ErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // getters

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}