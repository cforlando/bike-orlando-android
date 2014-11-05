/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.Orlando.opensource.bikeorlando.controller;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.RawRes;
import android.support.v4.content.LocalBroadcastManager;

import com.Orlando.opensource.bikeorlando.MapsActivity;
import com.Orlando.opensource.bikeorlando.R;
import com.Orlando.opensource.bikeorlando.data.BikeRackItem;
import com.Orlando.opensource.bikeorlando.loader.FeatureCollectionLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.iogistics.complexoverlaytiles.CustomTileProvider;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;

import java.util.ArrayList;
import java.util.List;

/**
 * Map manager handling async loading callbacks and interpretation as well as general instantiation and handling of the
 * map features.
 *
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
public class BikeMapController implements FeatureCollectionLoader.FeatureCollectionLoaderListener,
        GoogleMap.OnMarkerClickListener {

    private final Context context;
    private final BikeRackClusterManager bikeRackManager;
    private final GoogleMap map;

    private boolean tileProviderAdded;

    public BikeMapController(Context context, GoogleMap map) {
        this.map = map;
        this.context = context;

        bikeRackManager = new BikeRackClusterManager(context, map);
        bikeRackManager.getMarkerCollection().setOnMarkerClickListener(this);

        map.setOnCameraChangeListener(bikeRackManager);
        map.setOnMarkerClickListener(bikeRackManager);

        FeatureCollectionLoader.getInstance(this, R.raw.bike_parking);
        FeatureCollectionLoader.getInstance(this, R.raw.bike_lanes);
    }

    private static ArrayList<ArrayList<LatLng>> featureCollectionToRoutes(FeatureCollection featureCollection) {
        ArrayList<ArrayList<LatLng>> routes = new ArrayList<>();

        for (Feature feature : featureCollection.getFeatures()) {
            GeoJsonObject geometry = feature.getGeometry();
            switch (geometry.getClass().getSimpleName()) {
                case "LineString":
                    LineString lineString = (LineString) geometry;
                    routes.add(lineToRoute(lineString.getCoordinates()));
                    break;
                case "MultiLineString":
                    MultiLineString multiLineString = (MultiLineString) geometry;
                    for (List<LngLatAlt> line : multiLineString.getCoordinates()) {
                        // TODO Not sure if it is correct to draw each of these as individual routes
                        routes.add(lineToRoute(line));
                    }
                    break;
            }
        }

        return routes;
    }

    private static ArrayList<LatLng> lineToRoute(List<LngLatAlt> points) {
        ArrayList<LatLng> route = new ArrayList<>();

        for (LngLatAlt lngLatAlt : points)
            route.add(new LatLng(lngLatAlt.getLatitude(), lngLatAlt.getLongitude()));

        return route;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(MapsActivity.ACTION_MARKER_SELECTED);
        intent.putExtra(MapsActivity.EXTRA_LAT_LNG, marker.getPosition());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        return false;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void onFeatureCollectionLoaded(@RawRes int resourceId, FeatureCollection featureCollection) {
        switch (resourceId) {
            case R.raw.bike_lanes:

                if (!tileProviderAdded) {
                    ArrayList<ArrayList<LatLng>> routes = featureCollectionToRoutes(featureCollection);
                    CustomTileProvider customTileProvider = new CustomTileProvider(context, routes);
                    TileOverlayOptions tileOverlayOptions = new TileOverlayOptions().tileProvider(customTileProvider);

                    map.addTileOverlay(tileOverlayOptions);
                    tileProviderAdded = true;
                }

                break;
            case R.raw.bike_parking:
                setBikeParking(featureCollection);
                break;
        }
    }

    public void destroy() {
        bikeRackManager.clearItems();
    }

    private void setBikeParking(FeatureCollection featureCollection) {
        for (Feature feature : featureCollection.getFeatures()) {
            bikeRackManager.addItem(new BikeRackItem(feature));
        }

        bikeRackManager.cluster();
    }

}
