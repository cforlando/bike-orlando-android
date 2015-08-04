package com.codefororlando.transport.display;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.v4.content.LocalBroadcastManager;

import com.codefororlando.transport.IBroadcasts;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.ClusterManager;
import com.codefororlando.transport.controller.IMapController;
import com.codefororlando.transport.data.EventItem;
import com.codefororlando.transport.data.EventListings;
import com.codefororlando.transport.data.IClusterableParcelableItem;
import com.codefororlando.transport.loader.FeatureCollectionLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.util.LinkedList;
import java.util.List;

public class EventFeature implements IDisplayableFeature, EventListings.EventListingsListener, IBroadcasts {

    private final List<EventItem> eventItemList;
    private IMapController mapController;
    private ClusterManager clusterManager;
    private GoogleMap map;
    private boolean isShown, isAdded;

    public EventFeature() {
        eventItemList = new LinkedList<>();
    }

    @Override
    public int getGroupId() {
        return R.string.group_events;
    }

    @Override
    public int getFeatureName() {
        return R.string.display_feature_events_ticketed;
    }

    @Override
    public void setController(IMapController mapController) {
        this.mapController = mapController;
        clusterManager = mapController.getClusterManager();
        map = mapController.getMap();

        // Pre load the events such that they are hopefully already loaded when/if the user decides to view them
        EventListings.load(this);

        // Load the event locations
        FeatureCollectionLoader.load(this, R.raw.event_locations);
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
        final IClusterableParcelableItem clusterItem = clusterManager.getClusterItem(marker);
        final int idx = eventItemList.indexOf(clusterItem);

        // Find the bike rack item and retrieve it to prevent casting and instanceof checking. Effectively this is uncessary but more 'right'.
        if (idx > -1) {
            final EventItem eventItem = eventItemList.get(idx);
            final Intent intent = new Intent(ACTION_EVENT_MARKER_SELECTED);
            intent.putExtra(EXTRA_EVENT_ITEM, eventItem);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

            map.animateCamera(CameraUpdateFactory.newLatLng(eventItem.getPosition()));
            return true;
        }

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
        switch (resourceId) {
            case R.raw.event_locations:
                for (Feature feature : featureCollection) {
                    eventItemList.add(new EventItem(feature));
                }
                updateVisibility();
                break;
        }
    }

    @Override
    public void onEventListingsLoaded(@NonNull EventListings eventListings) {
    }

    @Override
    public void onEventListingsError(Exception e) {
        e.printStackTrace();
    }

    private void updateVisibility() {
        // Nothing to do if the items have not yet loaded
        if (eventItemList.isEmpty()) {
            return;
        }

        if (isShown) {
            clusterManager.addItems(new LinkedList<IClusterableParcelableItem>(eventItemList));
            isAdded = true;
        } else if (isAdded) {
            clusterManager.removeItems(eventItemList);
            isAdded = false;
        }

        // Recluster the points
        clusterManager.cluster();
    }

}
