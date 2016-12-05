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
/**
 * In this activity, a file containing all saved locations is read and its contents extracted to populate a listview, with a popup
 * menu for each entry where the user can delete a location, edit a lcoation's name, or show the location on the map to set an
 * alarm with it without having to search for the address. I created a class to model a saved location, which holds values for its
 * latitude and longitude, and a string name (which is the field a user can edit). I built a custom array adapter so the listview
 * could be populated by my custom objects.
 *
 * Threading is used to read data from the file, and the activity implements on click listeners for the different buttons
 * available for each entry in the listview. When the user is leaving the activity, the file's contents will be overwritten
 * with the new, changed data (where some locations could have been removed / had their names changed).
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
     * onCreate will initialise and set the array adapter to populate the listview. The data is read on a seperate thread.
     * @param savedInstanceState
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

    // Handler where the thread to read the data happens
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

    /**
     * When the activity is stopping (either because another activity is starting, or the back button is pressed), the data in the
     * file will be updated.
     * @param
     */
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

    /**
     * When the activity is resumed after having been stopped, the data will be read again to ensure it doesn't miss any updates
     * @param
     */
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

    /**
     * Here, the app's file directory is searched for a file with the same name as the string stored in this class.
     * If it exists, the array list containing the serializable SavedLocation objects is retrieved.
     * @param
     */
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

    /**
     * Custom array adapter to ensure my custom list contents (SavedLocation objects) and the custom row entry layout can be adapted
     * for the listview. Class contains onClickListeners for the various buttons presented with the popup menu. A viewholder is also
     * used to recycle rows to improve the listviews performance: as rolls scroll out of view, they are hot swapped out so the app doesn't
     * keep every row held at once, but only the amount that will fit in the screen.
     * @param
     */
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
                // Click listener for the button that activates the popdown menu. A switch statement is used to see which button
                // is pressed in this menu.
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
                                    // User wants to edit location name. Alert Dialog is created to allow user input.
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
                                    // Removing location from list.
                                    case R.id.location_remove:
                                        list.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(getApplicationContext(), "Removed location!", Toast.LENGTH_SHORT).show();
                                        return true;
                                    // User indicates they want to open location on map. Location info is stored in an intent,
                                    // which is used to start the next activity.
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
