package com.example.myfirstapp;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    Uri notification;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        this.ringtone= RingtoneManager.getRingtone(getApplicationContext(),notification);
        this.ringtone.play();
        setContentView(R.layout.activity_alarm);
    }

    public void killAlarm() {
        this.ringtone.stop();
        setContentView(R.layout.activity_main);
    }

}
