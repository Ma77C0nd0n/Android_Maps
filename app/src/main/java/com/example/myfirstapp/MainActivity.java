package com.example.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onButtonClick(View v) {
        Intent i;
        if (v.getId() == R.id.Bsettings) {
            i = new Intent(MainActivity.this, Settings.class);
        } else if (v.getId() == R.id.Bsavedlocations) {
            i = new Intent(MainActivity.this, SavedLocations.class);
        } else if (v.getId() == R.id.Bmap) {
            i = new Intent(MainActivity.this, Maps.class);
        } else if (v.getId() == R.id.debug_alarm_button) {
            i = new Intent(MainActivity.this, AlarmActivity.class);
        } else {
            i = null;
        }
        startActivity(i);
    }

}
