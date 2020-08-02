package com.example.hiichat.Model;

public class mLocation {
    public Double valueLong;
    public Double valueLat;


    public mLocation() {
        //mac dinh firebase khi nhan data
    }


    public mLocation (Double valueLong, Double valueLat) {
        this.valueLong = valueLong;
        this.valueLat = valueLat;
    }
}
