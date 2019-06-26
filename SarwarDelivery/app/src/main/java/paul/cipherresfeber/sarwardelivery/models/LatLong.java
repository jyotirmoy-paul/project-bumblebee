package paul.cipherresfeber.sarwardelivery.models;

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

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
