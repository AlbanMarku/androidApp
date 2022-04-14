package com.example.myapplicationtestmapfrag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultsActivity extends AppCompatActivity {

    private TextView txt;
    private Button btn;
    private ImageView iv;
    private String dayVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results2);

        txt = (TextView) findViewById(R.id.txtRes);
        btn = (Button) findViewById(R.id.dayBtn);
        iv  = (ImageView) findViewById(R.id.imageViewMap);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUp();
            }
        });

    }

    private void popUp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultsActivity.this);
                builder.setTitle("Choose a day");

                String[] animals = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                builder.setItems(animals, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                dayVal = "monday";
                                break;
                            case 1:
                                dayVal = "tuesday";
                                break;
                            case 2:
                                dayVal = "wednesday";
                                break;
                            case 3:
                                dayVal = "thursday";
                                break;
                            case 4:
                                dayVal = "friday";
                                break;
                            case 5:
                                dayVal = "saturday";
                                break;
                            case 6:
                                dayVal = "sunday";
                                break;
                        }
                        if(dayVal != null) {
                            displayResults();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private void displayResults() {


        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://albonoproj.herokuapp.com/results/"+dayVal).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("results","I didn't find anything");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                txt.setText(response.body().string());
                Log.i("eventRecord","There have been " + String.valueOf(MainActivity.events));
                loadImageFromStorage("/data/data/com.example.myapplicationtestmapfrag/app_imageDir");
            }
        });
    }

    private void loadImageFromStorage(String path)
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Stuff that updates the UI
                try {
                    File f=new File(path, "map.jpg");
                    Log.i("wheretho", "IIIIIN");
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                    iv.setImageBitmap(b);
                }
                catch (FileNotFoundException e)
                {
                    Log.i("wheretho", "I didn't find anything");
                    e.printStackTrace();
                }
            }
        });
    }


}