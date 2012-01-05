package com.jboss.jbosstm.xts.demo.services.theatre;

/**
 * Constant values used by the theatre service
 */
public class TheatreConstants
{
    /**
     * Constant (array index) used for the seating area CIRCLE.
     */
    public static final int CIRCLE = 0;

    /**
     * Constant (array index) used for the seating area STALLS.
     */
    public static final int STALLS = 1;

    /**
     * Constant (array index) used for the seating area BALCONY.
     */
    public static final int BALCONY = 2;

    /**
     * The total number (array size) of seating areas.
     */
    public static final int NUM_SEAT_AREAS = 3;

    /**
     * The default initial capacity of each seating area.
     */
    public static final int DEFAULT_SEATING_CAPACITY = 100;

    /**
     * the name of the file used to store the current theatre manager state
     */
    final static public String STATE_FILENAME = "theatreManagerState";

    /**
     * the name of the file used to store the shadow theatre manager state
     */
    final static public String SHADOW_STATE_FILENAME = "theatreManagerShadowState";
}
