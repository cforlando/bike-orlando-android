package com.codefororlando.transport.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.geojson.Feature;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import java.util.LinkedHashMap;

public class ParkingItem implements Parcelable {

    public static final Creator<ParkingItem> CREATOR = new Creator<ParkingItem>() {
        @Override
        public ParkingItem createFromParcel(Parcel source) {
            return new ParkingItem(source);
        }

        @Override
        public ParkingItem[] newArray(int size) {
            return new ParkingItem[0];
        }
    };

    private static final String PROP_ADDRESS = "address";
    private static final String PROP_PRICE = "price";
    private static final String PROP_TYPE = "type";

    private final LatLng position;
    private final String address;
    private final String type;
    private final Price price;

    @SuppressWarnings("unchecked")
    public ParkingItem(Feature feature) {
        final Point point = (Point) feature.getGeometry();
        final LngLatAlt latLngAlt = point.getCoordinates();

        position = new LatLng(latLngAlt.getLatitude(), latLngAlt.getLongitude());
        address = feature.getProperty(PROP_ADDRESS);
        type = feature.getProperty(PROP_TYPE);
        price = new Price((LinkedHashMap<String, String>) feature.getProperty(PROP_PRICE));

    }

    private ParkingItem(Parcel parcel) {
        position = new LatLng(parcel.readDouble(), parcel.readDouble());
        address = parcel.readString();
        type = parcel.readString();
        price = (Price) parcel.readValue(Price.class.getClassLoader());
    }

    public LatLng getPosition() {
        return position;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public Price getPrice() {
        return price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(position.latitude);
        dest.writeDouble(position.longitude);
        dest.writeString(address);
        dest.writeString(type);
        dest.writeValue(price);
    }

    public static final class Price implements Parcelable {

        public static final Creator<Price> CREATOR = new Creator<Price>() {
            @Override
            public Price createFromParcel(Parcel source) {
                return new Price(source);
            }

            @Override
            public Price[] newArray(int size) {
                return new Price[0];
            }
        };

        private static final String PROP_NORMAL = "normal";
        private static final String PROP_EVENT = "event";
        private static final String PROP_DAILY_MAX = "dailyMax";

        private final String normal;
        private final String event;
        private final String dailyMax;

        public Price(LinkedHashMap<String, String> priceMap) {
            normal = priceMap.get(PROP_NORMAL);
            event = priceMap.get(PROP_EVENT);
            dailyMax = priceMap.get(PROP_DAILY_MAX);
        }

        private Price(Parcel parcel) {
            normal = parcel.readString();
            event = parcel.readString();
            dailyMax = parcel.readString();
        }

        public String getNormal() {
            return normal;
        }

        public String getEvent() {
            return event;
        }

        public String getDailyMax() {
            return dailyMax;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(normal);
            parcel.writeString(event);
            parcel.writeString(dailyMax);
        }

    }

}
