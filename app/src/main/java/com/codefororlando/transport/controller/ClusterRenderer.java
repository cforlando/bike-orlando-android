package com.codefororlando.transport.controller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.data.IClusterableParcelableItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

final class ClusterRenderer extends DefaultClusterRenderer<IClusterableParcelableItem> {

    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};

    private final int[] clusterColors;
    private final SparseArray<BitmapDescriptor> clusterBitmapDescriptors = new SparseArray<>();

    private final IconGenerator iconGenerator;
    private final float density;
    private final ShapeDrawable clusterLayerBackground;
    private final Context context;

    public ClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);

        this.context = context.getApplicationContext();

        TypedArray typedArray = context.getResources().obtainTypedArray(R.array.colorClusters);
        clusterColors = new int[typedArray.length()];
        for (int i = 0; i < clusterColors.length; ++i) {
            clusterColors[i] = typedArray.getColor(i, 0);
        }
        typedArray.recycle();

        density = context.getResources().getDisplayMetrics().density;
        clusterLayerBackground = new ShapeDrawable(new OvalShape());

        iconGenerator = new IconGenerator(context);
        iconGenerator.setContentView(makeSquareTextView(context));
        iconGenerator.setTextAppearance(R.style.ClusterIcon_TextAppearance);
        iconGenerator.setBackground(makeClusterBackground());
    }

    private Context getContext() {
        return context;
    }

    @Override
    protected void onBeforeClusterItemRendered(IClusterableParcelableItem item, MarkerOptions markerOptions) {
        BitmapDescriptor icon = item.getMarkerIcon(getContext());
        if (icon != null) {
            markerOptions.icon(icon);
        }
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<IClusterableParcelableItem> cluster, MarkerOptions markerOptions) {
        int bucket = getBucket(cluster);
        BitmapDescriptor descriptor = clusterBitmapDescriptors.get(bucket);
        if (descriptor == null) {
            clusterLayerBackground.getPaint().setColor(getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getClusterText(bucket)));
            clusterBitmapDescriptors.put(bucket, descriptor);
        }

        markerOptions.icon(descriptor);
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
