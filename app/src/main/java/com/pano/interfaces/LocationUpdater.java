package com.pano.interfaces;

import android.location.Location;

import com.google.type.LatLng;

public interface LocationUpdater {
    void onLocationUpdated(Location location);
}
