package com.example.realtimechat.Model;

public class GroupChatModel {

    private String message,type,sender,timestamp;

    public GroupChatModel(String message, String type, String sender, String timestamp) {
        this.message = message;
        this.type = type;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public GroupChatModel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
