package com.travel.travelspot.travelspot;

public class tour_list {



    private String Country;
    private int Thumbnail;

    public tour_list(){}

    public tour_list(String country, int thumbnail) {
        Country = country;
        Thumbnail = thumbnail;
    }


    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public int getThumbnail() {
        return Thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        Thumbnail = thumbnail;
    }
}
