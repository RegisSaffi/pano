package com.pano;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pano.adapters.routesAdapter;
import com.pano.models.requestCard;

import java.util.ArrayList;
import java.util.List;


public class ChooseRouteActivity extends AppCompatActivity
        {


    RecyclerView recyclerView;
    routesAdapter routesAdapter;
    requestCard requestCard;
    List<requestCard> requestCards;
    Context mContext;
    SwipeRefreshLayout refresh;
    TextView info;
    SearchView search;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        info = findViewById(R.id.info);
        mContext = this;

        requestCards = new ArrayList<>();

        refresh = findViewById(R.id.swipe_layout);
        refresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        refresh.setOnRefreshListener(() -> {
            loadRoutes();
            refresh.postDelayed(() -> refresh.setRefreshing(false), 3000);
        });


        search=findViewById(R.id.search);
        recyclerView = findViewById(R.id.my_recycler_view);
        routesAdapter = new routesAdapter(requestCards, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(routesAdapter);

        setTitle("Choose your route");


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                routesAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                routesAdapter.getFilter().filter(newText);
                return true;
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int pos) {

                requestCard card = requestCards.get(pos);

                String name=card.getName();
                Intent in =new Intent();
                in.putExtra("name",name);

                setResult(RESULT_OK,in);
                finish();

            }

            @Override
            public void onLongClick(View view, int pos) {

            }
        }));

        loadRoutes();

    }



    public void loadRoutes() {

        info.setVisibility(View.VISIBLE);
        info.setText("Loading routes...");

        String myId = new PrefManager(this).getId();
        CollectionReference reference = FirebaseFirestore.getInstance().collection("routes");

        reference.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                info.setVisibility(View.GONE);
                requestCards.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String name = document.getString("name");
                    String origin = document.getString("zone");

                    requestCard = new requestCard(name, "", origin, "", document.getId());
                    requestCards.add(requestCard);
                    routesAdapter.notifyDataSetChanged();

                }
            }else{

                requestCards.clear();
                info.setVisibility(View.VISIBLE);
                info.setText("No available pending requests.");
                routesAdapter.notifyDataSetChanged();

            }


        }).addOnFailureListener(e -> {

            info.setVisibility(View.VISIBLE);
            info.setText("Failed to get requests");

        });



    }

}

