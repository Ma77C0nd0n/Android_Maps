package com.example.myfirstapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    public static final String LAT_KEY = "lat";
    public static final String LNG_KEY = "lng";
    public static final String NAME_KEY = "name";
    private LocationsAdapter adapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savedlocations);
        adapter = new LocationsAdapter(this, R.layout.locations_row, listLocations);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        retrieveLocations = new Runnable() {
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };
        Thread t = new Thread(null, retrieveLocations, "ReadFile");
        t.start();
    }

    @Override
    protected void onStop() {
        try {
            File file = new File(getFilesDir(), LOCATIONS_FILENAME);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(listLocations);
            oos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        try {
            listLocations = readData();
        } catch (IOException e) {
            listLocations = new ArrayList<SavedLocation>();
        }
        adapter = new LocationsAdapter(SavedLocations.this, R.layout.locations_row, listLocations);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                listLocations = readData();
            } catch (IOException e) {
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

        private ArrayList<SavedLocation> list;
        private final Context context;

        public LocationsAdapter(Context context, int textViewResourceId, ArrayList<SavedLocation> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
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
                viewHolder.popdown = (Button) convertView.findViewById(R.id.location_menu);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SavedLocation l = list.get(position);
            if (l != null) {
                viewHolder.locationName.setText(l.toString());
                viewHolder.popdown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(context, v);
                        popup.getMenuInflater().inflate(R.menu.locations_popdown, popup.getMenu());
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.location_edit:
                                        final EditText text = new EditText(context);
                                        text.setInputType(InputType.TYPE_CLASS_TEXT);
                                        new AlertDialog.Builder(context)
                                                .setTitle("Enter new location name")
                                                .setView(text)
                                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        String name = text.getText().toString();
                                                        list.get(position).setName(name);
                                                        adapter.notifyDataSetChanged();
                                                        Toast.makeText(getApplicationContext(), "Name changed!", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                }).show();
                                        return true;
                                    case R.id.location_remove:
                                        list.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(getApplicationContext(), "Removed location!", Toast.LENGTH_SHORT).show();
                                        return true;
                                    case R.id.location_set:
                                        Intent intent = new Intent(SavedLocations.this, Maps.class);
                                        SavedLocation s = list.get(position);
                                        intent.putExtra(LAT_KEY, s.lat);
                                        intent.putExtra(LNG_KEY, s.lng);
                                        intent.putExtra(NAME_KEY, s.name);
                                        startActivity(intent);
                                        return true;
                                }
                                return false;
                            }
                        });
                    }
                });
            }
            return convertView;
        }

        private class ViewHolder {
            TextView locationName;
            Button popdown;
        }

    }
}
