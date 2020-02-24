package com.pano.models;

import com.google.type.LatLng;

public class requestCard {

    public String name = "none";
    public String from = "none";
    public String to = "Route here";
    private String status = "none";
    private String id;

    public requestCard() {
    }

    public requestCard(String name, String from, String to, String status, String id) {

        this.name = name;
        this.from = from;
        this.to = to;
        this.status = status;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getStatus() {
        return status;
    }


}