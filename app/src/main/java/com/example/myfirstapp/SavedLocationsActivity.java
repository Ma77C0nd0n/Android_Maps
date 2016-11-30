package com.example.myfirstapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by rucha on 13-Nov-16.
 */

public class SavedLocationsActivity extends AppCompatActivity {
    private ArrayList<String> listItems = new ArrayList<String>();
    private ListView listView;
    private Runnable retrieveLocations;

    private static final String LOCATIONS_FILENAME = "locations";
    LocationsAdapter adapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savedlocations);
        listView = (ListView) findViewById(R.id.list);
        adapter = new LocationsAdapter<String>(this, R.layout.locations_row, listItems);
        listView.setAdapter(adapter);
        retrieveLocations = new Runnable(){
            public void run(){
                handler.sendEmptyMessage(0);
            }
        };
        Thread t = new Thread(null, retrieveLocations, "ReadFile");
        t.start();
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            BufferedReader input = null;
            File file = null;
            try {
                file = new File(getFilesDir(), LOCATIONS_FILENAME); // Pass getFilesDir() and "MyFile" to read file
                input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = input.readLine()) != null) {
//                    String[] locationArray = line.split("|");
//                    System.out.println(locationArray[0]);
                    listItems.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            adapter = new LocationsAdapter(SavedLocationsActivity.this, R.layout.locations_row, listItems);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    };

    public void writeData(View w) {
        File file;
        String content = "Location\n";
        String content1 = "Location\n";
        String content2 = "Location\n";
        FileOutputStream outputStream;
        try {
            file = new File(LOCATIONS_FILENAME);
            outputStream = openFileOutput(LOCATIONS_FILENAME, MODE_APPEND);
            outputStream.write(content.getBytes());
            outputStream.write(content1.getBytes());
            outputStream.write(content2.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        listItems.add("");
//        adapter.notifyDataSetChanged();
    }

    public class LocationsAdapter<String> extends ArrayAdapter<String> {

        ArrayList<String> list;
        private TextView text;

        public LocationsAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            list = objects;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            LayoutInflater inflater = getLayoutInflater();

            if (convertView == null) {
                viewHolder = new ViewHolder();
//                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.locations_row, null, false);
                viewHolder.locationName = (TextView) convertView.findViewById(R.id.location_name);
                viewHolder.delete = (Button) convertView.findViewById(R.id.delete);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
//            System.out.println(list.size());
//            System.out.println("hello: " +list.get(0));
            String i = list.get(position);
            if (i != null) {
                System.out.println(list.get(position));
                viewHolder.locationName.setText(list.get(position).toString());
                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        list.remove(position);
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
