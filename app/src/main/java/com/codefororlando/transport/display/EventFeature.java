package com.codefororlando.transport.display;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.v4.content.LocalBroadcastManager;

import com.codefororlando.transport.IBroadcasts;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.bitmap.BitmapUtility;
import com.codefororlando.transport.controller.IMapController;
import com.codefororlando.transport.data.EventItem;
import com.codefororlando.transport.data.EventListings;
import com.codefororlando.transport.loader.FeatureCollectionLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.util.HashMap;
import java.util.Map;

public class EventFeature implements IDisplayableFeature, EventListings.EventListingsListener, IBroadcasts {

    private final Map<Marker, EventItem> eventItemMap;
    private EventListings eventListings;
    private IMapController mapController;
    private GoogleMap map;
    private boolean isShown;

    public EventFeature() {
        eventItemMap = new HashMap<>();
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
        map = mapController.getMap();

        EventListings.load(this);
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
        final EventItem eventItem = eventItemMap.get(marker);
        if (eventItem != null) {
            Intent intent = new Intent(ACTION_EVENT_MARKER_SELECTED);
            intent.putExtra(EXTRA_EVENT_ITEM, eventItem);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

            // Animate to the location manually as true is returned by the click to prevent the info window popup
            map.animateCamera(CameraUpdateFactory.newLatLng(eventItem.getPosition()));

            // Return true to disable the info window popup. This inadvertently also disables animation to the position
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
                final Bitmap bitmap = BitmapUtility.createBitmapWithCircleAndOverlay(getContext(), R.color.colorPrimaryPurple, R.drawable.eventpintpoint, 32);
                final BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

                for (Feature feature : featureCollection) {
                    final EventItem eventItem = new EventItem(feature);
                    final Marker marker = map.addMarker(new MarkerOptions().position(eventItem.getPosition())
                            .icon(bitmapDescriptor)
                            .visible(isShown));
                    eventItemMap.put(marker, eventItem);
                }
                updateVisibility();
                break;
        }
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
        for (Marker marker : eventItemMap.keySet()) {
            marker.setVisible(isShown);
        }
    }

}
