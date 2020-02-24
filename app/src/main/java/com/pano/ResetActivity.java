package com.pano;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class ResetActivity extends AppCompatActivity {

    FancyButton reset;
    EditText phone;

    ProgressDialog progress;
    SharedPreferences shared;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        shared = getSharedPreferences("inspector", 0);

        login = findViewById(R.id.login);
        phone = findViewById(R.id.phone);
        reset = findViewById(R.id.reset);

        if (shared.contains("logged")) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        reset.setOnClickListener(v -> {
            if (phone.getText().toString().equals("")) {

                Toasty.error(getApplicationContext(), "Enter phone number", Toast.LENGTH_LONG, true).show();

            }

        });

        login.setOnClickListener(v -> {
            finish();
        });
    }


    public class loader extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {

            progress = ProgressDialog.show(ResetActivity.this, "", "Logging in...", true, false);
            // TODO: Implement this method
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {

            String address = "https://loyalty.centrika.rw/api/api/apk/AgentLogin/";

            try {

                url = new URL(address);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL

                @SuppressLint("WrongThread") Uri.Builder builder = new Uri.Builder()

                        .appendQueryParameter("phone", phone.getText().toString());

                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            progress.dismiss();
            // Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

            if (result.equals("exception")) {
                Toasty.error(getApplicationContext(), "No internet", Toast.LENGTH_LONG, true).show();

            } else if (result.startsWith("{")) {
                try {
                    JSONObject obj = new JSONObject(result);
                    String id = obj.getString("ID");
                    String email = obj.getString("Email");
                    String FirstName = obj.getString("FirstName");
                    String LastName = obj.getString("LastName");
                    boolean isActive = obj.getBoolean("IsActive");
                    int balance = obj.getInt("WalletBalance");

                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("id", id);
                    editor.putInt("balance", balance);
                    editor.putString("name", FirstName + " " + LastName);
                    editor.putString("email", email);
                    editor.putBoolean("isactive", isActive);
                    editor.putBoolean("logged", true);
                    editor.apply();

                    Toasty.success(getApplicationContext(), "Success!", Toast.LENGTH_SHORT, true).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

                Toasty.error(getApplicationContext(), "Invalid email or password", Toast.LENGTH_LONG, true).show();
            }
        }
    }


}
