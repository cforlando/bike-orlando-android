package com.codefororlando.transport.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codefororlando.transport.MapsActivity;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.data.BikeRackItem;

public class FragmentRack extends Fragment implements View.OnClickListener {

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
        getFragmentManager().beginTransaction()
                .setCustomAnimations(0, R.anim.slide_down)
                .hide(this)
                .commit();
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
