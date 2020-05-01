package com.pano;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.mancj.materialsearchbar.MaterialSearchBar;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.pano.interfaces.LocationUpdater;
import com.pano.models.requestCard;
import com.pano.services.GPSTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationUpdater, GoogleMap.OnMarkerClickListener, ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static double latitude;
    public static double longitude;
    ListenerRegistration listenMe;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    MaterialSearchBar searchbar;

    Context mContext;
    FrameLayout searcher;

    EditText origin, destination;

    Place currentOrigin, currentDestination;

    AlertDialog.Builder gpsDialog;
    String myPhone, myId;
    Location currentLocation;
    Marker marker;
    BottomSheetDialog dialog;
    Map<String, Marker> markers;
    boolean animated = false;
    Handler handler;
    Runnable runnable;
    FancyButton search;
    MaterialButton close;
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
            myService.setCallbacks(MainActivity.this); // register
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
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);

        com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        // Create a new Places client instance
        PlacesClient placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        fab.setOnClickListener(view -> {
            Snackbar.make(view, "No notification", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show();

            // startActivity(new Intent(MainActivity.this,DriverActivity.class));

        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        searchbar = findViewById(R.id.searchbar);
        searcher = findViewById(R.id.searcher);
        origin = findViewById(R.id.origin);
        destination = findViewById(R.id.destination);
        search = findViewById(R.id.btnSearch);
        close = findViewById(R.id.close);


        myPhone = new PrefManager(this).getPhone();
        myId = new PrefManager(this).getId();

        searchbar.setOnClickListener(v -> {
            searcher.setVisibility(View.VISIBLE);
            //  String add=getCompleteAddressString(latitude,longitude);
            origin.setText("Your location");

        });

        findViewById(R.id.mt_nav).setOnClickListener(v -> {

            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        markers = new HashMap<>();

        navigationView.setNavigationItemSelectedListener(this);
        View vvv = navigationView.getHeaderView(0);

        TextView number = vvv.findViewById(R.id.numbers);
        TextView names = vvv.findViewById(R.id.names);

        number.setText(new PrefManager(this).getPhone());
        names.setText(new PrefManager(this).getName());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mContext = this;


        origin.setOnClickListener(vv -> {
            List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, Place.Field.LAT_LNG);
// Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .setHint("Search location")
                    .setCountry("RW")
                    .build(this);

            startActivityForResult(intent, 0);

        });


        close.setOnClickListener(v -> searcher.setVisibility(View.GONE));

        destination.setOnClickListener(vv -> {
            List<com.google.android.libraries.places.api.model.Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, Place.Field.LAT_LNG);
// Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);

            startActivityForResult(intent, 1);
        });


        search.setOnClickListener(view -> {

            if (origin.getText().toString().equals("")) {
                Toasty.error(getApplicationContext(), "Enter valid origin").show();
            } else if (destination.getText().toString().equals("")) {
                Toasty.error(getApplicationContext(), "Enter valid destination").show();
            } else {

                Intent in = new Intent(MainActivity.this, RideActivity.class);

                Bundle bundle = new Bundle();
                if (currentOrigin == null) {

                    bundle.putDouble("originLatitude", latitude);
                    bundle.putDouble("originLongitude", longitude);
                    bundle.putString("originName", "none");

                } else {
                    bundle.putDouble("originLatitude", currentOrigin.getLatLng().latitude);
                    bundle.putDouble("originLongitude", currentOrigin.getLatLng().longitude);
                    bundle.putString("originName", currentOrigin.getName());
                }
                bundle.putDouble("destinationLatitude", currentDestination.getLatLng().latitude);
                bundle.putDouble("destinationLongitude", currentDestination.getLatLng().longitude);

                bundle.putString("destinationName", currentDestination.getName());
                bundle.putString("destinationId", currentDestination.getId());


                in.putExtras(bundle);

                startActivity(in);
            }

        });

        listenLocationOpen();
        listenDrivers();
        listenMe();
    }

    void listenLocationOpen() {
        runnable = () -> {

            if (!isLocationEnabled(getApplicationContext())) {
                showSettingsAlert();
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable, 5000);

    }

    void listenMe() {
        String myId = new PrefManager(this).getId();
        DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(myId);

        listenMe = reference.addSnapshotListener((documentSnapshot, e) -> {

            if (documentSnapshot != null) {
                String type = documentSnapshot.getString("type");
                if (type.equals("driver")) {

                    stopListenMe();

                    new PrefManager(getApplicationContext()).setType("driver");
                    startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                    finish();
                }
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    void stopListenMe() {
        listenMe.remove();
    }

    public void listenDrivers() {
        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("users");

        Query q = reference.whereEqualTo("online", true);

        q.addSnapshotListener((queryDocumentSnapshots, e) -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                mMap.clear();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name1 = (String) document.get("first_name");
                    String name2 = (String) document.get("last_name");
                    String id = (String) document.getId();
                    boolean on = (boolean) document.get("online");

                    Map<String, Object> driv = (Map<String, Object>) document.get("driver");


                    Map<String, Double> loc1 = (Map<String, Double>) document.get("coordinates");
                    LatLng loc = new LatLng(loc1.get("latitude"), loc1.get("longitude"));

                    LatLng latLng = new LatLng(loc.latitude, loc.longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(name1 + " " + name2);

                    long sts = (long) driv.get("available_seats");
                    markerOptions.snippet(sts+" Seats available");

                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));


                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Log.d("TAG", "New driver: " + dc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.d("TAg", "Modified city: " + dc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.d("Tag", "Removed city: " + dc.getDocument().getData());
                                break;
                        }
                    }

                    if (markers.containsKey(id)) {
                        Marker m = markers.get(id);
                        m.remove();
                    }

                    Marker otherplayermarker = mMap.addMarker(markerOptions);
                    markers.put(id, otherplayermarker);

                }

            }
        });
    }

    public void listenAccepted() {

        CollectionReference ref = FirebaseFirestore.getInstance().collection("requests");
        Query refQuery = ref.orderBy("modified", Query.Direction.ASCENDING);

        refQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                String status = document.getString("status");

                DocumentChange dc = queryDocumentSnapshots.getDocumentChanges().get(0);
                switch (dc.getType()) {
                    case ADDED:

                        if (status.equals("pending")) {
                            startActivity(new Intent(MainActivity.this, PendingActivity.class));
                            finish();
                        } else if (status.equals("accepted")) {
                            startActivity(new Intent(MainActivity.this, PendingActivity.class));
                            finish();
                        }
                        Log.d("TAG", "New driver: " + dc.getDocument().getData());
                        break;
                    case MODIFIED:
                        Log.d("TAg", "Modified city: " + dc.getDocument().getData());
                        if (status.equals("pending")) {
                            startActivity(new Intent(MainActivity.this, PendingActivity.class));
                            finish();
                        } else if (status.equals("accepted")) {
                            startActivity(new Intent(MainActivity.this, PendingActivity.class));
                            finish();
                        }
                        break;
                    case REMOVED:
                        Log.d("Tag", "Removed city: " + dc.getDocument().getData());
                        break;
                }


            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_account:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_history:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;
            case R.id.nav_logout:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are u sure you want to logout of your account?")
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {
                            dialogInterface.cancel();
                        }).setPositiveButton("Logout", (dialogInterface, i) -> {
                    clearAppData();
                    new PrefManager(MainActivity.this).logout();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();


                }).show();
                break;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnMyLocationClickListener(location -> {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.snippet("me");
            markerOptions.title(new PrefManager(getApplicationContext()).getName());

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            if (marker != null) {
                marker.remove();
            }

            marker = mMap.addMarker(markerOptions);

            if (!animated) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                animated = true;
            }
        });


        mMap.setOnMarkerClickListener(marker -> {

            marker.showInfoWindow();
            return true;
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        marker = mMap.addMarker(markerOptions);

        if (!animated) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            animated = true;
        }

        Map<String, Object> loc = new HashMap<>();
        loc.put("coordinates", new LatLng(location.getLatitude(), location.getLongitude()));
        loc.put("time", location.getTime());

        FirebaseFirestore.getInstance().collection("users").document(myId).set(loc, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });


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
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
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
    public boolean onMarkerClick(Marker marker) {


        return true;
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searcher.getVisibility() == View.VISIBLE) {
            searcher.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 || requestCode == 1) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (requestCode == 0) {
                    origin.setText(place.getName());
                    currentOrigin = place;
                } else if (requestCode == 1) {
                    destination.setText(place.getName());
                    currentDestination = place;
                }

                Log.i("NAME", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("MSG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    protected void onResume() {

        listenAccepted();

        if (!isLocationEnabled(this)) {
            showSettingsAlert();
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            Intent intent = new Intent(this, GPSTracker.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSettingsAlert() {

        gpsDialog = new AlertDialog.Builder(mContext);

        gpsDialog.setOnCancelListener(dialogInterface -> {

            if (!isLocationEnabled(mContext)) {
                showSettingsAlert();
            }
        });
        // Setting Dialog Title
        gpsDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        gpsDialog.setMessage("GPS is not enabled. Turn on GPS first for the app to function.");

        // On pressing Settings button
        gpsDialog.setPositiveButton("Turn on", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });

        gpsDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        // Showing Alert Message
        gpsDialog.show();
    }

    @Override
    public void onLocationUpdated(Location location) {

        currentLocation = location;


    }

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
