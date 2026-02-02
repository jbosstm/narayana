package com.jboss.jbosstm.xts.demo.services.restaurant;

/**
 * Constant values used by the restaurant service
 */
public class RestaurantConstants
{
    /**
     * The default initial capacity of each seating area.
     */
    public static final int DEFAULT_SEATING_CAPACITY = 100;

    /**
     * the name of the file used to persist the current restaurant state
     */
    public final static String STATE_FILENAME = "restaurantManagerState";

    /**
     * the name of the file used to persist the shadow restaurant state
     */
    public final static String SHADOW_STATE_FILENAME = "restaurantManagerShadowState";

}
