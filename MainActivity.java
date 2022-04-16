package com.example.myapplicationtestmapfrag;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private Switch sw;
    private ImageView iv;
    private BroadcastReceiver broadcastReceiver;
    private GoogleMap mMap;
    private Marker marker;
    public ArrayList<LatLng> list;
    public ArrayList<String> walkList;
    private PolylineOptions polylineOptions;
    public LatLng lt;
    private LocationListener locationListener;
    private LocationManager location;
    private Boolean newSession;
    private PolylineOptions op;
    private int cl;
    private LatLng p2;
    public static Boolean isWalking = true;
    public static int events= 0;//Set to zero for real life use.
    public static Boolean isWorking;
    private String workerUrl;
    private Integer startInt;
    private Integer endInt;
    private String m_Text = "";
    private String m1,m2;
    private String starterPoiLat;
    private String getStarterPoiLon;
    private double totalDistance;

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    textView.append("\n" + intent.getExtras().get("coords"));
                    m1 = intent.getExtras().get("coords").toString();
                    m2 = intent.getExtras().get("coords2").toString();
                    Integer iEvent = Integer.parseInt(intent.getExtras().get("intValue").toString());
                    Log.i("iEvent", iEvent.toString() + "YOOOOOOOO");
                    Float f = Float.parseFloat(m1);
                    Float f2 = Float.parseFloat(m2);
                    lt = new LatLng(f, f2);
                    list.add(lt);
                    walkList.add(isWalking.toString());
                    Log.i("list", list.toString());
                    getSessionDate();
                    if (list.size() == 1) {
                        LatLng sp = lt;
                        marker = mMap.addMarker(new MarkerOptions().position(sp).draggable(true));
                    }
                    switch (iEvent) {
                        case 2:
                            Log.i("bigger", "THAT WAS BIG GAS");
                            cl = context.getResources().getColor(R.color.teal_200);
                            events++;
                            break;
                        case 1:
                            Log.i("bigger", "THAT WAS BIG BREAK");
                            cl = context.getResources().getColor(R.color.red);
                            events++;
                            break;
                        case 3:
                            cl = context.getResources().getColor(R.color.purple_500);
                            break;
                        case 0:
                            Log.i("bigger", "SMOOTH AF");
                            cl = context.getResources().getColor(R.color.black);
                            break;
                    }
                    LatLng p1 = list.get(list.size() - 1);
                    if (list.size() > 1) {
                        p2 = list.get(list.size() - 2);
                    } else {
                        p2 = p1;
                    }
                    op = new PolylineOptions().add(p1).add(p2);
                    Polyline polyline = mMap.addPolyline(op);
                    polyline.setColor(cl);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lt, 15));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location update"));
    }

    private void askForStartBat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input car battery level");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                startInt = Integer.parseInt(m_Text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startInt = 0;
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void pushToDatabase() {
        OkHttpClient okHttpClient = new OkHttpClient();
        newSession = false;

        Location st = new Location("a");
        Location dt = new Location("b");
        st.setLatitude(Double.parseDouble(starterPoiLat));
        st.setLongitude(Double.parseDouble(getStarterPoiLon));

        dt.setLatitude(Double.parseDouble(m1));
        dt.setLongitude(Double.parseDouble(m2));

        totalDistance = st.distanceTo(dt);


        for (int i = 0; i < list.size(); i++) {
            RequestBody formbody = new FormBody.Builder().add("value", list.get(i).toString()).add("test", newSession.toString()).add("timeVal", getSessionDate()).add("walkVal", walkList.get(i)).add("workVal", isWorking.toString()).add("eventVal", String.valueOf(events)).add("startVal", startInt.toString()).add("endVal",endInt.toString()).add("disVal", String.valueOf(totalDistance)).build();

            Request request = new Request.Builder().url("https://albonoproj.herokuapp.com").post(formbody).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("error", "I didn't find anything");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.i("connectionFound", response.body().string());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
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
        sw = (Switch) findViewById(R.id.walkSwitch);
        iv = (ImageView) findViewById(R.id.imageViewBity);
        list = new ArrayList<>();
        walkList = new ArrayList<>();

        sw.setChecked(true);

        if (!runtime_permissions()) {
            enable_buttons();
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);


    }

    public void cleardb() {
        OkHttpClient okHttpClient = new OkHttpClient();
        newSession = true;
        RequestBody formbody = new FormBody.Builder().build();

        Request request = new Request.Builder().url("https://albonoproj.herokuapp.com/delete/"+workerUrl+"/"+getSessionDate()).post(formbody).build();
        Log.i("deletion", "https://albonoproj.herokuapp.com/delete/"+workerUrl+"/"+getSessionDate());
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("error", "I didn't find anything");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i("deletion", "deletion");
            }
        });
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/com.example.myapplicationtestmapfrag/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,getSessionDate()+isWorking.toString()+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void startService() {
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        startService(i);
    }

    private void openHome() {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }

    private void confirmOverwrite() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Journey exists. Confirm overwrite?");

                String[] s = {"Yes", "No"};
                builder.setItems(s, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                cleardb();
                                startService();
                                break;
                            case 1:
                                openHome();
                                break;
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void checkIfDayExists() {
        Log.i("existsCheck", "entered");
        if(isWorking) {
            workerUrl = "toWork";
        } else {
            workerUrl = "toHome";
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody formbody = new FormBody.Builder().build();

        Request request = new Request.Builder().url("https://albonoproj.herokuapp.com/"+workerUrl+"/"+getSessionDate()).post(formbody).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("existsCheck", "https://albonoproj.herokuapp.com/"+workerUrl+"/"+getSessionDate());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String s = response.body().string();

                if (s.equals("error")) {
                    Log.i("existsCheck", "match");
                    startService();

                } else {
                    Log.i("existsCheck", "nope");
                    confirmOverwrite();
                }
            }
        });

    }

    private void enable_buttons() {

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("existsCheck", "called");
                checkIfDayExists();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askStopBat();

            }
        });

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    isWalking = true;
                } else {
                    isWalking = false;
                    starterPoiLat = m1;
                    getStarterPoiLon = m2;
                    askForStartBat();
                }
            }
        });

    }

    private void closeProcess() {
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        stopService(i);
        pushToDatabase();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lt, 10));
        newSession = true; // sussy
        //goToUrl("https://albonoproj.herokuapp.com");

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after delay
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(@Nullable Bitmap bitmap) {
                        Bitmap bt = bitmap;
                        saveToInternalStorage(bt);
                        iv.setImageBitmap(bt);
                        //loadImageFromStorage("/data/data/com.example.myapplicationtestmapfrag/app_imageDir");
                    }
                });
            }
        }, 3000);
    }

    private void askStopBat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input car battery level");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                endInt = Integer.parseInt(m_Text);
                closeProcess();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endInt = 0;
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void goToUrl(String s) {
        Uri url = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW, url));

    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }
}