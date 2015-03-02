package com.codefororlando.transport.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.codefororlando.transport.bikeorlando.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class EventListings {

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration"})
    private Map<Integer, Venue> venues;

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration"})
    private Map<Integer, Event[]> events;

    public static synchronized void load(final EventListingsListener eventListingsListener) {
        final Context context = eventListingsListener.getContext();
        final String address = context.getString(R.string.event_listings_url);

        Ion.with(context)
                .load(address)
                .as(EventListings.class)
                .setCallback(new FutureCallback<EventListings>() {
                    @Override
                    public void onCompleted(Exception e, EventListings result) {
                        if (e == null) {
                            eventListingsListener.onEventListingsLoaded(result);
                        } else {
                            eventListingsListener.onEventListingsError(e);
                        }
                    }
                });

    }

    public Venue getVenueById(int id) {
        return venues.get(id);
    }

    public Collection<Venue> getVenues() {
        return venues.values();
    }

    public Collection<Event> getEvents(Venue venue) {
        return new LinkedList<>(Arrays.asList(events.get(venue.getVenueId())));
    }

    public static interface EventListingsListener {

        @NonNull
        Context
        getContext();

        void onEventListingsLoaded(@NonNull EventListings eventListings);

        void onEventListingsError(Exception e);

    }

    public static final class Venue {

        private int venueId;
        private String name;
        private String city;
        private String state;
        private String countryCode;
        private String timezone;

        public int getVenueId() {
            return venueId;
        }

        public String getName() {
            return name;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getTimezone() {
            return timezone;
        }

    }

    public static final class Event {

        private int eventId;
        private String eventName;
        private int minPrice;
        private int maxPrice;
        private String chartUrl;
        private String url;
        private String eventDateString;

        public int getEventId() {
            return eventId;
        }

        public String getEventName() {
            return eventName;
        }

        public int getMinPrice() {
            return minPrice;
        }

        public int getMaxPrice() {
            return maxPrice;
        }

        public String getChartUrl() {
            return chartUrl;
        }

        public String getUrl() {
            return url;
        }

        public String getEventDateString() {
            return eventDateString;
        }

    }

}
