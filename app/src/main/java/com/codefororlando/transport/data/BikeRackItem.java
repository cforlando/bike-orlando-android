package com.codefororlando.transport.data;

import android.content.Context;
import android.os.Parcel;

import com.codefororlando.transport.bikeorlando.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;

import org.geojson.Feature;
import org.geojson.LngLatAlt;
import org.geojson.Point;

public final class BikeRackItem implements IClusterableParcelableItem {

    public static final Creator<BikeRackItem> CREATOR = new Creator<BikeRackItem>() {
        @Override
        public BikeRackItem createFromParcel(final Parcel source) {
            return new BikeRackItem(source);
        }

        @Override
        public BikeRackItem[] newArray(final int size) {
            return new BikeRackItem[0];
        }
    };

    private static final String PROP_ADDRESS = "address";
    private static final String PROP_CAPACITY = "capacity";
    private static final String PROP_OWNERSHIP = "ownership";
    private static final String PROP_TYPE = "type";

    private final LatLng position;
    private final String address;
    private final String ownership;
    private final String type;
    private final int capacity;

    public BikeRackItem(Feature feature) {
        final Point point = (Point) feature.getGeometry();
        final LngLatAlt latLngAlt = point.getCoordinates();

        final String address = feature.getProperty(PROP_ADDRESS);
        final String ownership = feature.getProperty(PROP_OWNERSHIP);
        final String type = feature.getProperty(PROP_TYPE);
        final String capacity = feature.getProperty(PROP_CAPACITY);

        position = new LatLng(latLngAlt.getLatitude(), latLngAlt.getLongitude());
        this.address = address == null ? latLngAlt.toString() : address;
        this.ownership = ownership == null ? "" : ownership.trim();
        this.type = type == null ? "n/a" : type.trim();
        this.capacity = capacity == null ? 0 : Integer.parseInt(capacity);
    }

    private BikeRackItem(Parcel parcel) {
        position = new LatLng(parcel.readDouble(), parcel.readDouble());
        address = parcel.readString();
        ownership = parcel.readString();
        type = parcel.readString();
        capacity = parcel.readInt();
    }

    @Override
    public BitmapDescriptor getMarkerIcon(Context context) {
        return MarkerIcon.getMarkerIcon(R.drawable.bikerackpinpoint);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeDouble(position.latitude);
        parcel.writeDouble(position.longitude);
        parcel.writeString(address);
        parcel.writeString(ownership);
        parcel.writeString(type);
        parcel.writeInt(capacity);
    }

    /**
     * Returns the underlying LatLng position. It is imperative that the LatLng be cloned should
     * changes need to be made as the object being immutable is a dependency of {@link
     * com.codefororlando.transport.controller .BikeRackClusterManager}.
     *
     * @return World Geodetic coordinate of the bike rack
     */
    @Override
    public LatLng getPosition() {
        return position;
    }

    public String getAddress() {
        return address;
    }

    public String getOwnership() {
        return ownership;
    }

    public String getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return position.toString();
    }

}
