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
 * RestaurantManager.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 *
 * $Id: RestaurantManager.java,v 1.3 2004/04/21 13:09:18 jhalliday Exp $
 *
 */

package com.arjuna.xts.nightout.services.Restaurant;

import java.util.Hashtable;

/**
 * The transactional application logic for the Restaurant Service.
 * <p/>
 * Stores and manages seating reservations. Knows nothing about Web Services.
 * Understands transactional booking lifecycle: unprepared, prepared, finished.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantManager
{
    /**
     * Create and initialise a new RestaurantManager instance.
     */
    public RestaurantManager()
    {
        setToDefault();
    }

    /**
     * Book a number of seats in the restaurant.
     *
     * @param txID   The transaction identifier
     * @param nSeats The number of seats requested
     */
    public void bookSeats(Object txID, int nSeats)
    {
        // locate any pre-existing request for the same transaction
        Integer request = (Integer) unpreparedTransactions.get(txID);
        if (request == null)
        {
            // this is the first request for this transaction
            // setup the data structure to record it
            request = new Integer(0);
        }

        // record the request, keyed to its transaction scope
        request = new Integer(request.intValue() + nSeats);
        unpreparedTransactions.put(txID, request);

        // record the increased commitment to provide seating
        nBookedSeats += nSeats;
    }

    /**
     * Attempt to ensure availability of the requested seating.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean prepareSeats(Object txID)
    {
        // ensure that we have seen this transaction before
        Integer request = (Integer) unpreparedTransactions.get(txID);
        if (request == null)
        {
            return false; // error: transaction not registered
        }
        else
        {
            if (autoCommitMode)
            {
                if (request.intValue() <= nFreeSeats)
                {
                    // record the prepared transaction
                    preparedTransactions.put(txID, request);
                    unpreparedTransactions.remove(txID);
                    // mark the prepared seats as unavailable
                    nFreeSeats -= request.intValue();
                    nPreparedSeats += request.intValue();
                    return true;
                }
                else
                {
                    // we don't have enough seats available
                    return false;
                }
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
                        preparedTransactions.put(txID, request);
                        unpreparedTransactions.remove(txID);
                        // mark the prepared seats as unavailable
                        nFreeSeats -= request.intValue();
                        nPreparedSeats += request.intValue();
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                catch (Exception e)
                {
                    System.err.println("RestaurantManager.prepareSeats(): Unable to stop preparation.");
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
            Integer request = (Integer) preparedTransactions.remove(txID);
            nFreeSeats += request.intValue();
            nPreparedSeats -= request.intValue();
            nBookedSeats -= request.intValue();
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // undo the booking operations
            Integer request = (Integer) unpreparedTransactions.remove(txID);
            nBookedSeats -= request.intValue();
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
            Integer request = (Integer) preparedTransactions.remove(txID);
            nCommittedSeats += request.intValue();
            nPreparedSeats -= request.intValue();
            nBookedSeats -= request.intValue();
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // use one phase commit optimisation, skipping prepare
            Integer request = (Integer) unpreparedTransactions.remove(txID);
            nCommittedSeats += request.intValue();
            nFreeSeats -= request.intValue();
            nBookedSeats -= request.intValue();
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
     * Change the capacity of the Resaurant.
     *
     * @param nSeats The new capacity
     */
    public void newCapacity(int nSeats)
    {
        nFreeSeats += nSeats - nTotalSeats;
        nTotalSeats = nSeats;
    }

    /**
     * Get the number of free seats.
     *
     * @return The number of free seats
     */
    public int getNFreeSeats()
    {
        return nFreeSeats;
    }

    /**
     * Get the total number of seats.
     *
     * @return The total number of seats
     */
    public int getNTotalSeats()
    {
        return nTotalSeats;
    }

    /**
     * Get the number of booked seats in the given area.
     *
     * @return The number of booked seats
     */
    public int getNBookedSeats()
    {
        return nBookedSeats;
    }

    /**
     * Get the number of prepared seats.
     *
     * @return The number of prepared seats
     */
    public int getNPreparedSeats()
    {
        return nPreparedSeats;
    }

    /**
     * Get the number of committed seats in the given area.
     *
     * @return The number of committed seats
     */
    public int getNCommittedSeats()
    {
        return nCommittedSeats;
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
     *
     * @param commit true for commitment, false for rollback
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
        nTotalSeats = DEFAULT_SEATING_CAPACITY;
        nFreeSeats = nTotalSeats;
        nBookedSeats = 0;
        nPreparedSeats = 0;
        nCommittedSeats = 0;
        preparedTransactions = new Hashtable();
        unpreparedTransactions = new Hashtable();
        autoCommitMode = true;
        preparation = new Object();
        isPreparationWaiting = false;
        isCommit = true;
    }

    /**
     * Allow use of a singleton model for web services demo.
     *
     * @return the singleton RestaurantManager instance.
     */
    public static RestaurantManager getSingletonInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new RestaurantManager();
        }

        return singletonInstance;
    }

    /**
     * A singleton instance of this class.
     */
    private static RestaurantManager singletonInstance;

    /**
     * The total seating capacity.
     */
    private int nTotalSeats;

    /**
     * The number of free seats.
     */
    private int nFreeSeats;

    /**
     * The number of booked seats.
     * <p/>
     * Note: This may exceed the total seating capacity
     */
    private int nBookedSeats;

    /**
     * The number of prepared (promised) seats.
     */
    private int nPreparedSeats;
    /**
     * The number of committed seats in each area.
     */
    private int nCommittedSeats;

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
     * The default initial capacity of each seating area.
     */
    public static final int DEFAULT_SEATING_CAPACITY = 100;
}
