package com.codefororlando.transport.loader;

import com.google.android.gms.maps.model.LatLng;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;

import java.util.ArrayList;
import java.util.List;

public final class FeatureUtils {

    public static ArrayList<ArrayList<LatLng>> featureCollectionToRoutes(FeatureCollection featureCollection) {
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

}
