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
import com.codefororlando.transport.data.ParkingItem;
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

public class ParkingFeature implements IDisplayableFeature, IBroadcasts {

    private final Map<Marker, ParkingItem> parkingItemMap;
    private IMapController mapController;
    private GoogleMap map;
    private boolean isShown;

    public ParkingFeature() {
        parkingItemMap = new HashMap<>();
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
        for (Marker marker : parkingItemMap.keySet()) {
            marker.remove();
        }
        parkingItemMap.clear();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (parkingItemMap.containsKey(marker)) {
            final ParkingItem parkingItem = parkingItemMap.get(marker);

            Intent intent = new Intent(ACTION_PARKING_MARKER_SELECTED);
            intent.putExtra(EXTRA_PARKING_ITEM, parkingItem);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

            // Animate to the location manually as true is returned by the click to prevent the info window popup
            map.animateCamera(CameraUpdateFactory.newLatLng(parkingItem.getPosition()));

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
            case R.raw.parking_combined_min:
                final Bitmap bitmap = BitmapUtility.createBitmapWithCircleAndOverlay(getContext(), R.color.colorPrimaryDarkBlue, R.drawable.parkingpinpoint, 32);
                final BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

                for (Feature feature : featureCollection) {
                    final ParkingItem parkingItem = new ParkingItem(feature);
                    final Marker marker = map.addMarker(new MarkerOptions().position(parkingItem.getPosition())
                            .icon(bitmapDescriptor)
                            .visible(isShown));
                    parkingItemMap.put(marker, parkingItem);
                }
                updateVisibility();
                break;
        }
    }

    private void updateVisibility() {
        for (Marker marker : parkingItemMap.keySet()) {
            marker.setVisible(isShown);
        }
    }

}
