package com.example.myfirstapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
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

public class Maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private GoogleApiClient client;
    private Button alarmSetButton, alarmSaveButton, alarmCancelButton;
    private int MODE;


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
                alarmSetButton.setVisibility(View.INVISIBLE);
                alarmSaveButton.setVisibility(View.INVISIBLE);
                alarmCancelButton.setVisibility(View.VISIBLE);
                Toast t = Toast.makeText
                        (getApplicationContext(), "Alarm set!", Toast.LENGTH_SHORT);
                t.show();
            }
        });

        alarmCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();
                alarmCancelButton.setVisibility(View.INVISIBLE);
                Toast t = Toast.makeText
                        (getApplicationContext(), "Alarm stopped!", Toast.LENGTH_SHORT);
                t.show();
            }
        });

        alarmSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Saving"," starting save");
                if (currentMarker != null) {
                    Log.d("Saving", "got through");
                    String name = "location";
                    SavedLocation s = new SavedLocation(currentMarker.getPosition(), name);
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
                        Log.d("Saving","hmm");
                        ex.printStackTrace();
                    }
                } else {
                    Toast t = Toast.makeText
                            (getApplicationContext(), "No location selected!", Toast.LENGTH_LONG);
                    t.show();
                }
                //TODO: Evin put saving code here pls <3

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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);

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
                marker.hideInfoWindow();
                alarmSetButton.setVisibility(View.INVISIBLE);
                alarmSaveButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.setTitle(marker.getPosition().latitude + ", "
                        + marker.getPosition().longitude);
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                currentMarker = marker;
            }
        });

        // Save location button
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                alarmSetButton.setVisibility(View.VISIBLE);
                alarmSaveButton.setVisibility(View.VISIBLE);
                currentMarker = marker;
                return false;
            }

        });

        // Create Marker
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(
                        "" + latLng.latitude + "," + latLng.longitude
                ));
                marker.setDraggable(true);
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                currentMarker = marker;
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            private static final String TAG = "MainActivity";

            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                Log.i(TAG, "Place: " + place.getName());
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title((String) place.getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16.5f));

            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        // Set view over Dublin
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.34932, -6.2603), 6.5f));
    }

    private void hideKeyboard() {
        InputMethodManager imm
                = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private void setAlarm() {
        MODE = 1;
        // TODO: Get distance to work and use alarm
    }

    private void stopAlarm() {
        MODE = 0;
        // TODO: Stop distance form working
    }
}
