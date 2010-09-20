package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.jboss.jbosstm.xts.demo.services.state.ServiceState;
import static com.jboss.jbosstm.xts.demo.services.restaurant.RestaurantConstants.*;

/**
 * An object which models the state of a restaurant identifying the number of free and
 * booked seats and the total available
 */
public class RestaurantState extends ServiceState {
    int totalSeats;
    int bookedSeats;
    int freeSeats;

    /**
     * create a new initial restaurant state
     * @return
     */
    public static RestaurantState initialState()
    {
        return new RestaurantState();
    }

    /**
     * derive a child restaurant state from this state
     * @return
     */
    public RestaurantState derivedState()
    {
        return new RestaurantState(this);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RestaurantState{version=");
        builder.append(version);
        builder.append(", totalSeats=");
        builder.append(totalSeats);
        builder.append(", bookedSeats=");
        builder.append(bookedSeats);
        builder.append(", freeSeats=");
        builder.append(freeSeats);
        builder.append("}");
        return builder.toString();
    }
    /**
     * create a new initial restaurant state
     *
     * @param totalSeats
     */
    private RestaurantState()
    {
        super();
        this.totalSeats = DEFAULT_SEATING_CAPACITY;
        this.bookedSeats = 0;
        this.freeSeats = DEFAULT_SEATING_CAPACITY;
    }

    /**
     * create a derived restaurant state with a given number of bookings and a specific version
     *
     * @param totalSeats
     * @param bookedSeats
     * @param version
     */
    private RestaurantState(RestaurantState parent)
    {
        super(parent);
        this.totalSeats = parent.totalSeats;
        this.bookedSeats = parent.bookedSeats;
        this.freeSeats = totalSeats - bookedSeats;
    }

}
