package com.example.myapplicationtestmapfrag;

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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnStart, btnStop;
    private Switch sw;
    private BroadcastReceiver broadcastReceiver;
    private GoogleMap mMap;
    private Marker marker;
    public ArrayList<LatLng> list;
    public ArrayList<String> walkList;
    public LatLng lt;
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
    private Boolean connection = false;
    private TextView warnText;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override //on Resume called when gps service broadcasts data to this class. Method deals with adding coords to a list, storing events and drawing on the map.
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    connection = true;
                    btnStop.setEnabled(true);
                    m1 = intent.getExtras().get("coords").toString();
                    m2 = intent.getExtras().get("coords2").toString();
                    Integer iEvent = Integer.parseInt(intent.getExtras().get("intValue").toString());
                    Float f = Float.parseFloat(m1);
                    Float f2 = Float.parseFloat(m2);
                    lt = new LatLng(f, f2);
                    list.add(lt);
                    walkList.add(isWalking.toString());
                    getSessionDate();
                    if (list.size() == 1) {
                        LatLng sp = lt;
                        marker = mMap.addMarker(new MarkerOptions().position(sp).draggable(true));
                        sw.setEnabled(true);
                    }
                    switch (iEvent) {
                        case 2:
                            cl = context.getResources().getColor(R.color.teal_200);
                            events++;
                            break;
                        case 1:
                            cl = context.getResources().getColor(R.color.red);
                            events++;
                            break;
                        case 3:
                            cl = context.getResources().getColor(R.color.purple_500);
                            break;
                        case 0:
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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lt, 12));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location update"));
    }
    //Gets start battery
    private void askForStartBat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input car battery level");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

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

    //Called whe journey is over. Pushes all data to specified url with data attached.
    private void pushToDatabase() {
        if(startInt == null || endInt == null) {
            startInt = 100;
            endInt = 100;
        }
        OkHttpClient okHttpClient = new OkHttpClient();

        Location st = new Location("a");
        Location dt = new Location("b");
        st.setLatitude(Double.parseDouble(starterPoiLat));
        st.setLongitude(Double.parseDouble(getStarterPoiLon));

        dt.setLatitude(Double.parseDouble(m1));
        dt.setLongitude(Double.parseDouble(m2));

        totalDistance = st.distanceTo(dt);

        BigDecimal roundfinalPrice = new BigDecimal(totalDistance).setScale(2,BigDecimal.ROUND_HALF_UP);


        for (int i = 0; i < list.size(); i++) {
            RequestBody formbody = new FormBody.Builder().add("value", list.get(i).toString()).add("timeVal", getSessionDate()).add("walkVal", walkList.get(i)).add("workVal", isWorking.toString()).add("eventVal", String.valueOf(events)).add("startVal", startInt.toString()).add("endVal",endInt.toString()).add("disVal", roundfinalPrice.toString()).add("nameVal", Activity2.logStr).build();
            Request request = new Request.Builder().url("https://albonoproj.herokuapp.com").post(formbody).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("error", "I didn't find anything");
                    tryAgain();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.i("connectionFound", response.body().string());
                }
            });
        }
    }
    //Called when url for pushing data does not return a response.
    private void tryAgain() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Could not push journey online. Try again?");

                String[] options = {"Yes", "No"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                pushToDatabase();
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
    //Fetches date.
    private String getSessionDate() {

        Calendar calendar = Calendar.getInstance();
        String curDatStr = DateFormat.getDateInstance().format(calendar.getTime());
        curDatStr = curDatStr.replace(" ","");
        curDatStr = curDatStr.replace(",","");
        Log.i("datestr", curDatStr);
        return curDatStr;
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

        setTitle("Electric Journey Companion");
        btnStart = (Button) findViewById(R.id.startButton);
        btnStop = (Button) findViewById(R.id.stopButton);
        sw = (Switch) findViewById(R.id.walkSwitch);
        warnText = (TextView) findViewById(R.id.warningText);
        list = new ArrayList<>();
        walkList = new ArrayList<>();

        sw.setChecked(true);
        sw.setEnabled(false);
        btnStop.setEnabled(false);
        getSessionDate();

        if (!runtime_permissions()) {
            enable_buttons();
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);


    }
    //Called when user wants to overwirte existing journey. Url deletes related data.
    public void cleardb() {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody formbody = new FormBody.Builder().add("name",Activity2.logStr).build();

        Request request = new Request.Builder().url("https://albonoproj.herokuapp.com/delete/"+workerUrl+"/"+getSessionDate()).post(formbody).build();
        Log.i("deletion", "https://albonoproj.herokuapp.com/delete/"+workerUrl+"/"+getSessionDate());
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("error", "I didn't find anything");
                Toast.makeText(MainActivity.this,"Error connecting to server",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i("deletion", "deletion");
            }
        });
    }
    //saves a bitmap of the journey with a specific file name
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/com.example.myapplicationtestmapfrag/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath=new File(directory,getSessionDate()+isWorking.toString()+Activity2.logStr+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
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
    //Starts the gps service class. If no response is made then text will notify to try again.
    private void startService() {
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        startService(i);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connection == false) {
                    Log.i("contactcheck", "No connection");
                    warnText.setText("No connection? Try starting the journey again outdoors or when gps is stronger.");
                }
            }
        }, 5000);
    }

    private void openHome() {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }
    //Prompts user if they want to overwrite the data.
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btnStart.setEnabled(false);
                                    }
                                });

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
    //Checks if journey already exists by calling url to make a query.
    private void checkIfDayExists() {
        if(isWorking) {
            workerUrl = "toWork";
        } else {
            workerUrl = "toHome";
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody formbody = new FormBody.Builder().add("name",Activity2.logStr).build();

        Request request = new Request.Builder().url("https://albonoproj.herokuapp.com/"+workerUrl+"/"+getSessionDate()).post(formbody).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("existsCheck", "No connection");
                Toast.makeText(MainActivity.this,"Error connecting to server",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String s = response.body().string();

                if (s.equals("No_Results_For_Date")) {
                    Log.i("existsCheck", "match");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnStart.setEnabled(false);
                        }
                    });

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
                connection = false;
                checkIfDayExists();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askStopBat();
                btnStop.setEnabled(false);

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
    //When stop button is pressed, gps stops, data is pushed, picture is saved.
    private void closeProcess() {
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        stopService(i);
        pushToDatabase();
        Integer listInt = list.size()/2;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(list.get(listInt), 12));

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(@Nullable Bitmap bitmap) {
                        Bitmap bt = bitmap;
                        saveToInternalStorage(bt);
                    }
                });
            }
        }, 3000);
    }
    //Prompts remaining battery input.
    private void askStopBat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input car battery level");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

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
                closeProcess();
                dialog.cancel();
            }
        });

        builder.show();
    }
    //checks gps and storage permissions.
    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE

            }, 100);

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