package com.Orlando.opensource.bikeorlando.controller;

import android.content.Context;

import com.Orlando.opensource.bikeorlando.R;
import com.Orlando.opensource.bikeorlando.data.BikeRackItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class BikeRackClusterManager extends ClusterManager<BikeRackItem> {

    public BikeRackClusterManager(Context context, GoogleMap map) {
        super(context, map);

        BikeRackClusterRenderer bikeRackClusterRenderer = new BikeRackClusterRenderer(context, map, this);
        setRenderer(bikeRackClusterRenderer);

    }

    private static class BikeRackClusterRenderer extends DefaultClusterRenderer<BikeRackItem> {

        private final BitmapDescriptor bitmapDescriptor;

        public BikeRackClusterRenderer(Context context, GoogleMap map,
                                       ClusterManager<BikeRackItem> clusterManager) {
            super(context, map, clusterManager);

            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bikerackpinpoint);
        }

        @Override
        protected void onBeforeClusterItemRendered(BikeRackItem item, MarkerOptions markerOptions) {
            markerOptions.icon(bitmapDescriptor);
        }

    }

}
