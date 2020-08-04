package com.example.hiichat.Notification;

public class FCMModel {
    public String title;
    public String message;
    public String to;
    public String data;
    public String type;

    public static FCMModel init() {
        return new FCMModel();
    }

    public FCMModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public FCMModel setMessage(String message) {
        this.message = message;
        return this;
    }

    public FCMModel setTo(String to) {
        this.to = to;
        return this;
    }

    public FCMModel setData(String data) {
        this.data = data;
        return this;
    }

    public FCMModel setType(String type) {
        this.type = type;
        return this;
    }
}
