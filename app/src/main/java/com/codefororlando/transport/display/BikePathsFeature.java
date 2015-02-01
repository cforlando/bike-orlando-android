package com.codefororlando.transport.display;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.IMapController;
import com.codefororlando.transport.loader.FeatureCollectionLoader;
import com.codefororlando.transport.loader.FeatureUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.iogistics.complexoverlaytiles.CustomTileProvider;

import org.geojson.FeatureCollection;

import java.util.ArrayList;

public class BikePathsFeature implements IDisplayableFeature {

    private IMapController mapController;
    private GoogleMap map;
    private boolean isShown;
    private TileOverlay tileOverlay;

    @Override
    public int getFeatureName() {
        return R.string.display_feature_bike_paths;
    }

    @Override
    public void setController(IMapController mapController) {
        this.mapController = mapController;
        map = mapController.getMap();

        FeatureCollectionLoader.getInstance(this, R.raw.bike_lanes);
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

    @Override
    public boolean displayAtLaunch() {
        return true;
    }

    private void updateVisibility() {
        if (tileOverlay != null) {
            tileOverlay.setVisible(isShown);
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
            case R.raw.bike_lanes:

                if (tileOverlay == null) {
                    ArrayList<ArrayList<LatLng>> routes = FeatureUtils.featureCollectionToRoutes(featureCollection);
                    CustomTileProvider customTileProvider = new CustomTileProvider(getContext(), routes);
                    TileOverlayOptions tileOverlayOptions = new TileOverlayOptions().tileProvider(customTileProvider);
                    tileOverlay = map.addTileOverlay(tileOverlayOptions);

                    updateVisibility();
                }

                break;
        }
    }


}
