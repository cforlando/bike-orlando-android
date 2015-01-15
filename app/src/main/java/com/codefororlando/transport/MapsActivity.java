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

package com.codefororlando.transport;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.BikeMapController;
import com.codefororlando.transport.data.BikeRackItem;
import com.codefororlando.transport.fragment.FragmentRack;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
@SuppressWarnings("WeakerAccess")
public class MapsActivity extends Activity implements GoogleMap.OnMapClickListener {

    public static final String ACTION_MARKER_SELECTED = MapsActivity.class.getName() + ".ACTION_MARKER_SELECTED";
    public static final String EXTRA_BIKE_RACK_ITEM = "EXTRA_BIKE_RACK_ITEM";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_MARKER_SELECTED.equals(intent.getAction())) {
                final BikeRackItem bikeRackItem = intent.getParcelableExtra(EXTRA_BIKE_RACK_ITEM);

                Fragment fragment = getFragmentManager().findFragmentByTag(FragmentRack.TAG);
                if (fragment == null || fragment.isHidden()) {
                    fragment = FragmentRack.newInstance(bikeRackItem);
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_up, 0)
                            .replace(R.id.panel, fragment, FragmentRack.TAG)
                            .commit();
                } else {
                    ((FragmentRack) fragment).setBikeRackItem(bikeRackItem);
                }
            }
        }
    };

    private static final double ORLANDO_LAT = 28.5383355;
    private static final double ORLANDO_LNG = -81.3792365;
    private static final int ZOOM_CITY = 11;
    private static final int ZOOM_STATE = 7;
    private static final String KEY_FIRST_RUN = "KEY_FIRST_RUN";
    private BikeMapController mapController;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded(null);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver,
                new IntentFilter(ACTION_MARKER_SELECTED));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_FIRST_RUN, false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (map != null)
            map.setOnMapClickListener(null);

        mapController.destroy();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentByTag(FragmentRack.TAG);
        if (fragment != null && !fragment.isHidden()) {
            removeRackFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void setUpMapIfNeeded(Bundle savedInstanceState) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        if (map != null && mapController == null) {
            mapController = new BikeMapController(this, map);

            if (savedInstanceState == null) {
                // Look at Orlando from state level
                LatLng orlandoLatLng = new LatLng(ORLANDO_LAT, ORLANDO_LNG);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(orlandoLatLng, ZOOM_STATE));
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                // Animate zoom to city level
                map.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_CITY));
            }
        }

        if (map != null) {
            map.setOnMapClickListener(this);
        }
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        removeRackFragment();
    }

    private void removeRackFragment() {
        FragmentRack fragmentRack = (FragmentRack) getFragmentManager().findFragmentByTag(FragmentRack.TAG);
        if (fragmentRack != null) {
            fragmentRack.removeFragment();
        }
    }

}
