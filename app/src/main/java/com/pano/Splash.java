package com.pano;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class Splash extends AppCompatActivity {

    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        setTitle("");

        type = new PrefManager(this).getType();

        if (!new PrefManager(this).isFirstTimeLaunch()) {
            new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {

                    if (type.equals("driver")) {
                        startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }
            }.start();
        } else {

            startActivity(new Intent(getApplicationContext(), IntroActivity.class));
            finish();
        }

    }

}
