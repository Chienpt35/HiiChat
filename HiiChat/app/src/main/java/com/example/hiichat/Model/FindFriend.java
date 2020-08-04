package com.example.hiichat.Model;

public class FindFriend {
    public String name_fF;
    public String yearOld_ff;
    public String sex_ff;
    public String range_ff;
    public String avatar_ff;

    public String getName_fF() {
        return name_fF;
    }

    public void setName_fF(String name_fF) {
        this.name_fF = name_fF;
    }

    public String getYearOld_ff() {
        return yearOld_ff;
    }

    public void setYearOld_ff(String yearOld_ff) {
        this.yearOld_ff = yearOld_ff;
    }

    public String getSex_ff() {
        return sex_ff;
    }

    public void setSex_ff(String sex_ff) {
        this.sex_ff = sex_ff;
    }

    public String getRange_ff() {
        return range_ff;
    }

    public void setRange_ff(String range_ff) {
        this.range_ff = range_ff;
    }

    public String getAvatar_ff() {
        return avatar_ff;
    }

    public void setAvatar_ff(String avatar_ff) {
        this.avatar_ff = avatar_ff;
    }

    public FindFriend(String name_fF, String yearOld_ff, String sex_ff, String range_ff, String avatar_ff) {
        this.name_fF = name_fF;
        this.yearOld_ff = yearOld_ff;
        this.sex_ff = sex_ff;
        this.range_ff = range_ff;
        this.avatar_ff = avatar_ff;
    }
}
