package com.codefororlando.transport.fragment;

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

import com.codefororlando.transport.MapsActivity;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.data.BikeRackItem;

public class FragmentRack extends Fragment implements View.OnClickListener, ISelectableItemFragment {

    public static final String TAG = FragmentRack.class.getName();

    private TextView textViewRackAddress;
    private TextView textViewRackOwnership;
    private TextView textViewRackType;
    private TextView textViewRackCapacity;

    private BikeRackItem bikeRackItem;

    public static Fragment newInstance(BikeRackItem bikeRackItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(MapsActivity.EXTRA_BIKE_RACK_ITEM, bikeRackItem);

        FragmentRack fragment = new FragmentRack();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.fab_route_to_point:
                final Uri geoLocation = Uri.parse("geo:0,0?q=" + bikeRackItem.getAddress());
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
        final View view = inflater.inflate(R.layout.fragment_rack, container, false);

        view.setOnClickListener(this);

        textViewRackAddress = (TextView) view.findViewById(R.id.rack_text_view_address);
        textViewRackOwnership = (TextView) view.findViewById(R.id.rack_text_view_ownership);
        textViewRackType = (TextView) view.findViewById(R.id.rack_text_view_type);
        textViewRackCapacity = (TextView) view.findViewById(R.id.rack_text_view_capacity);

        setBikeRackItem(getArguments().<BikeRackItem>getParcelable(MapsActivity.EXTRA_BIKE_RACK_ITEM));

        final View fabRouteToPoint = view.findViewById(R.id.fab_route_to_point);
        fabRouteToPoint.setScaleY(0);
        fabRouteToPoint.setScaleX(0);
        fabRouteToPoint.setOnClickListener(this);
        fabRouteToPoint.animate()
                .scaleY(1)
                .scaleX(1)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        return view;
    }

    public void setBikeRackItem(BikeRackItem bikeRackItem) {
        this.bikeRackItem = bikeRackItem;
        updateRack();
    }

    private void updateRack() {
        final String ownership = getString(R.string.rack_ownership).replace("?", bikeRackItem.getOwnership());
        final String type = getString(R.string.rack_type).replace("?", bikeRackItem.getType());
        final String capacity = getString(R.string.rack_capacity).replace("?",
                Integer.toString(bikeRackItem.getCapacity()));

        textViewRackAddress.setText(bikeRackItem.getAddress());
        textViewRackOwnership.setText(ownership);
        textViewRackType.setText(type);
        textViewRackCapacity.setText(capacity);
    }

}
