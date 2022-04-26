package com.example.myapplicationtestmapfrag;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;

public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager location;
    private Float speedFl;
    private Float mphFl = 0f;
    private Float prevMphFl = 0f;
    public Integer eventInt;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() { // When gps service starts, location listener is created tracking the device. When location updates, speed is calculated and coords are sent to main activit.
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                speedFl = location.getSpeed();
                prevMphFl = mphFl;
                mphFl = speedFl * 2.2369f;
                compareSpeed();
                Intent i = new Intent("location update");
                i.putExtra("coords", location.getLatitude());
                i.putExtra("coords2", location.getLongitude());
                i.putExtra("intValue", eventInt);
                sendBroadcast(i);
            }

            @Override//opens gps settings if disabled.
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }

            @Override
            public void onProviderEnabled(String s) {

            }
        };
        //Location is checked when a min distance of 5 meters in 3 seconds has been traveled.
        location = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, listener);
    }

    public int compareSpeed() {// Decides if ad driving event has occured by difference in seed in a five second period.
        if (mphFl - prevMphFl <= -10f && !MainActivity.isWalking) {
            eventInt = 1;
            return 1;
        } else if (mphFl - prevMphFl >= 10f && !MainActivity.isWalking) {
            eventInt = 2;
            return 2;
        } else if (MainActivity.isWalking) {
            eventInt = 3;
            return 3;
        } else {
            eventInt = 0;
            return 0;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (location != null) {
            location.removeUpdates(listener);
        }
    }

}
