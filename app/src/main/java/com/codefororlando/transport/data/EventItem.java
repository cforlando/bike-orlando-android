package com.codefororlando.transport.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;

import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.bitmap.BitmapUtility;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import org.geojson.Feature;
import org.geojson.LngLatAlt;
import org.geojson.Point;

public class EventItem implements IClusterableParcelableItem {

    public static final Creator<EventItem> CREATOR = new Creator<EventItem>() {
        @Override
        public EventItem createFromParcel(Parcel source) {
            return new EventItem(source);
        }

        @Override
        public EventItem[] newArray(int size) {
            return new EventItem[0];
        }
    };

    private static final String PROP_NAME = "name";
    private static final String PROP_VENUE_ID = "venueId";
    private static final String PROP_ADDRESS = "address";
    private static BitmapDescriptor bitmapDescriptor;
    private final LatLng position;
    private final String name;
    private final int venueId;
    private final String address;

    public EventItem(Feature feature) {
        final Point point = (Point) feature.getGeometry();
        final LngLatAlt latLngAlt = point.getCoordinates();

        position = new LatLng(latLngAlt.getLatitude(), latLngAlt.getLongitude());
        name = feature.getProperty(PROP_NAME);
        venueId = feature.getProperty(PROP_VENUE_ID);
        address = feature.getProperty(PROP_ADDRESS);
    }

    private EventItem(Parcel parcel) {
        position = new LatLng(parcel.readDouble(), parcel.readDouble());
        venueId = parcel.readInt();
        name = parcel.readString();
        address = parcel.readString();
    }

    @Override
    public synchronized BitmapDescriptor getMarkerIcon(Context context) {
        if (bitmapDescriptor == null) {
            final Bitmap bitmap = BitmapUtility.createBitmapWithCircleAndOverlay(context, R.color.colorPrimaryPurple, R.drawable.eventpintpoint, 32);
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return bitmapDescriptor;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public int getVenueId() {
        return venueId;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(position.latitude);
        dest.writeDouble(position.longitude);
        dest.writeInt(venueId);
        dest.writeString(name);
        dest.writeString(address);
    }

}
