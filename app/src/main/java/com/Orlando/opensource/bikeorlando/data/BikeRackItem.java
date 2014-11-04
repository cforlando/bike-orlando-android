package com.Orlando.opensource.bikeorlando.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.geojson.Feature;
import org.geojson.LngLatAlt;
import org.geojson.Point;

public final class BikeRackItem implements ClusterItem {

    private final LatLng latLng;

    public BikeRackItem(Feature feature) {
        Point point = (Point) feature.getGeometry();
        LngLatAlt latLngAlt = point.getCoordinates();

        this.latLng = new LatLng(latLngAlt.getLatitude(), latLngAlt.getLongitude());
    }

    /**
     * Returns the underlying LatLng position. It is imperative that the LatLng be cloned should changes need to be made
     * as the object being immutable is a dependency of {@link com.Orlando.opensource.bikeorlando.controller
     * .BikeRackClusterManager}.
     *
     * @return
     */
    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String toString() {
        return latLng.toString();
    }

}
