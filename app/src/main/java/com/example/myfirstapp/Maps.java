package com.example.myfirstapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private GoogleApiClient client;
    private Button alarmSetButton, alarmSaveButton, alarmCancelButton;
    private int MODE;
    // TEMPORARY
    private int distanceSetting = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        alarmSaveButton = (Button) findViewById(R.id.save_location_button);
        alarmSetButton = (Button) findViewById(R.id.set_location_button);
        alarmCancelButton = (Button) findViewById(R.id.cancel_alarm_button);
        MODE = 0; // Set mode to 1 when alarm is set

        alarmSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarm();
                Toast t = Toast.makeText
                        (getApplicationContext(), "Alarm set!", Toast.LENGTH_SHORT);
                t.show();
            }
        });

        alarmCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();
            }
        });

        alarmSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Saving", " starting save");
                if (currentMarker != null) {
                    Log.d("Saving", "got through");
                    LatLng point = currentMarker.getPosition();
                    String addressLine = getAddressLine(point);
                    Log.d("Location", addressLine);
                    SavedLocation s = new SavedLocation(point, addressLine);
                    try {
                        File file = new File(getFilesDir(), SavedLocations.LOCATIONS_FILENAME);
                        ArrayList<SavedLocation> temp = readData();
                        temp.add(s);
                        FileOutputStream fos = new FileOutputStream(file);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(temp);
                        oos.close();
                        Toast t = Toast.makeText
                                (getApplicationContext(), "Save successful!", Toast.LENGTH_SHORT);
                        t.show();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast t = Toast.makeText
                            (getApplicationContext(), "No location selected!", Toast.LENGTH_LONG);
                    t.show();
                }

            }
        });
    }

    private ArrayList<SavedLocation> readData() throws IOException {
        ArrayList<SavedLocation> read = new ArrayList<SavedLocation>();
        ObjectInputStream ois = null;
        File file = new File(getFilesDir(), SavedLocations.LOCATIONS_FILENAME);
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                read = (ArrayList<SavedLocation>) ois.readObject();
                ois.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return read;
    }

    public String getAddressLine(LatLng point) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> results = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            if (results != null) {
                return results.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Location";
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set view over Dublin
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.34932, -6.2603), 6.5f));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Hide Save button
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                alarmSetButton.setVisibility(View.INVISIBLE);
                alarmSaveButton.setVisibility(View.INVISIBLE);
            }
        });

        // Set Drag instructions
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if(MODE == 0) {
                    marker.hideInfoWindow();
                    alarmSetButton.setVisibility(View.INVISIBLE);
                    alarmSaveButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.setTitle(getAddressLine(marker.getPosition()));
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                currentMarker = marker;
            }
        });

        // Save location button
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(MODE == 0) {
                    alarmSetButton.setVisibility(View.VISIBLE);
                    alarmSaveButton.setVisibility(View.VISIBLE);
                    currentMarker = marker;
                    getDistance(currentMarker.getPosition());
                }
                return false;
            }

        });

        // Create Marker
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(MODE == 0) {
                    mMap.clear();
                    currentMarker = mMap.addMarker
                            (new MarkerOptions().position(latLng).title(getAddressLine(latLng)));
                    currentMarker.setDraggable(true);
                    currentMarker.showInfoWindow();
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentMarker.getPosition()));
                }
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            private static final String TAG = "MainActivity";

            @Override
            public void onPlaceSelected(Place place) {
                if(MODE == 0) {
                    mMap.clear();
                    Log.i(TAG, "Place: " + place.getName());
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title((String) place.getName()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16.5f));
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    public int getDistance(LatLng end){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        final Location startLocation = new Location(""); // provider name is unecessary
        startLocation.setLatitude(latitude); // your coords of course
        startLocation.setLongitude(longitude);

        Location dest = new Location("");
        dest.setLatitude(end.latitude); // your coords of course
        dest.setLongitude(end.longitude);
        int distance = (int) startLocation.distanceTo(dest);
        String temp = Integer.toString(distance);
        Toast d = Toast.makeText
                (getApplicationContext(), temp, Toast.LENGTH_SHORT);
        d.show();
        return distance;
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
        stopAlarm();
    }

    // For updating map every X seconds
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: FIX ME WHAAAAAT
            if(getDistance(currentMarker.getPosition()) <= distanceSetting) {
                stopAlarm();
                Intent i = new Intent(Maps.this, AlarmActivity.class);
                setContentView(R.layout.activity_alarm);
            }
            else {
                setAlarm();
            }
        }
    };

    public void setAlarm() {
        MODE = 1;
        handler.postDelayed(runnable, 2000);
        alarmSetButton.setVisibility(View.INVISIBLE);
        alarmSaveButton.setVisibility(View.INVISIBLE);
        alarmCancelButton.setVisibility(View.VISIBLE);
    }

    public void stopAlarm() {
        MODE = 0;
        handler.removeCallbacks(runnable);
        alarmCancelButton.setVisibility(View.INVISIBLE);
        Toast t = Toast.makeText
                (getApplicationContext(), "Alarm stopped!", Toast.LENGTH_SHORT);
        t.show();
    }
}
