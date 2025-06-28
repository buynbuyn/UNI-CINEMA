package com.example.uni_cinema.ui.rap;

public class Region {
    public String nameProvince;
    public String nameTheater;

    public Region() {} // Required for Firebase

    public Region(String nameProvince, String nameTheater) {
        this.nameProvince = nameProvince;
        this.nameTheater = nameTheater;
    }

}
