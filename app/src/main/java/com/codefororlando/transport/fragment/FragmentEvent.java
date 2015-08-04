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

import com.codefororlando.transport.EventListActivity;
import com.codefororlando.transport.IBroadcasts;
import com.codefororlando.transport.animation.EmptyAnimatorListener;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.data.EventItem;

public class FragmentEvent extends Fragment implements View.OnClickListener, ISelectableItemFragment, IBroadcasts {

    private TextView textViewEventName;
    private TextView textViewEventAddress;

    private EventItem eventItem;

    public static Fragment newInstance(EventItem eventItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_EVENT_ITEM, eventItem);

        Fragment fragment = new FragmentEvent();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_route_to_point: {
                final Uri geoLocation = Uri.parse("geo:0,0?q=" + eventItem.getName() + ", " + eventItem.getAddress());
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
            case R.id.event_button_list: {
                final Intent intent = new Intent(getActivity(), EventListActivity.class);
                intent.putExtra(EXTRA_EVENT_ITEM, eventItem);
                startActivity(intent);
                break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_event, container, false);

        textViewEventName = (TextView) view.findViewById(R.id.event_text_view_name);
        textViewEventAddress = (TextView) view.findViewById(R.id.event_text_view_address);
        view.findViewById(R.id.event_button_list).setOnClickListener(this);

        setEventItem(getArguments().<EventItem>getParcelable(EXTRA_EVENT_ITEM));

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

    public void setEventItem(EventItem eventItem) {
        this.eventItem = eventItem;
        updateEvent();
    }

    private void updateEvent() {
        textViewEventName.setText(eventItem.getName());
        textViewEventAddress.setText(eventItem.getAddress());
    }

}
