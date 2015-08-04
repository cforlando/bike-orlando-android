package com.codefororlando.transport.fragment;

import android.animation.Animator;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.codefororlando.transport.IBroadcasts;
import com.codefororlando.transport.animation.EmptyAnimatorListener;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.data.ParkingItem;

public class FragmentParking extends Fragment implements View.OnClickListener, ISelectableItemFragment, IBroadcasts {

    private TextView textViewParkingAddress;
    private TextView textViewParkingAddressTwo;
    private TextView textViewParkingHourly;
    private TextView textViewParkingEvent;
    private TextView textViewParkingDailyMax;

    private ParkingItem parkingItem;

    public static Fragment newInstance(ParkingItem parkingItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PARKING_ITEM, parkingItem);

        Fragment fragment = new FragmentParking();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_route_to_point:
                final Uri geoLocation = Uri.parse("geo:0,0?q=" + parkingItem.getAddress());
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);
                if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                    Toast.makeText(getActivity(), R.string.error_no_mapping_app, Toast.LENGTH_LONG)
                            .show();
                } else {
                    startActivity(intent);
                }
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_parking, container, false);

        textViewParkingAddress = (TextView) view.findViewById(R.id.parking_text_view_address);
        textViewParkingAddressTwo = (TextView) view.findViewById(R.id.parking_text_view_address_two);
        textViewParkingHourly = (TextView) view.findViewById(R.id.parking_price_hourly);
        textViewParkingEvent = (TextView) view.findViewById(R.id.parking_price_event);
        textViewParkingDailyMax = (TextView) view.findViewById(R.id.parking_price_daily_max);

        setParkingItem(getArguments().<ParkingItem>getParcelable(EXTRA_PARKING_ITEM));

        final View fabRouteToPoint = view.findViewById(R.id.fab_route_to_point);
        fabRouteToPoint.setOnClickListener(this);
        fabRouteToPoint.animate()
                .setListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        fabRouteToPoint.setScaleX(0);
                        fabRouteToPoint.setScaleY(0);
                    }
                })
                .scaleY(1)
                .scaleX(1)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        return view;
    }

    public void setParkingItem(ParkingItem parkingItem) {
        this.parkingItem = parkingItem;
        updateParking();
    }

    private void updateParking() {
        // Address
        final String address = parkingItem.getAddress();
        final int newLineIdx = address.indexOf("\n");
        textViewParkingAddress.setText(address.substring(0, newLineIdx));
        textViewParkingAddressTwo.setText(address.substring(newLineIdx + 1, address.length()));

        // Prices
        final ParkingItem.Price price = parkingItem.getPrice();
        textViewParkingHourly.setText(price.getNormal());
        textViewParkingEvent.setText(price.getEvent());
        textViewParkingDailyMax.setText(price.getDailyMax());
    }


}
