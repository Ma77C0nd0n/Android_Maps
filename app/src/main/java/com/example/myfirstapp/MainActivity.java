package com.example.myfirstapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    /**
     * Main activity where permissions are set. Intents sent according to the button that is clicked.
     * @param
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions
                    (this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    public void onButtonClick(View v) {
        Intent i;
        if (v.getId() == R.id.Bsettings) {
            i = new Intent(MainActivity.this, Settings.class);
        } else if (v.getId() == R.id.Bsavedlocations) {
            i = new Intent(MainActivity.this, SavedLocations.class);
        } else if (v.getId() == R.id.Bmap) {
            i = new Intent(MainActivity.this, Maps.class);
        } else {
            i = null;
        }
        startActivity(i);
    }
}
