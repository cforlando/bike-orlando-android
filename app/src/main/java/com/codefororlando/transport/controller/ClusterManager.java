package com.codefororlando.transport.controller;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.codefororlando.transport.controller.clustering.NonHierarchicalDistanceBasedAlgorithm;
import com.codefororlando.transport.data.IClusterableParcelableItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Collection;

public class ClusterManager extends com.google.maps.android.clustering.ClusterManager<IClusterableParcelableItem> {

    private final ClusterRenderer clusterRenderer;
    private final LocalBroadcastManager localBroadcastManager;

    public ClusterManager(Context context, GoogleMap map) {
        super(context, map);

        // Fix the broken algo as it misses simple remove functionality.. SMH
        setAlgorithm(new NonHierarchicalDistanceBasedAlgorithm<IClusterableParcelableItem>());

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        clusterRenderer = new ClusterRenderer(context, map, this);
        setRenderer(clusterRenderer);
    }

    public IClusterableParcelableItem getClusterItem(Marker marker) {
        return clusterRenderer.getClusterItem(marker);
    }

    /**
     * Hack implementation to remove all items as the algorithm interface does not require it in it's
     * implementation for some reason.
     *
     * @param items to be removed from clustering
     */
    public void removeItems(Collection<? extends IClusterableParcelableItem> items) {
        for (IClusterableParcelableItem item : items) {
            removeItem(item);
        }
    }


}
