package com.example.realtimechat.Model;

public class Chat {
    private String messges;
    private String recevier;
    private String sender;

    public Chat(String messges, String recevier, String sender) {
        this.messges = messges;
        this.recevier = recevier;
        this.sender = sender;
    }

    public Chat() {
    }

    public String getMessges() {
        return messges;
    }

    public void setMessges(String messges) {
        this.messges = messges;
    }

    public String getRecevier() {
        return recevier;
    }

    public void setRecevier(String recevier) {
        this.recevier = recevier;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
