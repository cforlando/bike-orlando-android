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
import com.codefororlando.transport.data.BikeRackItem;
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

public class BikeRacksFeature implements IDisplayableFeature, IBroadcasts {

    private final List<BikeRackItem> bikeRackItems;
    private IMapController mapController;
    private ClusterManager clusterManager;
    private GoogleMap map;
    private boolean isShown, isAdded;

    public BikeRacksFeature() {
        bikeRackItems = new LinkedList<>();
    }

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
        clusterManager = mapController.getClusterManager();
        map = mapController.getMap();

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
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        final IClusterableParcelableItem clusterItem = clusterManager.getClusterItem(marker);
        final int idx = bikeRackItems.indexOf(clusterItem);

        // Find the bike rack item and retrieve it to prevent casting and instanceof checking. Effectively this is uncessary but more 'right'.
        if (idx > -1) {
            final BikeRackItem bikeRackItem = bikeRackItems.get(idx);
            final Intent intent = new Intent(ACTION_BIKE_MARKER_SELECTED);
            intent.putExtra(EXTRA_BIKE_RACK_ITEM, bikeRackItem);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

            map.animateCamera(CameraUpdateFactory.newLatLng(bikeRackItem.getPosition()));
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
            case R.raw.bike_parking:
                for (Feature feature : featureCollection) {
                    bikeRackItems.add(new BikeRackItem(feature));
                }
                updateVisibility();
                break;
        }
    }

    // FIXME This can probably be done as a static helper otherwise most if not all implementations will do the same thing
    private void updateVisibility() {
        // Nothing to do if the items have not yet loaded
        if (bikeRackItems.isEmpty()) {
            return;
        }

        if (isShown) {
            clusterManager.addItems(new LinkedList<IClusterableParcelableItem>(bikeRackItems));
            isAdded = true;
        } else if (isAdded) {
            clusterManager.removeItems(bikeRackItems);
            isAdded = false;
        }

        // Recluster the points
        clusterManager.cluster();
    }

}
