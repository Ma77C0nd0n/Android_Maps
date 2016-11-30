package com.example.myfirstapp;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by evin_ on 27/11/2016.
 */

public class SavedLocation implements Serializable {
    LatLng point;
    double lat, lng;
    String name;

    SavedLocation(LatLng point, String name){
        this.point = point;
        this.name = name;

    }

}
