package com.example.myfirstapp;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by evin_ on 27/11/2016.
 *
 * This class models a saved location with fields for a latitude and longitude taken from a LatLng, and then a string name whose
 * default is an address string retrieved from a geocoder.
 */
public class SavedLocation implements Serializable {
    String name;
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

    @Override
    public String toString() {
        return name;
    }

}
