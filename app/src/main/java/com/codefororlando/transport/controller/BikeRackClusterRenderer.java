package com.codefororlando.transport.controller;

import android.content.*;
import android.content.res.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.util.*;
import android.view.*;

import com.codefororlando.transport.bikeorlando.*;
import com.codefororlando.transport.data.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.clustering.*;
import com.google.maps.android.clustering.view.*;
import com.google.maps.android.ui.*;

import java.util.HashMap;
import java.util.Map;

final class BikeRackClusterRenderer extends DefaultClusterRenderer<BikeRackItem> {

    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};

    private final int[] clusterColors;
    private final SparseArray<BitmapDescriptor> clusterBitmapDescriptors = new SparseArray<>();
    private final Map<String, BikeRackItem> markerToBikeRackItemMap = new HashMap<>();

    private final BitmapDescriptor markerBitmapDescriptor;
    private final IconGenerator iconGenerator;
    private final float density;
    private final ShapeDrawable clusterLayerBackground;

    public BikeRackClusterRenderer(Context context, GoogleMap map,
                                   ClusterManager<BikeRackItem> clusterManager) {
        super(context, map, clusterManager);

        TypedArray typedArray = context.getResources().obtainTypedArray(R.array.colorClusters);
        clusterColors = new int[typedArray.length()];
        for (int i = 0; i < clusterColors.length; ++i) {
            clusterColors[i] = typedArray.getColor(i, 0);
        }
        typedArray.recycle();

        density = context.getResources().getDisplayMetrics().density;
        clusterLayerBackground = new ShapeDrawable(new OvalShape());
        markerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bikerackpinpoint);

        iconGenerator = new IconGenerator(context);
        iconGenerator.setContentView(makeSquareTextView(context));
        iconGenerator.setTextAppearance(R.style.ClusterIcon_TextAppearance);
        iconGenerator.setBackground(makeClusterBackground());
    }

    @Override
    protected void onBeforeClusterItemRendered(BikeRackItem item, MarkerOptions markerOptions) {
        markerOptions.icon(markerBitmapDescriptor);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<BikeRackItem> cluster, MarkerOptions markerOptions) {
        int bucket = getBucket(cluster);
        BitmapDescriptor descriptor = clusterBitmapDescriptors.get(bucket);
        if (descriptor == null) {
            clusterLayerBackground.getPaint().setColor(getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getClusterText(bucket)));
            clusterBitmapDescriptors.put(bucket, descriptor);
        }

        markerOptions.icon(descriptor);
    }

    @Override
    protected void onClusterItemRendered(final BikeRackItem clusterItem, final Marker marker) {
        markerToBikeRackItemMap.put(marker.getId(), clusterItem);
    }

    public BikeRackItem getItemForMarker(Marker marker) {
        return markerToBikeRackItemMap.get(marker.getId());
    }

    private LayerDrawable makeClusterBackground() {
        int strokeWidth = (int) (density * 3);

        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(0x80ffffff);

        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, clusterLayerBackground});
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);

        return background;
    }

    private SquareTextView makeSquareTextView(Context context) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int twelveDpi = (int) (12 * density);

        SquareTextView squareTextView = new SquareTextView(context);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(R.id.text);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }

    private int getColor(int clusterSize) {
        for (int i = 0; i < BUCKETS.length; i++) {
            if (clusterSize == BUCKETS[i])
                return clusterColors[i];
        }

        return clusterColors[0];
    }

}
