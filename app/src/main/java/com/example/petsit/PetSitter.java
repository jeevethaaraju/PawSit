package com.example.petsit;

import com.google.android.gms.maps.model.LatLng;

public class PetSitter {
    public int id;
    public String serviceName;
    public String price;
    public String acceptPet;
    public double lat;
    public double lng;

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }
}
