package com.codefororlando.transport.data;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.util.SparseArray;

import com.codefororlando.transport.bikeorlando.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.clustering.ClusterItem;

public interface IClusterableParcelableItem extends ClusterItem, Parcelable {

    public BitmapDescriptor getMarkerIcon(Context context);

    public static final class MarkerIcon {

        private static final SparseArray<BitmapDescriptor> iconBitmapDescriptorMap = new SparseArray<>();

        public static synchronized BitmapDescriptor getMarkerIcon(@DrawableRes int id) {
            BitmapDescriptor bitmapDescriptor = iconBitmapDescriptorMap.get(id);
            if (bitmapDescriptor == null) {
                bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bikerackpinpoint);
                iconBitmapDescriptorMap.put(id, bitmapDescriptor);
            }

            return bitmapDescriptor;
        }

    }

}
