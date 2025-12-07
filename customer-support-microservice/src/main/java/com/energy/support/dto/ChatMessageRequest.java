package com.energy.support.dto;

public class ChatMessageRequest {
    private String fromUserId;
    private String toUserId;
    private String message;
    private Boolean isAdmin;

    public ChatMessageRequest() {}

    public ChatMessageRequest(String fromUserId, String toUserId, String message, Boolean isAdmin) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.message = message;
        this.isAdmin = isAdmin;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
