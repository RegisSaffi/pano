package com.pano;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.pano.adapters.requestCardsAdapter;
import com.pano.models.requestCard;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.google.android.gms.common.api.GoogleApiClient.*;


public class DriverActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static double latitude;
    public static double longitude;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    AlertDialog.Builder gpsDialog;
    RecyclerView recyclerView;
    requestCardsAdapter cardBusAdapter;
    requestCard requestCard;
    List<requestCard> requestCards;
    Context mContext;
    SwipeRefreshLayout refresh;
    TextView info;
    SwitchCompat online;
    BottomSheetDialog dialog;
    ListenerRegistration listenMe;
    FloatingActionButton mapFab;
    TextView waitingTv, pendingTv;
    RelativeLayout waitingLayout, pendingLayout;

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

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View vvv = navigationView.getHeaderView(0);

        TextView number = vvv.findViewById(R.id.numbers);
        TextView names = vvv.findViewById(R.id.names);
        online = findViewById(R.id.online);

        online.setOnCheckedChangeListener((compoundButton, b) -> toggleOnline(b));

        number.setText(new PrefManager(this).getPhone());
        names.setText(new PrefManager(this).getName());

        info = findViewById(R.id.info);
        mContext = this;

        requestCards = new ArrayList<>();

        refresh = findViewById(R.id.swipe_layout);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        refresh.setOnRefreshListener(() -> {
            listenRequests();
            refresh.postDelayed(() -> refresh.setRefreshing(false), 3000);
        });

        waitingLayout = findViewById(R.id.waitingLayout);
        waitingTv = findViewById(R.id.waitingTv);
        pendingLayout = findViewById(R.id.pendingLayout);
        pendingTv = findViewById(R.id.pendingTv);

        waitingLayout.setOnClickListener(v -> {
            startActivity(new Intent(DriverActivity.this, DriverRequestsActivity.class));
        });

        recyclerView = findViewById(R.id.my_recycler_view);
        cardBusAdapter = new requestCardsAdapter(requestCards, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(cardBusAdapter);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();

            } else {
                //Request Location Permission
                checkLocationPermission();
            }

        } else {
            buildGoogleApiClient();

        }

        setTitle("Driver " + new PrefManager(this).getName());


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int pos) {

                requestCard card = requestCards.get(pos);

                Intent in = new Intent(DriverActivity.this, RequestsActivity.class);
                in.putExtra("origin", card.getFrom());
                in.putExtra("destination", card.getTo());
                in.putExtra("id", card.getId());

                startActivity(in);

            }

            @Override
            public void onLongClick(View view, int pos) {

            }
        }));

        mapFab = findViewById(R.id.fab);
        mapFab.setOnClickListener(v -> {
            Intent in = new Intent(DriverActivity.this, DriverMapActivity.class);
            in.putExtra("latitude", latitude);
            in.putExtra("longitude", longitude);
            startActivity(in);
        });

        listenMe();
    }

    void listenMe() {
        String myId = new PrefManager(this).getId();
        DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(myId);

        listenMe = reference.addSnapshotListener((documentSnapshot, e) -> {

            if (documentSnapshot != null) {
                String type = documentSnapshot.getString("type");
                if (type.equals("passenger")) {

                    stopListenMe();

                    new PrefManager(getApplicationContext()).setType("passenger");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
    }

    void stopListenMe() {
        listenMe.remove();
    }

    public void listenRequests() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading requests...");

        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("requests");
        Query query = reference.whereEqualTo("driver_id", myId).whereEqualTo("status", "pending");
        Query query2 = reference.whereEqualTo("driver_id", myId).whereEqualTo("status", "accepted");

        query.addSnapshotListener((queryDocumentSnapshots, e) -> {

            info.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                requestCards.clear();

                pendingTv.setText(queryDocumentSnapshots.size() + "");

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("requester_name");
                    String origin = document.getString("origin_name");
                    String dest = document.getString("destination_name");
                    String status = document.getString("status");

                    requestCard = new requestCard(name, origin, dest, status, document.getId());
                    requestCards.add(requestCard);

                }

                if (e != null && e.getCode() == FirebaseFirestoreException.Code.CANCELLED) {
                    info.setVisibility(View.VISIBLE);
                    info.setText("Failed to get requests");
                }

            } else {

                pendingTv.setText(0 + "");
                requestCards.clear();
                info.setVisibility(View.VISIBLE);
                info.setText("No available pending requests.");
                cardBusAdapter.notifyDataSetChanged();

            }

            cardBusAdapter.notifyDataSetChanged();
        });

        query2.addSnapshotListener((queryDocumentSnapshots, e) -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                waitingTv.setText(queryDocumentSnapshots.size() + "");
            } else {
                waitingTv.setText(0 + "");
            }
        });


        FirebaseFirestore.getInstance().collection("users").document(new PrefManager(getApplicationContext()).getId()).addSnapshotListener((documentSnapshot, e) -> {
            try {

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    boolean on = (boolean) documentSnapshot.get("online");
                    online.setChecked(on);
                }

            } catch (Exception e2) {
                e2.printStackTrace();
            }
        });
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
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
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


        Map<String, Object> loc = new HashMap<>();
        loc.put("coordinates", new LatLng(location.getLatitude(), location.getLongitude()));
        loc.put("time", location.getTime());


        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("users");
        GeoFirestore geoFirestore = new GeoFirestore(collectionRef);

        geoFirestore.setLocation(new PrefManager(getApplicationContext()).getId(), new GeoPoint(location.getLatitude(), location.getLongitude()));

        FirebaseFirestore.getInstance().collection("users").document(new PrefManager(getApplicationContext()).getId()).set(loc, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    void toggleOnline(boolean online) {

        Map<String, Object> on = new HashMap<>();
        on.put("online", online);

        FirebaseFirestore.getInstance().collection("users").document(new PrefManager(getApplicationContext()).getId()).set(on, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if (online) {
                    Toasty.success(getApplicationContext(), "You are online now").show();
                } else {
                    Toasty.error(getApplicationContext(), "You are offline").show();

                }

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
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(DriverActivity.this,
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        listenRequests();
        if (!isLocationEnabled(this)) {
            showSettingsAlert();
        }
        super.onResume();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_list:
                break;
            case R.id.nav_profile:
                startActivity(new Intent(DriverActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_accept:
                startActivity(new Intent(DriverActivity.this, DriverRequestsActivity.class));
                break;
            case R.id.nav_logout:
                new AlertDialog.Builder(DriverActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are u sure you want to logout of your account?")
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {
                            dialogInterface.cancel();
                        }).setPositiveButton("Logout", (dialogInterface, i) -> {
                    clearAppData();
                    new PrefManager(DriverActivity.this).logout();
                    startActivity(new Intent(DriverActivity.this, LoginActivity.class));
                    finish();

                }).show();
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

