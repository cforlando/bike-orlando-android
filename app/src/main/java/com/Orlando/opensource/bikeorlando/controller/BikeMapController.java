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
import android.support.annotation.RawRes;

import com.Orlando.opensource.bikeorlando.R;
import com.Orlando.opensource.bikeorlando.loader.FeatureCollectionLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.iogistics.complexoverlaytiles.CustomTileProvider;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;
import org.geojson.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Map manager handling async loading callbacks and interpretation as well as general instantiation and handling of the
 * map features.
 *
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
public class BikeMapController implements FeatureCollectionLoader.FeatureCollectionLoaderListener {

    private final Context context;
    private final BikeRackManager bikeRackManager;
    private final GoogleMap map;

    private boolean tileProviderAdded;

    public BikeMapController(Context context, GoogleMap map) {
        this.map = map;
        this.context = context;

        bikeRackManager = new BikeRackManager(context, map);
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

    public void destroy() {
        bikeRackManager.clearItems();
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
                    map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomTileProvider(routes
                    )));
                    tileProviderAdded = true;
                }

                break;
            case R.raw.bike_parking:
                setBikeParking(featureCollection);
                break;
        }
    }

    void setBikeParking(FeatureCollection featureCollection) {
        for (Feature feature : featureCollection.getFeatures()) {
            Point point = (Point) feature.getGeometry();
            LngLatAlt latLngAlt = point.getCoordinates();
            LatLng latLng = new LatLng(latLngAlt.getLatitude(), latLngAlt.getLongitude());

            bikeRackManager.addItem(new BikeRackItem(latLng));
        }

        bikeRackManager.cluster();
    }

    private static class BikeRackManager extends ClusterManager<BikeRackItem> {

        public BikeRackManager(Context context, GoogleMap map) {
            super(context, map);
        }

    }

    private static class BikeRackItem implements ClusterItem {

        private final LatLng latLng;

        BikeRackItem(LatLng latLng) {
            this.latLng = latLng;
        }

        @Override
        public LatLng getPosition() {
            return latLng;
        }
    }

}
