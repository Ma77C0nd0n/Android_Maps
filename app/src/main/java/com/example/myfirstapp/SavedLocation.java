package com.example.myfirstapp;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Objects;

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

    public void setName(String name){
        this.name = name;
    }

    @Override
    public boolean equals(Object s){
        if (s != null && s instanceof SavedLocation){
            return ((lat == ((SavedLocation) s).lat) && (lng == ((SavedLocation) s).lng));
        }
        return false;
    }

    public double getLat() {
        return lat;
    }

    @Override
    public String toString() {
        return name;
    }

}
