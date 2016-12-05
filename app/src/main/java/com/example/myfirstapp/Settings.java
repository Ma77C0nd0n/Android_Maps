package com.example.myfirstapp;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import java.util.List;

/**
 * Settings encompasses a number of application preferences
 * 2 Fragments are contained in the main screen, Alarm Settings and Map Settings
 * Alarm Settings allows for the configuration of Vibration, Shake Awake Sensitivity and the Alarm Tone
 * Map settings allows the user to select a numeric figure for the number of meters from which the
 * point that the alarm should go off
 * A lot of this implementation is extracted from the template set by the Android Studio default Settings Activity
 */
public class Settings extends AppCompatPreferenceActivity {
    //List of key values for mapping preferences both locally and globally
    public static final String SHAKE_KEY = "shake_amount";
    public static final String VIBRATE_KEY = "vibrate_status";
    public static final String ALARM_TONE = "alarm_tone";
    public static final String MAP_DISTANCE = "map_distance";

    /**
     * Helper method utilised for very large screens - default implementation
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method allows for the declaration of settings fragmentsS
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AlarmPreferenceFragment.class.getName().equals(fragmentName)
                || MapPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment displays the Alarm preferences fragment
     * This includes vibration, shake awake sensitivity and the alarm tone
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AlarmPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_alarm);
            setHasOptionsMenu(true);
        }
    }

    /**
     * This fragment displays the Map preference fragment
     * This includes just the alarm distance, measured in meters
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MapPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_map);
            setHasOptionsMenu(true);
        }
    }
}
