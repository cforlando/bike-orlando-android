package com.codefororlando.transport.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;

public interface IMapController {

    /**
     * Context of map.
     *
     * @return context of map.
     */
    public
    @NonNull
    Context getContext();

    /**
     * Map instance managed by the controller.
     *
     * @return the map
     */
    public
    @NonNull
    GoogleMap getMap();

    /**
     * Toggle the state of a feature.
     *
     * @param featureDescriptor the feature to toggle.
     */
    public void toggleFeature(FeatureDescriptor featureDescriptor);

    /**
     * Feature descriptors available for filtering on.
     *
     * @return feature descriptors
     */
    public
    @NonNull
    FeatureDescriptor[] getFeatureDescriptors();

    /**
     * The cluster manager on the map.
     *
     * @return cluster manager
     */
    public
    @NonNull
    ClusterManager getClusterManager();

}
