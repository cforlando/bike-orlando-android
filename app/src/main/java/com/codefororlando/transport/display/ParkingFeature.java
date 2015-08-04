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
import com.codefororlando.transport.data.IClusterableParcelableItem;
import com.codefororlando.transport.data.ParkingItem;
import com.codefororlando.transport.loader.FeatureCollectionLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.util.LinkedList;
import java.util.List;

public class ParkingFeature implements IDisplayableFeature, IBroadcasts {

    private final List<ParkingItem> parkingItemList;
    private IMapController mapController;
    private ClusterManager clusterManager;
    private GoogleMap map;
    private boolean isShown, isAdded;

    public ParkingFeature() {
        parkingItemList = new LinkedList<>();
    }

    @Override
    public int getGroupId() {
        return R.string.group_driving;
    }

    @Override
    public int getFeatureName() {
        return R.string.display_feature_parking;
    }

    @Override
    public void setController(IMapController mapController) {
        this.mapController = mapController;
        clusterManager = mapController.getClusterManager();
        map = mapController.getMap();

        FeatureCollectionLoader.load(this, R.raw.parking_combined_min);
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
        final int idx = parkingItemList.indexOf(clusterItem);

        // Find the bike rack item and retrieve it to prevent casting and instanceof checking. Effectively this is uncessary but more 'right'.
        if (idx > -1) {
            final ParkingItem parkingItem = parkingItemList.get(idx);
            final Intent intent = new Intent(ACTION_PARKING_MARKER_SELECTED);
            intent.putExtra(EXTRA_PARKING_ITEM, parkingItem);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

            map.animateCamera(CameraUpdateFactory.newLatLng(parkingItem.getPosition()));
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
            case R.raw.parking_combined_min:
                for (Feature feature : featureCollection) {
                    parkingItemList.add(new ParkingItem(feature));
                }
                updateVisibility();
                break;
        }
    }

    private void updateVisibility() {
        // Nothing to do if the items have not yet loaded
        if (parkingItemList.isEmpty()) {
            return;
        }

        if (isShown) {
            clusterManager.addItems(new LinkedList<IClusterableParcelableItem>(parkingItemList));
            isAdded = true;
        } else if (isAdded) {
            clusterManager.removeItems(parkingItemList);
            isAdded = false;
        }

        // Recluster the points
        clusterManager.cluster();
    }

}
