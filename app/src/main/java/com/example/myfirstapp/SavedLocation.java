package com.example.myfirstapp;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by evin_ on 27/11/2016.
 */

public class SavedLocation implements Serializable {
    private String name;
    double lat, lng;

    SavedLocation(LatLng point, String name) {
        lat = point.latitude;
        lng = point.longitude;
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    @Override
    public String toString() {
        return name;
    }

}
