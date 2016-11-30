package com.example.myfirstapp;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by rucha on 13-Nov-16.
 */

public class SavedLocations extends AppCompatActivity {

    private ArrayList<SavedLocation> listLocations = new ArrayList<SavedLocation>();
    private ListView listView;
    private Runnable retrieveLocations;
    public static final String LOCATIONS_FILENAME = "locations";
    private LocationsAdapter adapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savedlocations);
        adapter = new LocationsAdapter (this, R.layout.locations_row, listLocations);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        retrieveLocations = new Runnable(){
            public void run(){
                handler.sendEmptyMessage(0);
            }
        };
        Thread t = new Thread(null, retrieveLocations, "ReadFile");
        t.start();
    }

    @Override
    protected void onStop() {
        Log.d("Flow", "stop");
        try {
            File file = new File(getFilesDir(), LOCATIONS_FILENAME);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(listLocations);
            oos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Log.d("Leaving activity", "list size is " + String.valueOf(listLocations.size()));
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d("Flow", "pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("Flow", "destroy");
        super.onDestroy();
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            try {
                listLocations = readData();
            }
            catch (IOException e){
                listLocations = new ArrayList<SavedLocation>();
            }
            adapter = new LocationsAdapter(SavedLocations.this, R.layout.locations_row, listLocations);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    };

    private ArrayList<SavedLocation> readData() throws IOException {
        ArrayList<SavedLocation> read = new ArrayList<SavedLocation>();
        ObjectInputStream ois = null;
        File file = new File(getFilesDir(), LOCATIONS_FILENAME);
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

    public class LocationsAdapter extends ArrayAdapter<SavedLocation> {

        ArrayList<SavedLocation> list;
        private TextView text;

        public LocationsAdapter(Context context, int textViewResourceId, ArrayList<SavedLocation> objects) {
            super(context, textViewResourceId, objects);
            list = objects;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            LayoutInflater inflater = getLayoutInflater();

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.locations_row, null, false);
                viewHolder.locationName = (TextView) convertView.findViewById(R.id.location_name);
                viewHolder.delete = (Button) convertView.findViewById(R.id.delete);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SavedLocation l = (SavedLocation) list.get(position);
            if (l != null) {
                viewHolder.locationName.setText(l.toString());
                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        list.remove(position);
                        Log.d("Size", "adapter list: " + list.size());
                        Log.d("Size", "list: " + listLocations.size());
                        adapter.notifyDataSetChanged();
                    }
                });
            }
            return convertView;
        }

        private class ViewHolder {
            TextView locationName;
            Button delete;
        }

    }
}
