/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * TheatreManager.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 *
 * $Id: TheatreManager.java,v 1.4 2004/04/21 13:09:20 jhalliday Exp $
 *
 */

package com.arjuna.xts.nightout.services.Theatre;

import java.util.Hashtable;

/**
 * The transactional application logic for the Theatre Service.
 * <p/>
 * Stores and manages seating reservations. Knows nothing about Web Services.
 * Understands transactional booking lifecycle: unprepared, prepared, finished.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.4 $
 */
public class TheatreManager
{
    /**
     * Create and initialise a new TheatreManager instance.
     */
    public TheatreManager()
    {
        setToDefault();
    }

    /**
     * Book a number of seats in a specific area of the theatre.
     *
     * @param txID   The transaction identifier
     * @param nSeats The number of seats requested
     * @param area   The type of seating requested
     */
    public void bookSeats(Object txID, int nSeats, int area)
    {
        // locate any pre-existing request for the same transaction
        Integer[] requests = (Integer[]) unpreparedTransactions.get(txID);
        if (requests == null)
        {
            // this is the first request for this transaction
            // setup the data structure to record it
            requests = new Integer[NUM_SEAT_AREAS];
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                requests[i] = new Integer(0);
            }
        }

        // record the request, keyed to its transaction scope
        requests[area] = new Integer(requests[area].intValue() + nSeats);
        unpreparedTransactions.put(txID, requests);

        // record the increased commitment to provide seating
        nBookedSeats[area] += nSeats;
    }

    /**
     * Attempt to ensure availability of the requested seating.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean prepareSeats(Object txID)
    {
        int[] nSeats = new int[NUM_SEAT_AREAS];

        // ensure that we have seen this transaction before
        Integer[] requests = (Integer[]) unpreparedTransactions.get(txID);
        if (requests == null)
        {
            return false; // error: transaction not registered
        }
        else
        {
            // determine the number of seats available
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                nSeats[i] = nFreeSeats[i];
                nSeats[i] -= requests[i].intValue();
            }
            if (autoCommitMode)
            {
                boolean success = true;
                // check we have enough seats avaiable
                for (int i = 0; i < NUM_SEAT_AREAS; i++)
                {
                    if (nSeats[i] < 0)
                    {
                        success = false; // error: not enough seats
                    }
                }
                if (success)
                {
                    // record the prepared transaction
                    preparedTransactions.put(txID, requests);
                    unpreparedTransactions.remove(txID);
                    // mark the prepared seats as unavailable
                    for (int i = 0; i < NUM_SEAT_AREAS; i++)
                    {
                        nFreeSeats[i] = nSeats[i];
                        nPreparedSeats[i] += requests[i].intValue();
                    }
                }
                return success;
            }
            else
            {
                try
                {
                    // wait for a user commit/rollback decision
                    isPreparationWaiting = true;
                    synchronized (preparation)
                    {
                        preparation.wait();
                    }
                    isPreparationWaiting = false;

                    // process the user decision
                    if (isCommit)
                    {
                        // record the prepared transaction
                        preparedTransactions.put(txID, requests);
                        unpreparedTransactions.remove(txID);
                        // mark the prepared seats as unavailable
                        for (int i = 0; i < NUM_SEAT_AREAS; i++)
                        {
                            nFreeSeats[i] = nSeats[i];
                            nPreparedSeats[i] += requests[i].intValue();
                        }
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                catch (Exception e)
                {
                    System.err.println("TheatreManager.prepareSeats(): Unable to stop preparation.");
                    return false;
                }
            }
        }
    }

    /**
     * Release booked or prepared seats.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean cancelSeats(Object txID)
    {
        boolean success = false;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {
            // undo the prepare operations
            Integer[] requests = (Integer[]) preparedTransactions.remove(txID);
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                nFreeSeats[i] += requests[i].intValue();
                nPreparedSeats[i] -= requests[i].intValue();
                nBookedSeats[i] -= requests[i].intValue();
            }
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // undo the booking operations
            Integer[] requests = (Integer[]) unpreparedTransactions.remove(txID);
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                nBookedSeats[i] -= requests[i].intValue();
            }
            success = true;
        }
        else
        {
            success = false; // error: transaction not registered
        }

        return success;
    }

    /**
     * Commit seat bookings.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean commitSeats(Object txID)
    {
        boolean success = false;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {

            // complete the prepared transaction
            Integer[] requests = (Integer[]) preparedTransactions.remove(txID);
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                nCommittedSeats[i] += requests[i].intValue();
                nPreparedSeats[i] -= requests[i].intValue();
                nBookedSeats[i] -= requests[i].intValue();
            }
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // use one phase commit optimisation, skipping prepare
            Integer[] requests = (Integer[]) unpreparedTransactions.remove(txID);
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                nCommittedSeats[i] += requests[i].intValue();
                nFreeSeats[i] -= requests[i].intValue();
                nBookedSeats[i] -= requests[i].intValue();
            }
            success = true;
        }
        else
        {
            success = false; // error: transaction not registered
        }

        return success;
    }

    /**
     * Determine if a specific transaction is known to the business logic.
     *
     * @param txID The uniq id for the transaction
     * @return true if the business logic is holding state related to the given txID,
     *         false otherwise.
     */
    public boolean knowsAbout(Object txID)
    {
        return (unpreparedTransactions.containsKey(txID) || preparedTransactions.containsKey(txID));
    }

    /**
     * Change the capacity of a given seating area.
     *
     * @param area   The seating area to change
     * @param nSeats The new capacity for the area
     */
    public void newCapacity(int area, int nSeats)
    {
        nFreeSeats[area] += nSeats - nTotalSeats[area];
        nTotalSeats[area] = nSeats;
    }

    /**
     * Get the number of free seats in the given area.
     *
     * @param area The area of interest
     * @return The number of free seats
     */
    public int getNFreeSeats(int area)
    {
        return nFreeSeats[area];
    }

    /**
     * Get the total number of seats in the given area.
     *
     * @param area The area of interest
     * @return The total number of seats
     */
    public int getNTotalSeats(int area)
    {
        return nTotalSeats[area];
    }

    /**
     * Get the number of booked seats in the given area.
     *
     * @param area The area of interest
     * @return The number of booked seats
     */
    public int getNBookedSeats(int area)
    {
        return nBookedSeats[area];
    }

    /**
     * Get the number of prepared seats in the given area.
     *
     * @param area The area of interest
     * @return The number of prepared seats
     */
    public int getNPreparedSeats(int area)
    {
        return nPreparedSeats[area];
    }

    /**
     * Get the number of committed seats in the given area.
     *
     * @param area The area of interest
     * @return The number of committed seats
     */
    public int getNCommittedSeats(int area)
    {
        return nCommittedSeats[area];
    }

    /**
     * Determine the autoCommit status of the instance.
     *
     * @return true if autoCommit mode is active, false otherwise
     */
    public boolean isAutoCommitMode()
    {
        return autoCommitMode;
    }

    /**
     * Set the autoCommit mode of the instance.
     *
     * @param autoCommit true for automatic commit, false for manual commit.
     */
    public void setAutoCommitMode(boolean autoCommit)
    {
        autoCommitMode = autoCommit;
    }

    /**
     * Get the preparation object for manual commit wait/notify.
     *
     * @return The preparation object
     */
    public Object getPreparation()
    {
        return preparation;
    }

    /**
     * Determine if the instance is waiting for manual commit/rollback.
     *
     * @return true if waiting, false otherwise
     */
    public boolean getIsPreparationWaiting()
    {
        return isPreparationWaiting;
    }

    /**
     * Set the waiting status of the instance.
     *
     * @param isWaiting The new value to set
     */
    public void setIsPreparationWaiting(boolean isWaiting)
    {
        isPreparationWaiting = isWaiting;
    }

    /**
     * Set the manual commit status.
     */
    public void setCommit(boolean commit)
    {
        isCommit = commit;
    }

    /**
     * (re-)initialise the instance data structures.
     */
    public void setToDefault()
    {
        nTotalSeats = new int[NUM_SEAT_AREAS];
        nFreeSeats = new int[NUM_SEAT_AREAS];
        nBookedSeats = new int[NUM_SEAT_AREAS];
        nPreparedSeats = new int[NUM_SEAT_AREAS];
        nCommittedSeats = new int[NUM_SEAT_AREAS];
        for (int i = 0; i < NUM_SEAT_AREAS; i++)
        {
            nTotalSeats[i] = DEFAULT_SEATING_CAPACITY;
            nFreeSeats[i] = nTotalSeats[i];
            nBookedSeats[i] = 0;
            nPreparedSeats[i] = 0;
            nCommittedSeats[i] = 0;
        }
        preparedTransactions = new Hashtable();
        unpreparedTransactions = new Hashtable();
        autoCommitMode = true;
        preparation = new Object();
        isPreparationWaiting = false;
        isCommit = true;
    }

    /**
     * Allow use of a singleton model for web services demo.
     */
    public static TheatreManager getSingletonInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new TheatreManager();
        }

        return singletonInstance;
    }

    /**
     * A singleton instance of this class.
     */
    private static TheatreManager singletonInstance;

    /*
     * The following arrays are indexed by seating type.
     *
     * nTotalSeats = ( nFreeSeats + nBookedSeats + nPreparedSeats )
     */

    /**
     * The total seating capacity of each area.
     */
    private int[] nTotalSeats;

    /**
     * The number of free seats in each area.
     */
    private int[] nFreeSeats;

    /**
     * The number of booked seats in each area.
     * <p/>
     * Note: This may exceed the total size of the area
     */
    private int[] nBookedSeats;

    /**
     * The number of prepared (promised) seats in each area.
     */
    private int[] nPreparedSeats;

    /**
     * The number of committed seats in each area.
     */
    private int[] nCommittedSeats;

    /**
     * The auto commit mode.
     * <p/>
     * true = automatically commit, false = manually commit
     */
    private boolean autoCommitMode;

    /**
     * The object used for wait/notify in manual commit mode.
     */
    private Object preparation;

    /**
     * The waiting status, when in manual commit mode.
     */
    private boolean isPreparationWaiting;

    /**
     * The user specified outcome when in manual commit mode.
     */
    private boolean isCommit;

    /**
     * The transactions we know about but which have not been prepared.
     */
    private Hashtable unpreparedTransactions;

    /**
     * The transactions we know about and are prepared to commit.
     */
    private Hashtable preparedTransactions;

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
}
