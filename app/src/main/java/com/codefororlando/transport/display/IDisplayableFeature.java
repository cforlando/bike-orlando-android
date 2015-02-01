package com.codefororlando.transport.display;

import android.support.annotation.StringRes;

import com.codefororlando.transport.controller.IMapController;
import com.codefororlando.transport.loader.FeatureCollectionLoader;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

public interface IDisplayableFeature extends FeatureCollectionLoader.FeatureCollectionLoaderListener {

    /**
     * The name of the feature represented by a String resource.
     *
     * @return string resource
     */
    public
    @StringRes
    int getFeatureName();

    /**
     * Initialize the feature with the map. This should not place any items on the map.
     *
     * @param mapController the controller owning the map implementation
     */
    public void setController(IMapController mapController);

    /**
     * The feature has been added and should be added to the map.
     */
    public void show();

    /**
     * The feature has been removed and should be removed from the map.
     */
    public void hide();

    /**
     * Perform any necessary cleanup. The feature will no longer be used after this is called.
     */
    public void destroy();

    /**
     * Callback for a user clicking a map marker.
     *
     * @return event was consumed by the feature implementation and no other implementations should
     * be notified of the click.
     */
    public boolean onMarkerClick(Marker marker);

    /**
     * Notification of a camera change event.
     *
     * @param cameraPosition new camera position.
     */
    public void onCameraChange(CameraPosition cameraPosition);

    /**
     * Indicate if the feature should be turned on at the start of the application.
     *
     * @return true if should be shown at cold start
     */
    public boolean displayAtLaunch();

}
