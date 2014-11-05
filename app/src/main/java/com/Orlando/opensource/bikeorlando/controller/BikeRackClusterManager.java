package com.Orlando.opensource.bikeorlando.controller;

import android.content.Context;

import com.Orlando.opensource.bikeorlando.data.BikeRackItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.*;

public class BikeRackClusterManager extends ClusterManager<BikeRackItem> {

    public BikeRackClusterManager(Context context, GoogleMap map) {
        super(context, map);

        BikeRackClusterRenderer bikeRackClusterRenderer = new BikeRackClusterRenderer(context, map, this);
        setRenderer(bikeRackClusterRenderer);

    }

}
