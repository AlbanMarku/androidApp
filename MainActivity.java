package com.example.myapplicationtestmapfrag;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnStart, btnStop;
    private TextView textView;
    private BroadcastReceiver broadcastReceiver;
    private GoogleMap mMap;
    private Marker marker;
    public ArrayList<LatLng> list;
    private PolylineOptions polylineOptions;
    public LatLng lt;
    private LocationListener locationListener;
    private LocationManager location;
    private Boolean newSession;
    private PolylineOptions op;
    private int cl;
    private LatLng p2;

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    textView.append("\n" + intent.getExtras().get("coords"));
                    String m1 = intent.getExtras().get("coords").toString();
                    String m2 = intent.getExtras().get("coords2").toString();
                    Integer iEvent = Integer.parseInt(intent.getExtras().get("intValue").toString());
                    Log.i("iEvent",iEvent.toString() + "YOOOOOOOO"); //checking value of event
                    Float f = Float.parseFloat(m1);
                    Float f2 = Float.parseFloat(m2);
                    lt = new LatLng(f,f2);
                    list.add(lt);
                    getSessionDate();
                    //databaseMethod();
                    if (list.size() <=1) {
                        LatLng sp = lt;//Need to get current location on service start.
                        marker = mMap.addMarker(new MarkerOptions().position(sp).draggable(true));
                    }
                    //for (int i = 0; i < list.size(); i++) { // clean up code if this if statement isn't needed anymore.
                        switch (iEvent){
                            case 2:
                                Log.i("bigger","THAT WAS BIG GAS");
                                cl = context.getResources().getColor(R.color.teal_200);
                                break;
                            case 1:
                                Log.i("bigger","THAT WAS BIG BREAK");
                                cl = context.getResources().getColor(R.color.red);
                                break;
                            case 0:
                                Log.i("bigger","SMOOTH AF");
                                cl = context.getResources().getColor(R.color.black);
                                break;
                        }
                        LatLng p1 = list.get(list.size() - 1);
                        if(list.size() > 1) {
                            p2 = list.get(list.size() - 2);
                        } else {
                            p2 = p1;
                        }
                        op = new PolylineOptions().add(p1).add(p2);
                        Polyline polyline = mMap.addPolyline(op);
                        polyline.setColor(cl);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lt,15));
                   // }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location update"));
    }

    private void pushToDatabase() {
        OkHttpClient okHttpClient = new OkHttpClient();
        newSession = false;
        for (int i=0; i < list.size(); i++) {

            RequestBody formbody = new FormBody.Builder().add("value",list.get(i).toString()).add("test",newSession.toString()).add("timeVal",getSessionDate()).build();

            Request request = new Request.Builder().url("https://albonoproj.herokuapp.com").post(formbody).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("error","I didn't find anything");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.i("connectionFound",response.body().string());
                }
            });
        }
    }

    private String getSessionDate() {

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return "sunday";

            case Calendar.MONDAY:
                return "monday";


            case Calendar.TUESDAY:
                return "tuesday";


            case Calendar.WEDNESDAY:
                return "wednesday";

            case Calendar.THURSDAY:
                return "thursday";


            case Calendar.FRIDAY:
                return "friday";


            case Calendar.SATURDAY:
                return "saturday";

        }
        return "Unknown Date";
    }

//    private void databaseMethod() {
//        DataModel dataModel = new DataModel(lt,-1,getSessionDate());
//
//        DAHelper daHelper = new DAHelper(MainActivity.this);
//
//        boolean success = daHelper.addOne(dataModel);
//
//        Toast.makeText(MainActivity.this,"success" +success,Toast.LENGTH_SHORT).show();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.startButton);
        btnStop = (Button) findViewById(R.id.stopButton);
        textView = (TextView) findViewById(R.id.coordsText);
        //isWalking = true;
        list = new ArrayList<>();

        if(!runtime_permissions()) {
            enable_buttons();
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);


    }

    public void cleardb() {
        OkHttpClient okHttpClient = new OkHttpClient();
        newSession = true;
        RequestBody formbody = new FormBody.Builder().add("test",newSession.toString()).build();

        Request request = new Request.Builder().url("https://albonoproj.herokuapp.com").post(formbody).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("error","I didn't find anything");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i("connectionFound",response.body().string());
            }
        });
    }

    private void enable_buttons() {

        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                startService(i);
                //cleardb();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                stopService(i);
                pushToDatabase();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lt,10));
                //goToUrl("https://albonoproj.herokuapp.com");
            }
        });

    }

    private void goToUrl(String s) {
        Uri url = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW,url));

    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }
}