package com.codefororlando.transport.controller;

import android.support.annotation.StringRes;

import com.codefororlando.transport.display.IDisplayableFeature;

public final class FeatureDescriptor {

    private final int id;
    private IDisplayableFeature displayableFeature;
    private boolean enabled;

    public FeatureDescriptor(IDisplayableFeature displayableFeature, int id) {
        this.id = id;
        this.displayableFeature = displayableFeature;
    }

    String getFeatureIdName() {
        return "feature_name_" + id;
    }

    /**
     * Resource id of the feature name
     *
     * @return string resource id
     */
    public
    @StringRes
    int getFeatureName() {
        return displayableFeature.getFeatureName();
    }

    /**
     * Indication of feature drawn on screen.
     *
     * @return true if currently displayed
     */
    public boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    IDisplayableFeature getDisplayableFeature() {
        return displayableFeature;
    }

}
