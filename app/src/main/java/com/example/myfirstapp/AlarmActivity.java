package com.example.myfirstapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import static com.example.myfirstapp.Settings.SHAKE_KEY;
import static com.example.myfirstapp.Settings.VIBRATE_KEY;


public class AlarmActivity extends AppCompatActivity implements SensorEventListener {

    Uri notification;
    private static boolean is_playing = false;
    private Ringtone ringtone;
    private Vibrator vibrator;
    protected SensorManager sensorManager;
    protected Sensor shakeSensor;
    private static final int SHAKE_DELAY_MS = 100;
    private static final String DEFAULT_SHAKE_AMOUNT = "7";
    private long lastShake = 0;
    private int shakeNumber = 2;
    private int shakeAmount;
    private boolean vibrateOn = true;
    //TODO: Add option to disable shake awake from settings menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("ALARM STARTED");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        updatePreferences();

        // Force window to appear over lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Set up Accelerometer if shake awake is active
        if(shakeAmount!=0) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                shakeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast t = Toast.makeText
                        (this.getApplicationContext(), "Shake awake not available!", Toast.LENGTH_SHORT);
                t.show();
            }
        }

        this.notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        this.ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        this.vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        if (!is_playing) {
            this.ringtone.play();
            if (vibrateOn) {
                long[] pattern = {400, 600};
                this.vibrator.vibrate(pattern, 0);
            }
        }
        is_playing = true;
    }

    private void updatePreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        shakeAmount = Integer.parseInt(prefs.getString(SHAKE_KEY, DEFAULT_SHAKE_AMOUNT));
        vibrateOn = prefs.getBoolean(VIBRATE_KEY, vibrateOn);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        long current_time = System.currentTimeMillis();
        double shake_force = 0;
        if (current_time - lastShake > SHAKE_DELAY_MS) {

            for (int i = 0; i < 3; i++) {
                shake_force += Math.abs(event.values[i]);
            }
            shake_force -= 9.8; // To cancel out gravity

            if (shake_force > shakeAmount) {
                shakeNumber--;
                lastShake = current_time;
            } else {
                // Reset shakes if small movement detected (eg. taking out of pocket)
                shakeNumber = 2;
                lastShake = current_time;
            }
            if (shakeNumber == 0) {
                killAlarm();
            }

        }
        // For debug
//        System.out.println("Force: "+shake_force+" | Shakes left: "+shakeNumber
//                +" | Time: "+current_time+ " | Last time: "+lastShake
//                +" | Diff: "+(current_time-lastShake));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing, accuracy is not changed
    }

    public void onPause() {
        super.onPause();
        if(shakeAmount!=0) {
            sensorManager.unregisterListener(this);
        }
    }

    public void onResume() {
        super.onResume();
        if (shakeAmount != 0) {
            sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void killAlarm() {
        if(shakeAmount!=0) {
            sensorManager.unregisterListener(this);
        }
        this.ringtone.stop();
        vibrator.cancel();
        is_playing = false;
        this.finish();
    }

    public void killAlarm(View v) {
        if(shakeAmount!=0) {
            sensorManager.unregisterListener(this);
        }
        this.ringtone.stop();
        this.vibrator.cancel();
        is_playing = false;
        this.finish();
    }

}
