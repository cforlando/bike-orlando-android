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
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.codefororlando.transport.animation.EmptyAnimationListener;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.BikeMapController;
import com.codefororlando.transport.data.BikeRackItem;
import com.codefororlando.transport.fragment.FragmentRack;
import com.codefororlando.transport.fragment.ISelectableItemFragment;
import com.codefororlando.transport.view.FilterView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
@SuppressWarnings("WeakerAccess")
public class MapsActivity extends Activity implements GoogleMap.OnMapClickListener {

    /**
     * Bike item marker selected.
     * <p/>
     * Always includes {@link #EXTRA_BIKE_RACK_ITEM}.
     */
    public static final String ACTION_BIKE_MARKER_SELECTED = "ACTION_BIKE_MARKER_SELECTED";

    /**
     * {@link com.codefororlando.transport.data.BikeRackItem} instance.
     */
    public static final String EXTRA_BIKE_RACK_ITEM = "EXTRA_BIKE_RACK_ITEM";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_BIKE_MARKER_SELECTED: {
                    final BikeRackItem bikeRackItem = intent.getParcelableExtra(EXTRA_BIKE_RACK_ITEM);
                    Fragment fragment = getFragmentManager().findFragmentByTag(ISelectableItemFragment.TAG);
                    if (fragment == null) {
                        fragment = FragmentRack.newInstance(bikeRackItem);
                        showSelectableItemFragment(fragment);
                    } else {
                        ((FragmentRack) fragment).setBikeRackItem(bikeRackItem);
                    }
                }
            }
        }
    };
    private static final int ZOOM_CITY = 11;
    private static final String KEY_FIRST_RUN = "KEY_FIRST_RUN";
    private BikeMapController mapController;
    private GoogleMap map;
    private FilterView filterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded(savedInstanceState);

        filterView = (FilterView) findViewById(R.id.filter_view);
        filterView.setMapController(mapController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded(null);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver,
                new IntentFilter(ACTION_BIKE_MARKER_SELECTED));

        // FIXME Filter should save state to prevent ghost touch of recyclerview
        filterView.animateOnScreen(true);
        filterView.animateOnScreen(getFragmentManager().findFragmentByTag(ISelectableItemFragment.TAG) == null);
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
        Fragment fragment = getFragmentManager().findFragmentByTag(ISelectableItemFragment.TAG);
        if (filterView.isExpanded()) {
            filterView.animateOpen(false);
        } else if (fragment != null && !fragment.isHidden()) {
            removeSelectableItemFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void removeSelectableItemFragment() {
        final Fragment selectableItemFragment = getFragmentManager().findFragmentByTag(ISelectableItemFragment.TAG);
        if (selectableItemFragment != null) {
            final View view = selectableItemFragment.getView();
            if (view == null) {
                return;
            }

            final int animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

            {
                final Animation animation = new TranslateAnimation(0, 0, 0, view.getHeight());
                animation.setDuration(animationDuration);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                filterView.startAnimation(animation);
            }
            {
                final FragmentManager fragmentManager = getFragmentManager();
                final Animation animation = new TranslateAnimation(0, 0, 0, view.getHeight());
                animation.setDuration(animationDuration);
                animation.setAnimationListener(new EmptyAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        try {
                            fragmentManager.beginTransaction()
                                    .remove(selectableItemFragment)
                                    .commitAllowingStateLoss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                view.startAnimation(animation);
            }
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
        filterView.animateOpen(false);
        filterView.animateOnScreen(true);
        removeSelectableItemFragment();
    }

    private void showSelectableItemFragment(final Fragment removableFragment) {
        if (!(removableFragment instanceof ISelectableItemFragment)) {
            throw new IllegalArgumentException("Item fragments must implement " + ISelectableItemFragment.class.getName());
        }

        filterView.animateOnScreen(false);

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, 0)
                .replace(R.id.details_fragment_container, removableFragment, ISelectableItemFragment.TAG)
                .commit();

    }

}
