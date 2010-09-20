package com.jboss.jbosstm.xts.demo.services.theatre;

import com.jboss.jbosstm.xts.demo.services.state.ServiceState;

import java.util.Arrays;

import static com.jboss.jbosstm.xts.demo.services.theatre.TheatreConstants.*;

/**
 * An object which models the state of a restaurant identifying the number of free and
 * booked seats and the total available
 */
public class TheatreState extends ServiceState {
    int[] totalSeats;
    int[] bookedSeats;
    int[] freeSeats;

    /**
     * create a new initial theatre state
     * @param count
     * @return
     */
    public static TheatreState initialState()
    {
        return new TheatreState();
    }
    /**
     * derive a child theatre state from this state
     * @return
     */
    public TheatreState derivedState()
    {
        return new TheatreState(this);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("TheatreState{version=");
        builder.append(version);
        builder.append(", totalSeats=[circle=");
        builder.append(totalSeats[CIRCLE]);
        builder.append(", STALLS=");
        builder.append(totalSeats[STALLS]);
        builder.append(", BALCONY=");
        builder.append(totalSeats[BALCONY]);
        builder.append("], bookedSeats=[circle=");
        builder.append(bookedSeats[CIRCLE]);
        builder.append(", STALLS=");
        builder.append(bookedSeats[STALLS]);
        builder.append(", BALCONY=");
        builder.append(bookedSeats[BALCONY]);
        builder.append("], freeSeats=[circle=");
        builder.append(freeSeats[CIRCLE]);
        builder.append(", STALLS=");
        builder.append(freeSeats[STALLS]);
        builder.append(", BALCONY=");
        builder.append(freeSeats[BALCONY]);
        builder.append("]}");
        return builder.toString();
    }
    /**
     * create a new initial restaurant state
     *
     * @param totalSeats
     */
    private TheatreState()
    {
        this.totalSeats = new int[NUM_SEAT_AREAS];
        this.bookedSeats = new int[NUM_SEAT_AREAS];
        this.freeSeats = new int[NUM_SEAT_AREAS];
        for (int i = 0; i < NUM_SEAT_AREAS; i++) {
            totalSeats[i] = DEFAULT_SEATING_CAPACITY;
            bookedSeats[i] = 0;
            freeSeats[i] = DEFAULT_SEATING_CAPACITY;
        }
    }

    /**
     * create a restaurant state with a given number of bookings and a specific version
     *
     * @param totalSeats
     * @param bookedSeats
     * @param version
     */
    private TheatreState(TheatreState parent)
    {
        super(parent);
        this.totalSeats =  Arrays.copyOf(parent.totalSeats, NUM_SEAT_AREAS);
        this.bookedSeats =  Arrays.copyOf(parent.bookedSeats, NUM_SEAT_AREAS);
        this.freeSeats =  Arrays.copyOf(parent.freeSeats, NUM_SEAT_AREAS);
    }

}
