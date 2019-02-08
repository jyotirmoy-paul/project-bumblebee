package com.android.mr_paul.sarwar.UtilityPackage;

import java.io.Serializable;

public class LatLong implements Serializable {

    public double latitude;
    public double longitude;

    public LatLong(){

    }

    public LatLong(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
