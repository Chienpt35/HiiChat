package com.example.hiichat.Model;

public class User {
    public String id;
    public String name;
    public String email;
    public String avata;
    public String gioiTinh;
    public String tuoi;
    public Status status;
    public Message message;
    public Double valueLong;
    public Double valueLat;

    public User(Double valueLong, Double valueLat) {
        this.valueLong = valueLong;
        this.valueLat = valueLat;
    }



    public User(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvata() {
        return avata;
    }

    public void setAvata(String avata) {
        this.avata = avata;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getTuoi() {
        return tuoi;
    }

    public void setTuoi(String tuoi) {
        this.tuoi = tuoi;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Double getValueLong() {
        return valueLong;
    }

    public void setValueLong(Double valueLong) {
        this.valueLong = valueLong;
    }

    public Double getValueLat() {
        return valueLat;
    }

    public void setValueLat(Double valueLat) {
        this.valueLat = valueLat;
    }
}
