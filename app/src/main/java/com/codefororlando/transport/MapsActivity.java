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
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.codefororlando.transport.animation.EmptyAnimationListener;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.BikeMapController;
import com.codefororlando.transport.data.BikeRackItem;
import com.codefororlando.transport.data.ParkingItem;
import com.codefororlando.transport.fragment.FragmentParking;
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
public class MapsActivity extends Activity implements GoogleMap.OnMapClickListener, IBroadcasts {

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_BIKE_MARKER_SELECTED: {
                    final BikeRackItem bikeRackItem = intent.getParcelableExtra(EXTRA_BIKE_RACK_ITEM);
                    Fragment fragment = getFragmentManager().findFragmentByTag(ISelectableItemFragment.TAG);
                    if (fragment != null && !fragment.getClass().equals(FragmentRack.class)) {
                        getFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commit();
                        fragment = null;
                    }
                    if (fragment == null) {
                        showSelectableItemFragment(FragmentRack.newInstance(bikeRackItem));
                    } else {
                        ((FragmentRack) fragment).setBikeRackItem(bikeRackItem);
                    }
                    break;
                }
                case ACTION_PARKING_MARKER_SELECTED: {
                    final ParkingItem parkingItem = intent.getParcelableExtra(EXTRA_PARKING_ITEM);
                    Fragment fragment = getFragmentManager().findFragmentByTag(ISelectableItemFragment.TAG);
                    if (fragment != null && !fragment.getClass().equals(FragmentParking.class)) {
                        getFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commit();
                        fragment = null;
                    }
                    if (fragment == null) {
                        showSelectableItemFragment(FragmentParking.newInstance(parkingItem));
                    } else {
                        ((FragmentParking) fragment).setParkingItem(parkingItem);
                    }
                    break;
                }
            }
        }
    };
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

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BIKE_MARKER_SELECTED);
        intentFilter.addAction(ACTION_PARKING_MARKER_SELECTED);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);

        // FIXME Filter should save state to prevent ghost touch of recyclerview
        filterView.animateOnScreen(true);
        filterView.animateOnScreen(getFragmentManager().findFragmentByTag(ISelectableItemFragment.TAG) == null);
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
            // Close the FilterView if it is open
            filterView.animateOpen(false);
        } else if (fragment != null && !fragment.isHidden()) {
            // Display the filter fab
            filterView.animateOnScreen(true);

            // Remove the on screen fragment
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
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                // Animate zoom to city level and move to Orlando
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.5383355d, -81.3792365d), 11));
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
