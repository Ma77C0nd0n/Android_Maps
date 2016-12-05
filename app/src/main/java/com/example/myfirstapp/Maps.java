package com.example.myfirstapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

import static com.example.myfirstapp.Settings.MAP_DISTANCE;

public class Maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private Circle currentCircle = null;
    private GoogleApiClient client;
    private Button alarmSetButton, alarmSaveButton, alarmCancelButton, spotifyButton;
    private int MODE;
    // TEMPORARY
    private static final String DEFAULT_DISTANCE = "200";
    private int distanceSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        distanceSetting = Integer.parseInt(prefs.getString(MAP_DISTANCE, DEFAULT_DISTANCE));

        // Set up buttons and alarm mode
        alarmSaveButton = (Button) findViewById(R.id.save_location_button);
        alarmSetButton = (Button) findViewById(R.id.set_location_button);
        alarmCancelButton = (Button) findViewById(R.id.cancel_alarm_button);
        spotifyButton = (Button) findViewById(R.id.spotifyButton);
        MODE = 0; // Set mode to 1 when alarm is set

        // Google Maps API default code {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        // } end Google Maps API default code

        // Set click listener for SET ALARM button
        alarmSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarm();
                Toast t = Toast.makeText
                        (getApplicationContext(), "Alarm set!", Toast.LENGTH_SHORT);
                t.show();
            }
        });

        // Set click listener for STOP ALARM button
        alarmCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();
                Toast t = Toast.makeText
                        (getApplicationContext(), "Alarm stopped!", Toast.LENGTH_SHORT);
                t.show();
            }
        });

        // Set click listener for SPOTIFY button
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpotifyActivity();
            }
        });

        // Set click listener for SAVE ALARM button
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

    // Method to read data from saved locations
    private ArrayList<SavedLocation> readData() throws IOException {
        ArrayList<SavedLocation> read = new ArrayList<SavedLocation>();
        ObjectInputStream ois;
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

    // Method to get address from LatLng
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

    // onMapReady method for Google Maps API
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        // Set view over Dublin
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.34932, -6.2603), 6.5f));

        // Show 'my location' button
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
            // Hide the text above marker, hide set and save alarm buttons
            @Override
            public void onMarkerDragStart(Marker marker) {
                if(MODE == 0) {
                    marker.hideInfoWindow();
                    currentCircle.remove();
                    alarmSetButton.setVisibility(View.INVISIBLE);
                    alarmSaveButton.setVisibility(View.INVISIBLE);
                }
            }

            // Required method
            @Override
            public void onMarkerDrag(Marker marker) {
                // Do nothing
            }

            // Update marker with new location information
            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.setTitle(getAddressLine(marker.getPosition()));
                marker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                currentMarker = marker;
                drawCircle();
            }
        });

        // Show SET / SAVE buttons when marker is clicked
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(MODE == 0) {
                    alarmSetButton.setVisibility(View.VISIBLE);
                    alarmSaveButton.setVisibility(View.VISIBLE);
                    currentMarker = marker;
                }
                return false;
            }
        });

        // Create Marker from long press
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(MODE == 0) {
                    mMap.clear();
                    currentMarker = mMap.addMarker
                            (new MarkerOptions().position(latLng).title(getAddressLine(latLng)));
                    currentMarker.setDraggable(true);
                    currentMarker.showInfoWindow();
                    currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pin_small));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentMarker.getPosition()));
                    drawCircle();
                }
            }
        });

        // Initialize search bar above map fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Set up listener for when address is selected from search bar
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            private static final String TAG = "MapsActivity";

            // Location has been selected, place marker on map and zoom to location
            @Override
            public void onPlaceSelected(Place place) {
                if(MODE == 0) {
                    mMap.clear();
                    Log.i(TAG, "Place: " + place.getName());
                    currentMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                            .title((String) place.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_small)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16.5f));
                    drawCircle();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    // Method to get distance from current location to marker on map
    public int getDistance(LatLng end){

        // Check location is enabled
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
        return distance;
    }

    // Default method from Google Maps API
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

    // onStart method: Set up Google map
    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    // onStop method: Prevent death while alarm is set
    @Override
    public void onStop() {
        if(MODE == 1) {
            Toast t = Toast.makeText
                    (getApplicationContext(), "Alarm still running in background!", Toast.LENGTH_LONG);
            t.show();
        }
        super.onStop();
    }

    // onBackPressed method: Prevent maps death while alarm is set
    @Override
    public void onBackPressed() {
        if (MODE == 0) {
            super.onBackPressed();
        } else {
            Toast t = Toast.makeText
                    (getApplicationContext(), "Please stop alarm before" +
                            " leaving this screen!", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    // For updating alarm every 2 seconds, seems like a reasonable amount of time
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(getDistance(currentMarker.getPosition()) <= distanceSetting) {
                stopAlarm();
                Intent i = new Intent(Maps.this, AlarmActivity.class);
                startActivity(i);
            }
            else {
                setAlarm();
            }
        }
    };

    // setAlarm method: update MODE and set button visibility + begin handler for alarm
    public void setAlarm() {
        MODE = 1;
        handler.postDelayed(runnable, 2000);
        alarmSetButton.setVisibility(View.INVISIBLE);
        alarmSaveButton.setVisibility(View.INVISIBLE);
        alarmCancelButton.setVisibility(View.VISIBLE);
        spotifyButton.setVisibility(View.VISIBLE);
    }

    // stopAlarm method: update MODE and set button visibility + stop handler for alarm
    public void stopAlarm() {
        MODE = 0;
        handler.removeCallbacks(runnable);
        alarmCancelButton.setVisibility(View.INVISIBLE);
        spotifyButton.setVisibility(View.INVISIBLE);
    }

    // startSpotifyActivity method: Starts Spotify
    public void startSpotifyActivity() {
        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.spotify.music");
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=com.spotify.music"));
            startActivity(intent);
        }
    }

    // Draw circle around marker
    private void drawCircle() {
        CircleOptions circleOptions = new CircleOptions().center(currentMarker.getPosition())
                .radius(distanceSetting).fillColor(R.color.colorAccent).strokeWidth(3)
                .strokeColor(R.color.colorWhite);
        currentCircle = mMap.addCircle(circleOptions);
    }
}
