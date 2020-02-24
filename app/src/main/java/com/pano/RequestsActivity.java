package com.pano;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.pano.adapters.requestCardsAdapter;
import com.pano.models.requestCard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class RequestsActivity extends AppCompatActivity {

    TextView distanceTv, timeTv, nameTv, phoneTv, costTv, seatsTv, originTv, destinationTv;

    FancyButton accept, reject;

    MaterialButton call;

    String requestId, origin, destination;
    ProgressBar progress;
    Toolbar toolbar;

    String phn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }


        costTv = findViewById(R.id.cost);
        distanceTv = findViewById(R.id.distance);
        nameTv = findViewById(R.id.name);
        phoneTv = findViewById(R.id.phone);
        seatsTv = findViewById(R.id.seats);
        originTv = findViewById(R.id.originTv);
        destinationTv = findViewById(R.id.destinationTv);
        timeTv = findViewById(R.id.time);

        accept = findViewById(R.id.accept);
        reject = findViewById(R.id.reject);
        call = findViewById(R.id.call);
        progress = findViewById(R.id.progress);

        requestId = getIntent().getStringExtra("id");
        origin = getIntent().getStringExtra("origin");
        destination = getIntent().getStringExtra("destination");

        originTv.setText(origin);
        destinationTv.setText(destination);

        setTitle("Request details");

        call.setOnClickListener(v -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        return;
                    }
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phn));
                startActivity(callIntent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        accept.setOnClickListener(v -> {

            accept.setEnabled(false);
            progress.setVisibility(View.VISIBLE);

            DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(requestId);
            Map<String, Object> d = new HashMap<>();
            d.put("status", "accepted");

            reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                Toasty.success(RequestsActivity.this, "User request accepted,we'll notify user once you reach in his/her area.").show();
                finish();
            }).addOnFailureListener(e -> {
                        Toasty.error(RequestsActivity.this, "Acceptance failed.").show();
                        accept.setEnabled(true);
                        progress.setVisibility(View.GONE);

                    }
            );

        });

        reject.setOnClickListener(v -> {

            reject.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(requestId);
            Map<String, Object> d = new HashMap<>();
            d.put("status", "rejected");
            d.put("modified", true);

            reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                Toasty.info(RequestsActivity.this, "User request rejected").show();
                finish();
            }).addOnFailureListener(e -> {
                        accept.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        Toasty.error(RequestsActivity.this, "Rejection failed.").show();
                    }
            );

        });
        getRequestDetails();
    }

    void getRequestDetails() {
        progress.setVisibility(View.VISIBLE);
        call.setEnabled(false);
        accept.setEnabled(false);
        reject.setEnabled(false);

        DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(requestId);
        reference.get().addOnSuccessListener(document -> {

            String name = document.getString("requester_name");
            String origin = document.getString("origin_name");
            String dest = document.getString("destination_name");
            String status = document.getString("status");
            String phone = document.getString("requester_phone");

            Timestamp time = document.getTimestamp("time");
            String distance = document.getString("distance");
            String amount = document.getString("amount");
            String duration = document.getString("duration");

            timeTv.setText(duration);
            nameTv.setText(name);
            phoneTv.setText(phone);
            toolbar.setSubtitle(time.toDate().toString().substring(0, 16));
            distanceTv.setText(distance);
            costTv.setText(amount);

            progress.setVisibility(View.GONE);
            call.setEnabled(true);
            accept.setEnabled(true);
            reject.setEnabled(true);

        }).addOnFailureListener(e -> {
            progress.setVisibility(View.GONE);
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toasty.warning(this, "Please enable call permission", Toast.LENGTH_SHORT).show();
        }
    }
}
