package com.hackervenom.mychatapp;

public class Messages {
    private String from , message, type;

    public Messages()
    {

    }

    public Messages(String from, String msg, String type) {
        this.from = from;
        this.message = msg;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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
}
