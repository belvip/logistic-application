package com.example.demo.exceptions;

import java.time.LocalDateTime;
import java.util.Map;


public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Map<String, String> messages;

    public ErrorResponse() {
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String error, Map<String, String> messages) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.messages = messages;
    }

    // Getters and setters

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }
}