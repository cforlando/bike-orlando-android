package com.codefororlando.transport.controller;

import android.content.Context;

import com.codefororlando.transport.data.BikeRackItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

public class BikeRackClusterManager extends ClusterManager<BikeRackItem> {

    private final BikeRackClusterRenderer bikeRackClusterRenderer;

    public BikeRackClusterManager(Context context, GoogleMap map) {
        super(context, map);

        bikeRackClusterRenderer = new BikeRackClusterRenderer(context, map, this);
        setRenderer(bikeRackClusterRenderer);

    }

    public BikeRackItem getBikeRackItem(Marker marker) {
        return bikeRackClusterRenderer.getClusterItem(marker);
    }

}
