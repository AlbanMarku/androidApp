package com.example.myapplicationtestmapfrag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity2 extends AppCompatActivity {

    private Button button;
    private Button loginBtn;
    private Button resBtn;
    private TextView txtView;
    private String s;
    public static String logStr;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Electric Journey Companion");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        txtView = (TextView) findViewById(R.id.textViewLogin);
        button = (Button) findViewById(R.id.mapButton);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        Log.i("loginStr","LOADING NAMES");
        Log.i("loginStr",TEXT + "I got this back from last time");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginPop();
                    }
                });
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(logStr == null) {
                            loginPop();
                        } else {
                            s="m";
                            popWork(s);
                        }

                    }
                });
            }
        });

        resBtn = (Button) findViewById(R.id.activity2btn);
        resBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (logStr == null) {
                            loginPop();
                        } else {
                            s="r";
                            popWork(s);
                        }
                    }
                });
            }
        });
    }
    //prompts username input. Then checks if username already exists.
    public void loginPop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What is your username?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logStr = input.getText().toString();
                OkHttpClient okHttpClient = new OkHttpClient();

                RequestBody requestBody = new FormBody.Builder().add("name", logStr).build();

                Request request = new Request.Builder().url("https://albonoproj.herokuapp.com/login").post(requestBody).build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.i("results","I didn't find anything");
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String s = response.body().string();

                        if (s.equals("exists")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtView.setText("WELCOME BACK " + logStr);
                                }
                            });


                        } else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtView.setText("ACCOUNT CREATED. WELCOME " + logStr);
                                }
                            });

                        }


                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void openMapActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openResultsActivity() {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }
    //Asks if query or map will be in work or home mode.
    private void popWork(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity2.this);
                builder.setTitle("To Work or to Home?");

                String[] options = {"To Work", "To Home"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                MainActivity.isWorking = true;
                                break;
                            case 1:
                                MainActivity.isWorking = false;
                                break;
                        }
                        if(MainActivity.isWorking != null) {
                            if(s == "m") {
                                openMapActivity();
                            } else {
                                openResultsActivity();
                            }
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}