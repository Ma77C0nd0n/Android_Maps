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


    public void onButtonClick(View v){
        if(v.getId()==R.id.Bsettings){
            Intent i = new Intent(MainActivity.this,Settings.class);
            startActivity(i);
        }
    }
    public void onSLClick(View v){
        if(v.getId()==R.id.Bsavedlocations){
            Intent i = new Intent(MainActivity.this,SavedLocations.class);
            startActivity(i);
        }
    }

    public void onMapButtonClick(View v){
        if(v.getId()==R.id.Bmap){
            Intent i = new Intent(MainActivity.this,Maps.class);
            startActivity(i);
        }
    }


}
