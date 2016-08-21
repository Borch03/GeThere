package pl.edu.agh.gethere.model;

import java.io.Serializable;

/**
 * Created by Dominik on 18.06.2016.
 */

@SuppressWarnings("serial")
public class Coordinates implements Serializable {

    private double latitude;
    private double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
