package com.energy.support.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Boolean success;
    private LocalDateTime timestamp;
    private String message;

    public ChatMessageResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessageResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
