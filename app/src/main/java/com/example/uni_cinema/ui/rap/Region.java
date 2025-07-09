package com.example.uni_cinema.ui.rap;

public class Region {
    private String nameProvince;
    private String nameTheater;
    private String theaterId;
    private String addressTheater;

    public Region() {
    } // Firebase needs this

    // Dùng cho vùng (province only)
    public Region(String nameProvince) {
        this.nameProvince = nameProvince;
    }

    // Dùng cho rạp (đầy đủ thông tin)
    public Region(String nameProvince, String nameTheater, String theaterId, String addressTheater) {
        this.nameProvince = nameProvince;
        this.nameTheater = nameTheater;
        this.theaterId = theaterId;
        this.addressTheater = addressTheater;
    }

    public String getNameProvince() {
        return nameProvince;
    }

    public void setNameProvince(String nameProvince) {
        this.nameProvince = nameProvince;
    }

    public String getNameTheater() {
        return nameTheater;
    }

    public void setNameTheater(String nameTheater) {
        this.nameTheater = nameTheater;
    }

    public String getAddressTheater() {
        return addressTheater;
    }

    public void setAddressTheater(String addressTheater) {
        this.addressTheater = addressTheater;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(String theaterId) {
        this.theaterId = theaterId;
    }
}