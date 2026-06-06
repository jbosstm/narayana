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

import com.arjuna.wst.FaultedException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

/**
 * The transactional application logic for the Restaurant Service.
 * <p/>
 * Stores and manages seating reservations. Knows nothing about Web Services.
 * Understands transactional booking lifecycle: unprepared, prepared, finished.
 *
 * </p>The manager maintains the following invariants regarding seating capacity:
 * <ul>
 * <li>nBooked == sum(unpreparedList.seatCount) + sum(preparedList.seatCount)
 *
 * <li>nPrepared = sum(prepared.seatCount)
 *
 * <li>nTotal == nFree + nPrepared + nCommitted
 * </ul>
 * Extended to include support for BA compensation based rollback
 * </p>
 * The manager now maintains an extra list compensatableList:
 *  <ul>
 * <li>nCompensatable == sum(compensatableList.seatCount)
 * </ul>
 * changes to nPrepared, nFree, nCommitted, nCompensatable, nTotal, preparedList and compensatableList are
 * always shadowed in persistent storage before returning control to clients.
 * </p>
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantManager implements Serializable
{
    /**
     * Create and initialise a new RestaurantManager instance.
     */
    private RestaurantManager()
    {
        setToDefault(false);
        // restore any state saved by a previous installation of this web service
        restoreState();
    }

    /**
     * Book a number of seats in the restaurant.
     *
     * @param txID   The transaction identifier
     * @param nSeats The number of seats requested
     */
    public synchronized void bookSeats(Object txID, int nSeats)
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
        // we don't actually need to update until prepare
    }

    /**
     * Attempt to ensure availability of the requested seating.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean prepareSeats(Object txID)
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
                    updateState();

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
                        updateState();
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
    public synchronized boolean cancelSeats(Object txID)
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
            updateState();
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
     * Compensate a committed booking.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean compensateSeats(Object txID)
            throws FaultedException
    {
        boolean success = false;

        // the transaction must be compensatable

        if (compensatableTransactions.containsKey(txID))
        {
            // see if the user wants to report a compensation fault

            if (!autoCommitMode)
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
                    if (!isCommit)
                    {
                        throw new FaultedException("RestaurantManager.compensateSeats(): compensation fault");
                    }
                }
                catch (Exception e)
                {
                    System.err.println("RestaurantManager.compensateSeats(): Unexpected error during compensation.");
                    throw new FaultedException("RestaurantManager.compensateSeats(): compensation fault");
                }
            }

            // compensate the committed transaction
            Integer request = (Integer) compensatableTransactions.remove(txID);
            nCompensatableSeats -= request.intValue();

            nCommittedSeats -= request.intValue();
            nFreeSeats += request.intValue();
            updateState();
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
    public synchronized boolean commitSeats(Object txID)
    {
        return commitSeats(txID, false);
    }

    /**
     * Commit seat bookings, possibly allowing subsequent compensation.
     *
     * @param txID The transaction identifier
     * @param compensatable true if it may be necessary to compensate this commit laer
     * @return true on success, false otherwise
     */
    public synchronized boolean commitSeats(Object txID, boolean compensatable)
    {
        boolean success;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {
            // complete the prepared transaction
            Integer request = (Integer) preparedTransactions.remove(txID);
            if (compensatable) {
                nCompensatableSeats += request.intValue();
                compensatableTransactions.put(txID, request);
            }
            nCommittedSeats += request.intValue();
            nPreparedSeats -= request.intValue();
            nBookedSeats -= request.intValue();
            updateState();
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            Integer request = (Integer) unpreparedTransactions.remove(txID);
            boolean doCommit;
            // check we have enough seats and if so
            // use one phase commit optimisation, skipping prepare

            if (autoCommitMode)
            {
                if (request.intValue() <= nFreeSeats)
                {
                    doCommit = true;
                } else {
                    doCommit = false;
                }
            }
            else
            {
                try
                {
                    // wait for a user decision
                    isPreparationWaiting = true;
                    synchronized (preparation)
                    {
                        preparation.wait();
                    }
                    isPreparationWaiting = false;

                    // process the user decision
                    doCommit = isCommit;
                } catch (Exception e) {
                    System.err.println("RestaurantManager.commitSeats(): Unable to perform commit.");
                    doCommit = false;
                }
            }

            if (doCommit) {
                if (compensatable) {
                    nCompensatableSeats += request.intValue();
                    compensatableTransactions.put(txID, request);
                }
                nCommittedSeats += request.intValue();
                nFreeSeats -= request.intValue();
                nBookedSeats -= request.intValue();
                updateState();
                success = true;
            } else {
                // get rid of the commitment to keep these seats
                nBookedSeats -= request.intValue();
                success = false;
            }
        }
        else
        {
            success = false; // error: transaction not registered for compensation
        }

        return success;
    }

    /**
     * Close seat bookings, removing possibility for compensation.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean closeSeats(Object txID)
    {
        boolean success;

        // the transaction may be compensatable or unknown

        if (compensatableTransactions.containsKey(txID))
        {
            // complete the prepared transaction
            Integer request = (Integer) compensatableTransactions.remove(txID);

            nCompensatableSeats -= request.intValue();
            updateState();
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
     * Get the number of compensatable seats in the given area.
     *
     * @return The number of compensatable seats
     */
    public int getNCompensatableSeats()
    {
        return nCompensatableSeats;
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
     * (re-)initialise the instance data structures deleting any previously saved
     * transaction state.
     */
    public void setToDefault()
    {
        setToDefault(true);
    }

    /**
     * (re-)initialise the instance data structures, potentially committing any saved state
     * to disk
     * @param deleteSavedState true if any cached transaction state should be deleted otherwise false
     */
    public void setToDefault(boolean deleteSavedState)
    {
        nTotalSeats = DEFAULT_SEATING_CAPACITY;
        nFreeSeats = nTotalSeats;
        nBookedSeats = 0;
        nPreparedSeats = 0;
        nCommittedSeats = 0;
        nCompensatableSeats = 0;
        compensatableTransactions = new Hashtable();
        preparedTransactions = new Hashtable();
        unpreparedTransactions = new Hashtable();
        autoCommitMode = true;
        preparation = new Object();
        isPreparationWaiting = false;
        isCommit = true;
        if (deleteSavedState) {
            // just write the current state.
            updateState();
        }
    }

    /**
     * Allow use of a singleton model for web services demo.
     *
     * @return the singleton RestaurantManager instance.
     */
    public synchronized static RestaurantManager getSingletonInstance()
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
     * The number of compensatable seats in each area.
     */
    private int nCompensatableSeats;

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
     * The transactions we know about and are prepared to compensate.
     */
    private Hashtable compensatableTransactions;

    /**
     * The default initial capacity of each seating area.
     */
    public static final int DEFAULT_SEATING_CAPACITY = 100;

    /**
     * the name of the file used to store the restaurant manager state
     */
    final static private String STATE_FILENAME = "restaurantManagerRPCState";

    /**
     * the name of the file used to store the restaurant manager shadow state
     */
    final static private String SHADOW_STATE_FILENAME = "restaurantManagerRPCShadowState";

    /**
     * load any previously saved manager state
     *
     * n.b. can only be called once from the singleton constructor before save can be called
     * so there is no need for any synchronization here
     */

    private void restoreState()
    {
        File file = new File(STATE_FILENAME);
        File shadowFile = new File(SHADOW_STATE_FILENAME);
        if (file.exists()) {
            if (shadowFile.exists()) {
                // crashed during shadow file write == just trash it
                shadowFile.delete();
            }
        } else if (shadowFile.exists()) {
            // crashed afetr successful write - promote shadow file to real file
            shadowFile.renameTo(file);
            file = new File(STATE_FILENAME);
        }
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                readState(ois);
            } catch (Exception e) {
                System.out.println("error : could not restore restaurant manager state" + e);
            }
        } else {
            System.out.println("Starting with default restaurant manager state");
        }
    }

    /**
     * write the current manager state to a shadow disk file then commit it as the latest state
     * by relinking it to the current file
     *
     * n.b. must always called synchronized since the caller must always atomically check the
     * current state, modify it and write it.
     */
    private void updateState()
    {
        File file = new File(STATE_FILENAME);
        File shadowFile = new File(SHADOW_STATE_FILENAME);

        if (shadowFile.exists()) {
            // previous write must have barfed
            shadowFile.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(shadowFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            writeState(oos);
        } catch (Exception e) {
            System.out.println("error : could not restore restaurant manager state" + e);
        }

        shadowFile.renameTo(file);
    }

    /**
     * does the actual work of reading in the saved manager state
     *
     * @param ois
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readState(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        nTotalSeats = ois.readInt();
        nFreeSeats = ois.readInt();
        nPreparedSeats = ois.readInt();
        nCommittedSeats = ois.readInt();
        nCompensatableSeats = ois.readInt();
        compensatableTransactions = new Hashtable();
        String name = (String)ois.readObject();
        while (!"".equals(name)) {
            int count = ois.readInt();
            compensatableTransactions.put(name, new Integer(count));
            name = (String)ois.readObject();
        }
        preparedTransactions = new Hashtable();
        name = (String)ois.readObject();
        while (!"".equals(name)) {
            int count = ois.readInt();
            preparedTransactions.put(name, new Integer(count));
            name = (String)ois.readObject();
        }
        unpreparedTransactions = new Hashtable();
        // derive nBookedSeats from invariant
        nBookedSeats = nPreparedSeats;
        // assert invariant for total seats
        assert nTotalSeats == nFreeSeats + nPreparedSeats + nCommittedSeats;
    }

    /**
     * does the actual work of writing out the saved manager state
     * @param oos
     * @throws IOException
     */
    private void writeState(ObjectOutputStream oos) throws IOException
    {
        // assert invariant for total seats
        assert nTotalSeats == nFreeSeats + nPreparedSeats + nCommittedSeats;
        oos.writeInt(nTotalSeats);
        oos.writeInt(nFreeSeats);
        oos.writeInt(nPreparedSeats);
        oos.writeInt(nCommittedSeats);
        oos.writeInt(nCompensatableSeats);
        Enumeration keys = compensatableTransactions.keys();
        while (keys.hasMoreElements()) {
            String name = (String)keys.nextElement();
            int count = ((Integer)compensatableTransactions.get(name)).intValue();
            oos.writeObject(name);
            oos.writeInt(count);
        }
        oos.writeObject("");
        keys = preparedTransactions.keys();
        while (keys.hasMoreElements()) {
            String name = (String)keys.nextElement();
            int count = ((Integer)preparedTransactions.get(name)).intValue();
            oos.writeObject(name);
            oos.writeInt(count);
        }
        oos.writeObject("");
    }
}
