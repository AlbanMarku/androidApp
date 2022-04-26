package com.example.myapplicationtestmapfrag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private TextView txt;
    private Button btn;
    private ImageView iv;
    private String dayVal;
    private String workUrl;
    private String curDatStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results2);

        setTitle("Electric Journey Companion");
        txt = (TextView) findViewById(R.id.txtRes);
        btn = (Button) findViewById(R.id.dayBtn);
        iv = (ImageView) findViewById(R.id.imageViewMap);
        if(MainActivity.isWorking == true) {
            workUrl = "toWork";
        } else {
            workUrl = "toHome";
        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dp = new DatePickerFragment();
                dp.show(getSupportFragmentManager(),"date picker");
            }
        });

    }

    @Override//Sets date based on calander pop up.
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, i);
        c.set(Calendar.MONTH, i1);
        c.set(Calendar.DAY_OF_MONTH,i2);
        curDatStr = DateFormat.getDateInstance().format(c.getTime());
        curDatStr = curDatStr.replace(" ","");
        curDatStr = curDatStr.replace(",","");
        displayResults(curDatStr);
    }


    private void displayResults(String s) {//calls url to fetch results for given date and fetches image from storage.

        dayVal = s;

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody formbody = new FormBody.Builder().add("name", Activity2.logStr).build();
        Request request = new Request.Builder().url("https://albonoproj.herokuapp.com/"+workUrl+"/"+s).post(formbody).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("results","I didn't find anything");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                txt.setText(response.body().string());
                Log.i("eventRecord","There have been " + String.valueOf(MainActivity.events));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iv.setImageDrawable(null);
                    }
                });
                loadImageFromStorage("/data/data/com.example.myapplicationtestmapfrag/app_imageDir");
            }
        });
    }

    private void loadImageFromStorage(String path)//searches storage for journey.
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Stuff that updates the UI
                try {
                    File f=new File(path, dayVal+MainActivity.isWorking.toString()+Activity2.logStr+".jpg");
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