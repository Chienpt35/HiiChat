package com.example.hiichat.Notification;

import com.example.hiichat.util.TypeNotification;

public class Data {
    private String user;
    private int icon;
    private String body;
    private String title;
    private String sender;
    private String senderName;
    private String idRoom;
    private TypeNotification type;

    public Data(String user, int icon, String body, String title, String sender, String senderName, String idRoom, TypeNotification type) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sender = sender;
        this.senderName = senderName;
        this.idRoom = idRoom;
        this.type = type;
    }

    public Data() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public TypeNotification getType() {
        return type;
    }

    public void setType(TypeNotification type) {
        this.type = type;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(String idRoom) {
        this.idRoom = idRoom;
    }
}
