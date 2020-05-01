package com.pano;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.pano.interfaces.LocationUpdater;
import com.pano.services.GPSTracker;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

public class RideActivity extends AppCompatActivity implements OnMapReadyCallback, LocationUpdater, ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static String API;
    public static String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
    public static String refurl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJN1t_tDeuEmsRUsoyG83frY4&key=YOUR_API_KEY";
    public static String directionUrl = "https://maps.googleapis.com/maps/api/directions/json?";
    public static double latitude;
    public static double longitude;
    public int count2 = 0;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Context mContext;
    ImageView toggle;
    Marker marker;
    BottomSheetDialog dialog;
    BottomSheetBehavior sheetBehavior;
    double originLatitude, originLongitude, destinationLatitude, destinationLongitude;
    String destinationName, destinationId, originName;
    List<LatLng> currentPoly;
    Polyline polyline1;
    TextView timeTv, distanceTv, nameTv, costTv, modelTv, seatsTv,routeTv;
    ProgressDialog progressDialog;
    DocumentSnapshot driverDocument;
    FancyButton requestBtn;
    private GoogleMap mMap;
    private GPSTracker myService;
    private boolean bound = false;
    /**
     * Callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            GPSTracker.LocalBinder binder = (GPSTracker.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(RideActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            return lm.isLocationEnabled();

        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        API = getString(R.string.google_api_key);

        Bundle bundle = getIntent().getExtras();

        originLatitude = bundle.getDouble("originLatitude", latitude);
        originLongitude = bundle.getDouble("originLongitude", longitude);
        destinationLatitude = bundle.getDouble("destinationLatitude");
        destinationLongitude = bundle.getDouble("destinationLongitude");
        destinationId = bundle.getString("destinationId");
        destinationName = bundle.getString("destinationName");
        originName = bundle.getString("originName");
        originName = getCompleteAddressString(originLatitude, originLongitude);

        com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        // Create a new Places client instance
        PlacesClient placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        nameTv = findViewById(R.id.name);
        modelTv = findViewById(R.id.model);
        costTv = findViewById(R.id.cost);
        timeTv = findViewById(R.id.time);
        distanceTv = findViewById(R.id.distance);
        seatsTv = findViewById(R.id.seats);
        requestBtn = findViewById(R.id.request);
        routeTv=findViewById(R.id.route);

        toggle = findViewById(R.id.toggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setTitle(destinationName);

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {

                        toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_down_black_24dp, getTheme()));

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {

                        toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_up_black_24dp, getTheme()));

                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mContext = this;

        toggle.setOnClickListener(v -> toggleBottomSheet());

        String dirUrl = directionUrl + "origin=" + originLatitude + "," + originLongitude + "&destination=" + destinationLatitude + "," + destinationLongitude + "&key=" + API;

        getDirection gd = new getDirection();
        gd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dirUrl);

        requestBtn.setOnClickListener(view -> {

            requestBtn.setEnabled(false);
            Map<String, Object> request = new HashMap<>();
            request.put("requester_id", new PrefManager(getApplicationContext()).getId());
            request.put("requester_phone", new PrefManager(getApplicationContext()).getPhone());
            request.put("requester_name", new PrefManager(getApplicationContext()).getName());
            request.put("requester_origin", new LatLng(originLatitude, originLongitude));
            request.put("requester_destination", new LatLng(destinationLatitude, destinationLongitude));
            request.put("origin_name", originName);
            request.put("destination_name", destinationName);
            request.put("time", new Date());
            request.put("model", modelTv.getText().toString());
            request.put("duration", timeTv.getText().toString());
            request.put("distance", distanceTv.getText());
            request.put("amount", costTv.getText());
            request.put("driver_name", driverDocument.getString("first_name") + " " + driverDocument.getString("last_name"));
            request.put("driver_id", driverDocument.getId());
            request.put("modified", false);
            request.put("status", "pending");

            FirebaseFirestore.getInstance().collection("requests").document().set(request).addOnSuccessListener(aVoid -> {

                Toasty.success(getApplicationContext(), "Ride requested, you will be notified once request accepted.").show();
                finish();

            }).addOnFailureListener(e -> {
                        Toasty.error(getApplicationContext(), "Error requesting ride,try again later").show();
                        requestBtn.setEnabled(true);
                    }
            );
        });

        getNearby();
    }

    void toggleBottomSheet() {

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_down_black_24dp, getTheme()));

            return;
        }

        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        toggle.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.ic_keyboard_arrow_up_black_24dp, getTheme()));

    }

    void getNearby() {

        requestBtn.setEnabled(false);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);

        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("users");
        Query query = collectionRef.whereEqualTo("online", true);

        GeoFirestore geoFirestore = new GeoFirestore(collectionRef);

        GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(originLatitude, originLongitude), 1);

        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDocumentEntered(@NotNull DocumentSnapshot documentSnapshot, @NotNull GeoPoint geoPoint) {

                driverDocument = documentSnapshot;

                requestBtn.setEnabled(true);
                findViewById(R.id.progress).setVisibility(View.GONE);
                String fname = documentSnapshot.getString("first_name");
                String lname = documentSnapshot.getString("last_name");

                Map<String, Object> driv = (Map<String, Object>) documentSnapshot.get("driver");

                nameTv.setText(fname + " " + lname);
                modelTv.setText((String) driv.get("model"));
                routeTv.setText((String) driv.get("routes"));

                long sts = (long) driv.get("available_seats");
                seatsTv.setText(sts + "");
                findViewById(R.id.progress).setVisibility(View.GONE);

                geoQuery.removeAllListeners();

            }

            @Override
            public void onDocumentExited(@NotNull DocumentSnapshot documentSnapshot) {


            }

            @Override
            public void onDocumentMoved(@NotNull DocumentSnapshot documentSnapshot, @NotNull GeoPoint geoPoint) {

            }

            @Override
            public void onDocumentChanged(@NotNull DocumentSnapshot documentSnapshot, @NotNull GeoPoint geoPoint) {

            }

            @Override
            public void onGeoQueryReady() {

                findViewById(R.id.progress).setVisibility(View.GONE);
                if (driverDocument == null) {
                    new AlertDialog.Builder(RideActivity.this)
                            .setTitle("No nearby cars")
                            .setMessage("There are no nearby cars available at the moment.")
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {

                                finish();

                            }).setCancelable(true).show();
                }
            }

            @Override
            public void onGeoQueryError(@NotNull Exception e) {

                new AlertDialog.Builder(RideActivity.this)
                        .setTitle("Error")
                        .setMessage("Error occurred while loading nearby cars,try again later.")
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {

                            finish();

                        }).setCancelable(false).show();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationClickListener(location -> {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.snippet(originName.equals("none") ? getCompleteAddressString(originLatitude, originLongitude) : originName);
            markerOptions.title("Pickup");

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            if (marker != null) {
                marker.remove();
            }

            marker = mMap.addMarker(markerOptions);

        });

        mMap.setOnMarkerClickListener(marker -> {

            marker.showInfoWindow();
            return true;
        });


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }

        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.snippet("me");
        markerOptions.title(new PrefManager(getApplicationContext()).getName());

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if (marker != null) {
            marker.remove();
        }


        Map<String, Object> loc = new HashMap<>();
        loc.put("coordinates", new LatLng(location.getLatitude(), location.getLongitude()));
        loc.put("time", location.getTime());


    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(RideActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, GPSTracker.class);
        try {
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////


    // Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    public void decodeDirection(String json) {
        try {
            JSONObject full = new JSONObject(json);

            JSONArray routesArray = full.getJSONArray("routes");
            JSONObject routes = routesArray.getJSONObject(0);
            JSONObject overviewPoly = routes.getJSONObject("overview_polyline");

            JSONArray legs = routes.getJSONArray("legs");
            JSONObject legs0 = legs.getJSONObject(0);
            JSONObject distance = legs0.getJSONObject("distance");
            JSONObject duration = legs0.getJSONObject("duration");

            String distancetxt = distance.getString("text");
            String durationtxt = duration.getString("text");

            distanceTv.setText(distancetxt);
            timeTv.setText(durationtxt);

            String encodedString = overviewPoly.getString("points");
            List<LatLng> polies = decodePoly(encodedString);

            currentPoly = polies;

            PolylineOptions options = new PolylineOptions();
            for (int i = 0; i < polies.size(); i++) {
                options.add(new LatLng(polies.get(i).latitude, polies.get(i).longitude));

            }
            options.color(getResources().getColor(R.color.colorPrimary)).width(8).geodesic(true);
            if (polyline1 != null) {
                polyline1.remove();
            }
            polyline1 = mMap.addPolyline(options);

            if (count2 == 0) {
                zoomRoute(mMap, polies);
                count2++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void zoomRoute(final GoogleMap googleMap, List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        final int routePadding = 100;
        final LatLngBounds latLngBounds = boundsBuilder.build();

        LatLng latLng = new LatLng(destinationLatitude, destinationLongitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.snippet("Your destination");
        markerOptions.title(destinationName);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(markerOptions);


        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));

        CameraPosition camera = new CameraPosition.Builder()
                .target(new LatLng(destinationLatitude, destinationLongitude))
                .bearing(90)
                .zoom(20)
                .tilt(45)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));


        new CountDownTimer(4000, 100) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));

            }
        }.start();

    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current", strReturnedAddress.toString());
            } else {
                Log.w("My Current", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current", "Canont get Address!");
        }
        return strAdd;
    }

    @Override
    public void onLocationUpdated(Location location) {


    }

    public class getDirection extends AsyncTask<String, Void, String> {
        String server_response = "none";

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(RideActivity.this, "", "Please wait...", true, false);
            // TODO: Implement this method
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = readStream(urlConnection.getInputStream());
                    Log.e("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.cancel();

            if (!(server_response.equals(null) || server_response.equals("none"))) {
                decodeDirection(server_response);
                //hours.setText(server_response);
            }
        }
    }
}
