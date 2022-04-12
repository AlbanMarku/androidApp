package com.example.myapplicationtestmapfrag;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.security.Provider;

public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager location;
    private Float speedFl;
    private Float mphFl = 0f;
    private Float prevMphFl = 0f;
    public Integer eventInt;
    private MainActivity ma;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        MainActivity.isWalking= true;
        Log.i("walk","reading not crashed");
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                speedFl = location.getSpeed();
                prevMphFl = mphFl;
                mphFl= speedFl * 2.2369f;
                Log.i("speed",mphFl.toString() + " mph");
                Log.i("speedDifference","PREV  " +prevMphFl.toString()+"  " +"NOW  "+mphFl.toString());
               // if (prevMphFl != 0f) { // Test if this is good condition to stop init big  gas
                    compareSpeed();
               // }
                Intent i = new Intent("location update");
                i.putExtra("coords", location.getLatitude());
                i.putExtra("coords2", location.getLongitude());
                i.putExtra("intValue", eventInt);
                sendBroadcast(i);
            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };
        location = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, listener); //set min distance proper units
    }

    public int compareSpeed() {
        if (mphFl - prevMphFl <= -10f) {
            Log.i("checkSlow","Big breaks");
            eventInt = 1;
            return 1;
        } else if(mphFl - prevMphFl >= 10f) {
            Log.i("checkSlow","Big gas");
            eventInt = 2;
            return 2;
        } else {
            Log.i("checkSlow","No event");
            eventInt = 0;
            return 0;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(location != null) {
            location.removeUpdates(listener);
        }
    }

}
