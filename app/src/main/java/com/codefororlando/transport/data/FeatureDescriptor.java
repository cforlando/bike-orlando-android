package com.codefororlando.transport.data;

import android.support.annotation.StringRes;

public class FeatureDescriptor {

    private final
    @StringRes
    int featureName;

    private final int featureId;

    private boolean enabled;

    public FeatureDescriptor(int featureName, int featureId) {
        this.featureName = featureName;
        this.featureId = featureId;
    }

    public
    @StringRes
    int getFeatureName() {
        return featureName;
    }

    public int getFeatureId() {
        return featureId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
