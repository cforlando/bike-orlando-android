package com.Orlando.opensource.bikeorlando.controller;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.RawRes;

import com.Orlando.opensource.bikeorlando.R;
import com.Orlando.opensource.bikeorlando.loader.FeatureCollectionLoader;
import com.Orlando.opensource.bikeorlando.overlay.PolyLineTileProvider;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;
import org.geojson.Point;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by tencent on 9/19/2014.
 */
public class BikeMapController implements FeatureCollectionLoader.FeatureCollectionLoaderListener {

    Context context;
    GoogleMap map;
    BikeRackManager bikeRackManager;
    List<Polyline> polylines;

    public BikeMapController(Context context, GoogleMap map) {
        this.map = map;
        this.context = context;

        polylines = new LinkedList<>();
        bikeRackManager = new BikeRackManager(context, map);
        map.setOnCameraChangeListener(bikeRackManager);
        map.setOnMarkerClickListener(bikeRackManager);
        map.addTileOverlay(new TileOverlayOptions().tileProvider(new PolyLineTileProvider()));

        FeatureCollectionLoader.getInstance(this, R.raw.bike_parking);
        FeatureCollectionLoader.getInstance(this, R.raw.bike_lanes);
    }

    public void destroy() {
        polylines.clear();
        bikeRackManager.clearItems();
        map = null;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void onFeatureCollectionLoaded(@RawRes int resourceId, FeatureCollection featureCollection) {
        switch (resourceId) {
            case R.raw.bike_lanes:
                setBikeLanes(featureCollection);
                break;
            case R.raw.bike_parking:
                setBikeParking(featureCollection);
                break;
        }
    }

    void setBikeLanes(FeatureCollection featureCollection) {
        Iterator<Feature> iterator = featureCollection.getFeatures().iterator();
        while (iterator.hasNext()) {
            Feature feature = iterator.next();

            GeoJsonObject geometry  = feature.getGeometry();
            switch (geometry.getClass().getSimpleName()) {
                case "LineString":
                    LineString lineString = (LineString) geometry;
                    drawLine(lineString.getCoordinates());
                    break;
                case "MultiLineString":
                    MultiLineString multiLineString = (MultiLineString) geometry;
                    Iterator<List<LngLatAlt>> lineStringIterator = multiLineString.getCoordinates()
                            .iterator();
                    while (lineStringIterator.hasNext()) {
                        List<LngLatAlt> line = lineStringIterator.next();
                        drawLine(line);
                    }
                    break;
            }
        }
    }

    private void drawLine(List<LngLatAlt> coordinates) {
        List<LatLng> latLngList = new LinkedList<>();
        for (LngLatAlt lngLatAlt : coordinates)
            latLngList.add(new LatLng(lngLatAlt.getLatitude(), lngLatAlt.getLongitude()));

        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngList).color(Color.RED);

        polylines.add(map.addPolyline(polylineOptions));
    }

    void setBikeParking(FeatureCollection featureCollection) {
        Iterator<Feature> iterator = featureCollection.getFeatures().iterator();
        while (iterator.hasNext()) {
            Feature feature = iterator.next();

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
