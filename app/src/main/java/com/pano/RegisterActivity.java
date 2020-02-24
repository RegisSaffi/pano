package com.pano;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class RegisterActivity extends AppCompatActivity {

    FancyButton register;

    TextInputEditText fname, lname, email, phone, seats, plate, model;

    ProgressDialog progress;
    RadioGroup gender;

    String selectedGender = "Male";
    String phoneNumber;
    SwitchCompat driver;

    boolean canDrive = false;

    LinearLayout driveLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        gender = findViewById(R.id.gender);
        register = findViewById(R.id.btnRegister);

        driveLayout = findViewById(R.id.driveLayout);
        model = findViewById(R.id.model);
        seats = findViewById(R.id.seats);
        plate = findViewById(R.id.plate);

        driver = findViewById(R.id.driver);

        phoneNumber = getIntent().getStringExtra("phone");
        phone.setText(phoneNumber);

        gender.setOnCheckedChangeListener((radioGroup, i) -> {

            if (radioGroup.getId() == R.id.male) {
                selectedGender = "Male";
            } else {
                selectedGender = "Female";
            }
        });

        driver.setOnCheckedChangeListener((compoundButton, b) -> {

            canDrive = b;

            if (b) {
                driveLayout.setVisibility(View.VISIBLE);
            } else {
                driveLayout.setVisibility(View.GONE);
            }
        });

        register.setOnClickListener(v -> validate());

    }


    void validate() {
        if (fname.getText().toString().equals("")) {
            Toasty.error(getApplicationContext(), "First name is empty").show();
        } else if (lname.getText().toString().equals("")) {
            Toasty.error(getApplicationContext(), "Last name is empty").show();
        } else if (phone.getText().toString().equals("")) {
            Toasty.error(getApplicationContext(), "First name is empty").show();
        } else if (canDrive) {
            if (model.getText().toString().equals("")) {
                Toasty.error(getApplicationContext(), "Since you are a driver, enter model of your car.").show();
            } else if (seats.getText().toString().equals("")) {
                Toasty.error(getApplicationContext(), "Enter number of seats in your car").show();
            } else if (plate.getText().toString().equals("")) {
                Toasty.error(getApplicationContext(), "Enter plate number of your car").show();
            } else {
                register();
            }
        } else {

            register();
        }
    }


    void register() {
        progress = ProgressDialog.show(RegisterActivity.this, "", "Please wait...", false, false);

        CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
        Map<String, Object> user = new HashMap<>();
        user.put("first_name", fname.getText().toString());
        user.put("last_name", lname.getText().toString());
        user.put("phone", phoneNumber);
        user.put("type", canDrive ? "driver" : "passenger");

        if (canDrive) {
            Map<String, Object> userDriver = new HashMap<>();
            userDriver.put("model", model.getText().toString());
            userDriver.put("seats", seats.getText().toString());
            userDriver.put("plate", plate.getText().toString());
            userDriver.put("available_seats", 0);

            user.put("driver", userDriver);
        }

        DocumentReference id = userRef.document();

        userRef.document(id.getId()).set(user).addOnSuccessListener(aVoid -> {

            progress.cancel();

            Toasty.success(getApplicationContext(), "Registered successfully").show();

            new PrefManager(getApplicationContext()).setType(canDrive ? "driver" : "passenger");
            new PrefManager(getApplicationContext()).saveUser(id.getId(), fname.getText().toString() + " " + lname.getText().toString());
            new PrefManager(getApplicationContext()).setFirstTimeLaunch(false);

            Intent in;
            if (canDrive) {
                in = new Intent(getApplicationContext(), DriverActivity.class);
            } else {
                in = new Intent(getApplicationContext(), MainActivity.class);
            }

            in.putExtra("user", true);
            in.putExtra("phone", phoneNumber.replace("+", ""));

            startActivity(in);
            finish();
        }).addOnFailureListener(e -> Toasty.error(getApplicationContext(), "Registration failed, try again.").show())
                .addOnCompleteListener(task -> progress.cancel());

    }
}
