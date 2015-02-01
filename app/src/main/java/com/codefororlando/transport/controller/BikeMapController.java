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
import android.support.annotation.NonNull;

import com.codefororlando.transport.data.FeatureDescriptor;
import com.codefororlando.transport.display.BikePathsFeature;
import com.codefororlando.transport.display.BikeRacksFeature;
import com.codefororlando.transport.display.IDisplayableFeature;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Map manager handling async loading callbacks and interpretation as well as general instantiation
 * and handling of the map features.
 *
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
public final class BikeMapController implements IMapController, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener {

    private static final Class[] DISPLAYABLE_FEATURE_CLASSES = new Class[]{
            BikePathsFeature.class
            , BikeRacksFeature.class
    };

    private final List<IDisplayableFeature> featureList;
    private final FeatureDescriptor[] featureDescriptors;
    private final Context context;
    private final GoogleMap map;

    public BikeMapController(Context context, GoogleMap map) {
        this.map = map;
        this.context = context;
        featureList = new ArrayList<>(DISPLAYABLE_FEATURE_CLASSES.length);
        featureDescriptors = new FeatureDescriptor[DISPLAYABLE_FEATURE_CLASSES.length];

        map.setOnMarkerClickListener(this);
        map.setOnCameraChangeListener(this);

        for (int i = 0; i < DISPLAYABLE_FEATURE_CLASSES.length; i++) {
            Class displayableFeatureClass = DISPLAYABLE_FEATURE_CLASSES[i];
            try {
                final IDisplayableFeature displayableFeature = (IDisplayableFeature) displayableFeatureClass.newInstance();
                displayableFeature.setController(this);

                final FeatureDescriptor featureDescriptor = new FeatureDescriptor(displayableFeature.getFeatureName(), i);
                featureDescriptor.setEnabled(displayableFeature.displayAtLaunch());

                if (featureDescriptor.isEnabled()) {
                    displayableFeature.show();
                }

                featureList.add(displayableFeature);
                featureDescriptors[i] = featureDescriptor;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate feature: " + displayableFeatureClass.getName(), e);
            }
        }

    }

    @Override
    public void toggleFeature(FeatureDescriptor featureDescriptor) {
        featureDescriptor.setEnabled(!featureDescriptor.isEnabled());
        final IDisplayableFeature feature = featureList.get(featureDescriptor.getFeatureId());
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
        for (IDisplayableFeature displayableFeature : featureList) {
            displayableFeature.destroy();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (IDisplayableFeature displayableFeature : featureList) {
            if (displayableFeature.onMarkerClick(marker))
                return true;
        }

        return false;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        for (IDisplayableFeature displayableFeature : featureList) {
            displayableFeature.onCameraChange(cameraPosition);
        }
    }

}
