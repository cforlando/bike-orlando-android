package com.codefororlando.transport.controller;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.codefororlando.transport.display.IDisplayableFeature;

public final class FeatureDescriptor implements Comparable {

    private final int id;
    private IDisplayableFeature displayableFeature;
    private boolean enabled;

    public FeatureDescriptor(IDisplayableFeature displayableFeature, int id) {
        this.id = id;
        this.displayableFeature = displayableFeature;
    }

    private static int compare(FeatureDescriptor lhs, FeatureDescriptor rhs) {
        final int groupCompare = lhs.getGroupName().compareTo(rhs.getGroupName());
        return groupCompare != 0 ? groupCompare : lhs.getFeatureName().compareTo(rhs.getFeatureName());
    }

    /**
     * Resource id of the feature name
     *
     * @return string resource id
     */
    public
    @StringRes
    int getFeatureId() {
        return displayableFeature.getFeatureName();
    }

    /**
     * String name of the feature.
     *
     * @return feature name
     */
    public String getFeatureName() {
        return displayableFeature.getContext().getString(getFeatureId());
    }

    /**
     * Resource id of the group name.
     *
     * @return string resource id
     */
    public
    @StringRes
    int getGroupId() {
        return displayableFeature.getGroupId();
    }

    /**
     * String name of the group.
     *
     * @return group name
     */
    public String getGroupName() {
        return displayableFeature.getContext().getString(getGroupId());
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

    @Override
    public int compareTo(@NonNull Object another) {
        return compare(this, (FeatureDescriptor) another);
    }

}
