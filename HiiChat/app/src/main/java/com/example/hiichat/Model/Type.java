package com.example.hiichat.Model;

public class Type {
    String type;
    String nameType;

    public Type(String type, String nameType) {
        this.type = type;
        this.nameType = nameType;
    }

    public Type() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameType() {
        return nameType;
    }

    public void setNameType(String nameType) {
        this.nameType = nameType;
    }
}
