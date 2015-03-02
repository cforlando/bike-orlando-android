/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.codefororlando.transport.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.display.BikePathsFeature;
import com.codefororlando.transport.display.BikeRacksFeature;
import com.codefororlando.transport.display.EventFeature;
import com.codefororlando.transport.display.IDisplayableFeature;
import com.codefororlando.transport.display.ParkingFeature;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.Arrays;

/**
 * Map manager handling async loading callbacks and interpretation as well as general instantiation
 * and handling of the map features.
 *
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
public final class MapController implements IMapController, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener {

    private static final Class[] DISPLAYABLE_FEATURE_CLASSES = new Class[]{
            BikePathsFeature.class
            , BikeRacksFeature.class
            , ParkingFeature.class
            , EventFeature.class
    };

    private final FeatureDescriptor[] featureDescriptors;
    private final Context context;
    private final GoogleMap map;
    private final SharedPreferences sharedPreferences;
    private final ClusterManager clusterManager;

    public MapController(Context context, GoogleMap map) {
        this.map = map;
        this.context = context;
        featureDescriptors = new FeatureDescriptor[DISPLAYABLE_FEATURE_CLASSES.length];
        clusterManager = new ClusterManager(context, map);

        map.setOnMarkerClickListener(this);
        map.setOnCameraChangeListener(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Determine the default group to display
        final TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.string.default_selected_group, typedValue, true);
        final int selectedDefaultGroup = typedValue.resourceId;

        // Instantiate the display implementations and show the default or user selected items
        for (int i = 0; i < DISPLAYABLE_FEATURE_CLASSES.length; i++) {
            Class displayableFeatureClass = DISPLAYABLE_FEATURE_CLASSES[i];
            try {
                final IDisplayableFeature displayableFeature = (IDisplayableFeature) displayableFeatureClass.newInstance();
                displayableFeature.setController(this);

                final FeatureDescriptor featureDescriptor = new FeatureDescriptor(displayableFeature, i);
                featureDescriptor.setEnabled(sharedPreferences.getBoolean(featureDescriptor.getFeatureName(), displayableFeature.getGroupId() == selectedDefaultGroup));

                if (sharedPreferences.getBoolean(featureDescriptor.getFeatureName(), featureDescriptor.isEnabled())) {
                    displayableFeature.show();
                }

                featureDescriptors[i] = featureDescriptor;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate feature: " + displayableFeatureClass.getName(), e);
            }
        }

    }

    @Override
    public void toggleFeature(FeatureDescriptor featureDescriptor) {
        featureDescriptor.setEnabled(!featureDescriptor.isEnabled());

        sharedPreferences.edit()
                .putBoolean(featureDescriptor.getFeatureName(), featureDescriptor.isEnabled())
                .apply();

        final IDisplayableFeature feature = featureDescriptor.getDisplayableFeature();
        if (featureDescriptor.isEnabled()) {
            feature.show();
        } else {
            feature.hide();
        }
    }

    @Override
    public
    @NonNull
    FeatureDescriptor[] getFeatureDescriptors() {
        return Arrays.copyOf(featureDescriptors, featureDescriptors.length);
    }

    @NonNull
    @Override
    public Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public GoogleMap getMap() {
        return map;
    }

    public void destroy() {
        // Remove item references
        clusterManager.clearItems();

        // Notify features that the view is being destroyed
        for (FeatureDescriptor featureDescriptor : featureDescriptors) {
            featureDescriptor.getDisplayableFeature().destroy();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Notify the clusters of a marker click
        clusterManager.onMarkerClick(marker);

        // Notify the features of a marker click
        for (FeatureDescriptor featureDescriptor : featureDescriptors) {
            if (featureDescriptor.getDisplayableFeature().onMarkerClick(marker))
                return true;
        }

        return false;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // Notify the clusters of camera change
        clusterManager.onCameraChange(cameraPosition);

        // Notify features of camera changes
        for (FeatureDescriptor featureDescriptor : featureDescriptors) {
            featureDescriptor.getDisplayableFeature().onCameraChange(cameraPosition);
        }
    }

    @NonNull
    @Override
    public ClusterManager getClusterManager() {
        return clusterManager;
    }

}
