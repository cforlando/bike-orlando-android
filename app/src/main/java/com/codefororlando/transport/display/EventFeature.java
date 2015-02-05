package com.codefororlando.transport.display;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.IMapController;
import com.codefororlando.transport.data.EventListings;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import org.geojson.FeatureCollection;

public class EventFeature implements IDisplayableFeature, EventListings.EventListingsListener {

    private EventListings eventListings;
    private IMapController mapController;
    private GoogleMap map;
    private boolean isShown;

    @Override
    public int getGroupId() {
        return R.string.group_events;
    }

    @Override
    public int getFeatureName() {
        return 0;
    }

    @Override
    public void setController(IMapController mapController) {
        this.mapController = mapController;
        map = mapController.getMap();

        EventListings.load(this);
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

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @NonNull
    @Override
    public Context getContext() {
        return mapController.getContext();
    }

    @Override
    public void onFeatureCollectionLoaded(@RawRes int resourceId, FeatureCollection featureCollection) {

    }

    @Override
    public void onEventListingsLoaded(@NonNull EventListings eventListings) {
        this.eventListings = eventListings;
    }

    @Override
    public void onEventListingsError(Exception e) {
        e.printStackTrace();
    }

    private void updateVisibility() {
    }

}
