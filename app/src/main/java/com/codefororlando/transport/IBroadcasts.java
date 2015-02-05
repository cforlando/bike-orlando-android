package com.codefororlando.transport;

public interface IBroadcasts {

    /**
     * Bike item marker selected.
     * <p/>
     * Always includes {@link #EXTRA_BIKE_RACK_ITEM}.
     */
    public static final String ACTION_BIKE_MARKER_SELECTED = "ACTION_BIKE_MARKER_SELECTED";

    /**
     * Parking item marker selected.
     * <p/>
     * Always includes {@link #EXTRA_PARKING_ITEM}.
     */
    public static final String ACTION_PARKING_MARKER_SELECTED = "ACTION_PARKING_MARKER_SELECTED";

    /**
     * {@link com.codefororlando.transport.data.BikeRackItem} instance.
     */
    public static final String EXTRA_BIKE_RACK_ITEM = "EXTRA_BIKE_RACK_ITEM";

    /**
     * {@link com.codefororlando.transport.data.ParkingItem} instance.
     */
    public static final String EXTRA_PARKING_ITEM = "EXTRA_PARKING_ITEM";

}
