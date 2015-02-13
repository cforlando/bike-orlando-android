package com.codefororlando.transport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.ToxicBakery.widget.calendarview.CalendarScheduleView;
import com.ToxicBakery.widget.calendarview.OnEntryClickListener;
import com.ToxicBakery.widget.calendarview.data.ICalendarEntry;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.data.EventItem;
import com.codefororlando.transport.data.EventListings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class EventListActivity extends Activity implements EventListings.EventListingsListener, IBroadcasts, OnEntryClickListener {

    private final SimpleDateFormat incomingDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private EventItem eventItem;
    private CalendarScheduleView calendarScheduleView;
    private String priceRangeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        priceRangeFormat = getString(R.string.event_price_range_format);
        eventItem = getIntent().getExtras().getParcelable(EXTRA_EVENT_ITEM);

        calendarScheduleView = (CalendarScheduleView) findViewById(R.id.calendar_view);
        calendarScheduleView.setOnEntryClickListener(this);

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
            // Convert the events to calendar entries
            final Collection<EventListings.Event> events = eventListings.getEvents(selectedVenue);
            final Entry[] entries = new Entry[events.size()];
            int idx = 0;
            for (EventListings.Event event : events) {
                entries[idx++] = new Entry(event);
            }
            calendarScheduleView.addAllEntries(entries);
        }
    }

    @Override
    public void onEventListingsError(Exception e) {
        showError();
        e.printStackTrace();
    }

    @Override
    public void OnEntryClick(ICalendarEntry calendarEntry) {
        if (calendarEntry != null) {
            final EventListings.Event event = ((Entry) calendarEntry).getEvent();
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getUrl()));
            startActivity(intent);
        }
    }

    private void showError() {
        // FIXME put the error on the screen
        Toast.makeText(this, R.string.events_error_loading, Toast.LENGTH_LONG).show();
    }

    private final class Entry implements ICalendarEntry {

        private final EventListings.Event event;
        private final Date startDate;

        private Entry(EventListings.Event event) {
            this.event = event;

            try {
                startDate = incomingDateFormat.parse(event.getEventDateString());
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private EventListings.Event getEvent() {
            return event;
        }

        @NonNull
        @Override
        public String getTitle() {
            return event.getEventName();
        }

        @Override
        public String getSubTitle() {
            return String.format(priceRangeFormat, event.getMinPrice(), event.getMaxPrice());
        }

        @NonNull
        @Override
        public Date getDateStart() {
            return startDate;
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @NonNull
        @Override
        public ICalendarEntry clone() throws CloneNotSupportedException {
            return new Entry(event);
        }

    }

}
