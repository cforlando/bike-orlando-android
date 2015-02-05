package com.codefororlando.transport.display;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.v4.content.LocalBroadcastManager;

import com.codefororlando.transport.IBroadcasts;
import com.codefororlando.transport.MapsActivity;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.BikeRackClusterManager;
import com.codefororlando.transport.controller.IMapController;
import com.codefororlando.transport.data.BikeRackItem;
import com.codefororlando.transport.loader.FeatureCollectionLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import org.geojson.Feature;
import org.geojson.FeatureCollection;

public class BikeRacksFeature implements IDisplayableFeature, IBroadcasts {

    private BikeRackClusterManager bikeRackManager;
    private IMapController mapController;
    private GoogleMap map;
    private boolean isShown;
    private FeatureCollection featureCollection;

    @Override
    public int getGroupId() {
        return R.string.group_biking;
    }

    @Override
    public int getFeatureName() {
        return R.string.display_feature_racks;
    }

    @Override
    public void setController(IMapController mapController) {
        this.mapController = mapController;
        map = mapController.getMap();

        bikeRackManager = new BikeRackClusterManager(getContext(), map);
        bikeRackManager.getMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                BikeRackItem bikeRackItem = bikeRackManager.getBikeRackItem(marker);

                Intent intent = new Intent(ACTION_BIKE_MARKER_SELECTED);
                intent.putExtra(EXTRA_BIKE_RACK_ITEM, bikeRackItem);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                // Animate to the location manually as true is returned by the click to prevent the info window popup
                map.animateCamera(CameraUpdateFactory.newLatLng(bikeRackItem.getPosition()));

                // Return true to disable the info window popup. This inadvertently also disables animation to the position
                return true;
            }
        });

        FeatureCollectionLoader.load(this, R.raw.bike_parking);
    }

    @Override
    public void show() {
        isShown = true;
        updateVisibility();
    }

    @Override
    public void hide() {
        isShown = false;
        updateVisibility();
    }

    @Override
    public void destroy() {
        if (bikeRackManager != null) {
            bikeRackManager.clearItems();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return bikeRackManager.onMarkerClick(marker);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        bikeRackManager.onCameraChange(cameraPosition);
    }

    private void updateVisibility() {
        if (bikeRackManager != null) {
            if (featureCollection != null && isShown) {
                for (Feature feature : featureCollection.getFeatures()) {
                    bikeRackManager.addItem(new BikeRackItem(feature));
                }
            } else {
                bikeRackManager.clearItems();
            }
            bikeRackManager.cluster();
        }
    }

    @NonNull
    @Override
    public Context getContext() {
        return mapController.getContext();
    }

    @Override
    public void onFeatureCollectionLoaded(@RawRes int resourceId, FeatureCollection featureCollection) {
        switch (resourceId) {
            case R.raw.bike_parking:
                setBikeParking(featureCollection);
                break;
        }
    }

    private void setBikeParking(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
        updateVisibility();
    }

}
