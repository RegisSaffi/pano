package com.pano;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class PendingActivity extends AppCompatActivity {

    TextView timeTv, distanceTv, nameTv, costTv, modelTv, seatsTv, originTv, destinationTv, statusTv, desc;
    FancyButton cancel;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_dialog);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        nameTv = findViewById(R.id.name);
        modelTv = findViewById(R.id.model);
        costTv = findViewById(R.id.cost);
        timeTv = findViewById(R.id.time);
        distanceTv = findViewById(R.id.distance);
        seatsTv = findViewById(R.id.seats);
        cancel = findViewById(R.id.cancel);
        originTv = findViewById(R.id.originTv);
        destinationTv = findViewById(R.id.destinationTv);
        statusTv = findViewById(R.id.status);
        progressBar = findViewById(R.id.progress);
        desc = findViewById(R.id.desc);

        CollectionReference ref = FirebaseFirestore.getInstance().collection("requests");

        Query refQuery = ref.limit(1).orderBy("modified", Query.Direction.ASCENDING);

//        Query refQuery2=ref.whereEqualTo("status","accepted").limitToLast(1).orderBy("time");
//        Query refQuery3=ref.whereEqualTo("status","rejected").limitToLast(1).orderBy("time");

        listenPending(refQuery);
//        listenPending(refQuery);
//        listenRejected(refQuery3);

    }


    public void listenPending(Query refQuery) {
        progressBar.setVisibility(View.VISIBLE);
        cancel.setEnabled(false);

        refQuery.addSnapshotListener((queryDocumentSnapshots, e) -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                String name = document.getString("driver_name");
                String origin = document.getString("origin_name");
                String dest = document.getString("destination_name");
                String status = document.getString("status");
                String model = document.getString("model");

                Timestamp time = document.getTimestamp("time");
                String distance = document.getString("distance");
                String amount = document.getString("amount");
                String duration = document.getString("duration");


                originTv.setText(origin);
                destinationTv.setText(dest);
                timeTv.setText(duration);
                nameTv.setText(name);
                modelTv.setText(model);
                //  toolbar.setSubtitle(time.toDate().toString().substring(0,16));
                distanceTv.setText(distance);
                costTv.setText(amount);

                progressBar.setVisibility(View.GONE);
                cancel.setEnabled(true);

                listenSingleOne(document);
            }
        });
    }


    void listenSingleOne(DocumentSnapshot document) {

        DocumentReference ref = FirebaseFirestore.getInstance().collection("requests").document(document.getId());

        ref.addSnapshotListener((documentSnapshot, e) -> {

            String status = documentSnapshot.getString("status");

            if (status != null && status.equals("accepted")) {
                Toasty.success(PendingActivity.this, "Your request was accepted, we will notify you once driver reaches your pickup, don't close the app", Toasty.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    statusTv.setText(Html.fromHtml("<font color='green'>" + status + "</font>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    statusTv.setText(Html.fromHtml("<font color='green'>" + status + "</font>"));
                }


                cancel.setBorderColor(getResources().getColor(R.color.colorPrimary));
                cancel.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cancel.setText("Confirm delivery");

                cancel.setOnClickListener(v -> {
                    new AlertDialog.Builder(PendingActivity.this)
                            .setTitle("Confirm")
                            .setMessage("Confirm your journey complete and provide some feedback")
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                dialogInterface.cancel();
                            }).setPositiveButton("Confirm", (dialogInterface, i) -> {

                        //cancelling request

                        DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(document.getId());
                        Map<String, Object> d = new HashMap<>();
                        d.put("status", "confirmed");
                        d.put("modified", true);

                        reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                            Toasty.success(PendingActivity.this, "Journey confirmed, you can give us a feedback later.").show();
                            startActivity(new Intent(PendingActivity.this, MainActivity.class));
                            finish();

                        }).addOnFailureListener(e2 -> Toasty.error(PendingActivity.this, "Confirmation failed.").show());

                    }).show();
                });


            } else if (status != null && status.equals("pending")) {

                Toasty.info(PendingActivity.this, "Your request is pending", Toasty.LENGTH_LONG).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    statusTv.setText(Html.fromHtml("<font color='darkblue'>" + status + " request...</font>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    statusTv.setText(Html.fromHtml("<font color='darkblue'>" + status + " request ...</font>"));
                }

                cancel.setOnClickListener(v -> {
                    new AlertDialog.Builder(PendingActivity.this)
                            .setTitle("Confirm")
                            .setMessage("Would you like to cancel your request?")
                            .setNegativeButton("No", (dialogInterface, i) -> {
                                dialogInterface.cancel();
                            }).setPositiveButton("Yes", (dialogInterface, i) -> {

                        //cancelling request

                        DocumentReference reference = FirebaseFirestore.getInstance().collection("requests").document(document.getId());
                        Map<String, Object> d = new HashMap<>();
                        d.put("status", "cancelled");
                        d.put("modified", true);

                        reference.set(d, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                            Toasty.success(PendingActivity.this, "Request cancelled by you.").show();
                            startActivity(new Intent(PendingActivity.this, MainActivity.class));
                            finish();

                        }).addOnFailureListener(e2 -> Toasty.error(PendingActivity.this, "Cancel failed.").show());

                    }).show();
                });
            } else if (status != null && status.equals("confirmed")) {

            } else if (status != null && status.equals("cancelled")) {

            } else {
                startActivity(new Intent(PendingActivity.this, MainActivity.class));
                finish();
                Toasty.error(PendingActivity.this, "Your request might have been rejected by the driver.").show();
            }


        });

    }

}
