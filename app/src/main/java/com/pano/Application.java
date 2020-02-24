package com.pano;

import android.content.Context;
import android.location.Location;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.google.type.LatLng;

public class Application extends android.app.Application {

    public static Location currentLocation;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public Application() {

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
