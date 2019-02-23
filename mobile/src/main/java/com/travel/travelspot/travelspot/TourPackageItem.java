package com.travel.travelspot.travelspot;

public class TourPackageItem {

    //save tour name
    private String tourName;

    //save tour name image resource id
    private int tourImageId;

    //save tour name description
    private String tourNameDesc;

    public TourPackageItem(String tourName, int tourImageId, String tourNameDesc) {
        this.tourName = tourName;
        this.tourImageId = tourImageId;
        this.tourNameDesc = tourNameDesc;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public int getTourImageId() {
        return tourImageId;
    }

    public void setTourImageId(int tourImageId) {
        this.tourImageId = tourImageId;
    }

    public String getTourNameDesc() {
        return tourNameDesc;
    }

    public void setTourNameDesc(String tourNameDesc) {
        this.tourNameDesc = tourNameDesc;
    }
}
