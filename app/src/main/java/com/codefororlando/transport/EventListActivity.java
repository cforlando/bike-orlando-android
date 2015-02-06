package com.codefororlando.transport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.data.EventItem;
import com.codefororlando.transport.data.EventListings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventListActivity extends Activity implements EventListings.EventListingsListener, IBroadcasts {

    private EventItem eventItem;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        eventItem = getIntent().getExtras().getParcelable(EXTRA_EVENT_ITEM);
        adapter = new Adapter();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        EventListings.load(this);
    }

    @NonNull
    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onEventListingsLoaded(@NonNull EventListings eventListings) {
        final EventListings.Venue selectedVenue = eventListings.getVenueById(eventItem.getVenueId());
        if (selectedVenue == null) {
            showError();
        } else {
            adapter.setEventList(eventListings.getEvents(selectedVenue));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEventListingsError(Exception e) {
        showError();
        e.printStackTrace();
    }

    private void showError() {
        // FIXME put the error on the screen
        Toast.makeText(this, R.string.events_error_loading, Toast.LENGTH_LONG).show();
    }

    private static final class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private final List<EventListings.Event> eventList;

        private Adapter() {
            eventList = new ArrayList<>();
        }

        void setEventList(Collection<EventListings.Event> eventList) {
            this.eventList.clear();
            this.eventList.addAll(eventList);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final EventListings.Event event = eventList.get(position);
            holder.setEvent(event);
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

    }

    private static final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final DateFormat dateFormatInput;
        private final DateFormat dateFormatOutput;
        private final String priceRangeFormat;

        private final TextView textViewName;
        private final TextView textViewDate;
        private final TextView textViewPriceRange;

        private EventListings.Event event;

        private ViewHolder(View itemView) {
            super(itemView);

            priceRangeFormat = itemView.getContext().getString(R.string.event_price_range_format);
            dateFormatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
            dateFormatOutput = new SimpleDateFormat("MMM F, h:mma");

            textViewDate = (TextView) itemView.findViewById(R.id.event_date);
            textViewPriceRange = (TextView) itemView.findViewById(R.id.event_price_range);
            textViewName = (TextView) itemView.findViewById(R.id.event_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemView.equals(v)) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getUrl()));
                itemView.getContext().startActivity(intent);
            }
        }

        void setEvent(EventListings.Event event) {
            this.event = event;
            textViewName.setText(event.getEventName());
            textViewDate.setText(getDate(event));
            textViewPriceRange.setText(getPriceRange(event));
        }

        private String getPriceRange(EventListings.Event event) {
            return String.format(Locale.ENGLISH, priceRangeFormat, event.getMinPrice(), event.getMaxPrice());
        }

        private String getDate(EventListings.Event event) {
            try {
                final Date date = dateFormatInput.parse(event.getEventDateString() + ".000-05:00");
                return dateFormatOutput.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return "N/A";
            }
        }

    }

}
