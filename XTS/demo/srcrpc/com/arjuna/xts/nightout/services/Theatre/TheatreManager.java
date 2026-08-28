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

import com.arjuna.wst.FaultedException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

/**
 * The transactional application logic for the Theatre Service.
 * <p/>
 * Stores and manages seating reservations. Knows nothing about Web Services.
 * Understands transactional booking lifecycle: unprepared, prepared, finished.
 *
 * </p>The manager maintains the following invariants regarding seating capacity:
 * <ul>
 * <li>nBooked[area] == sum(unpreparedList.seatCount[area]) + sum(preparedList.seatCount[area])
 *
 * <li>nPrepared[area] = sum(prepared.seatCount[area])
 *
 * <li>nTotal[area] == nFree[area] + nPrepared[area] + nCommitted[area]
 * </ul>
 * Extended to include support for BA compensation based rollback
 * </p>
 * The manager now maintains an extra list compensatableList:
 *  <ul>
 * <li>nCompensatable[area] == sum(compensatableList.seatCount[area])
 * </ul>
 * changes to nPrepared, nFree, nCommitted, nCompensatable, nTotal, preparedList and compensatableList are
 * always shadowed in persistent storage before returning control to clients.
 * </p>
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.4 $
 */
public class TheatreManager implements Serializable
{
    /**
     * Create and initialise a new TheatreManager instance.
     */
    public TheatreManager()
    {
        setToDefault(false);
        restoreState();
    }

    /**
     * Book a number of seats in a specific area of the theatre.
     *
     * @param txID   The transaction identifier
     * @param nSeats The number of seats requested
     * @param area   The type of seating requested
     */
    public synchronized void bookSeats(Object txID, int nSeats, int area)
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
                    updateState();
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
            updateState();
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
            // we don't need to update state
            success = true;
        }
        else
        {
            success = false; // error: transaction not registered
        }

        return success;
    }

    /**
     * Compensate a booking.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean compensateSeats(Object txID)
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
                        throw new FaultedException("TheatreManager.compensateSeats(): compensation fault");
                    }
                }
                catch (Exception e)
                {
                    System.err.println("TheatreManager.compensateSeats(): Unexpected error during compensation.");
                    throw new FaultedException("TheatreManager.compensateSeats(): compensation fault");
                }
            }

            // compensate the prepared transaction
            Integer[] requests = (Integer[]) compensatableTransactions.remove(txID);
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                nCompensatableSeats[i] -= requests[i].intValue();
                nCommittedSeats[i] -= requests[i].intValue();
                nFreeSeats[i] += requests[i].intValue();
            }
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
    public boolean commitSeats(Object txID)
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
        boolean success = false;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {

            // complete the prepared transaction
            Integer[] requests = (Integer[]) preparedTransactions.remove(txID);
            if (compensatable)
            {
                compensatableTransactions.put(txID, requests);
            }
            for (int i = 0; i < NUM_SEAT_AREAS; i++)
            {
                if (compensatable) {
                    nCompensatableSeats[i] += requests[i].intValue();
                }
                nCommittedSeats[i] += requests[i].intValue();
                nPreparedSeats[i] -= requests[i].intValue();
                nBookedSeats[i] -= requests[i].intValue();
            }
            updateState();
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // use one phase commit optimisation, skipping prepare
            Integer[] requests = (Integer[]) unpreparedTransactions.remove(txID);
            boolean doCommit = true;
            // check we have enough seats and if so
            // use one phase commit optimisation, skipping prepare

            if (autoCommitMode)
            {
                for (int i = 0; doCommit && i < NUM_SEAT_AREAS; i++)
                {
                    if (requests[i].intValue() > nFreeSeats[i])
                    {
                        doCommit = false;
                    }
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
                    System.err.println("TheatreManager.commitSeats(): Unable to perform commit.");
                    doCommit = false;
                }
            }

            if (doCommit) {
                if (compensatable) {
                    compensatableTransactions.put(txID, requests);
                }
                for (int i = 0; i < NUM_SEAT_AREAS; i++)
                {
                    if (compensatable) {
                        nCompensatableSeats[i] += requests[i].intValue();
                    }
                    nCommittedSeats[i] += requests[i].intValue();
                    nFreeSeats[i] -= requests[i].intValue();
                    nBookedSeats[i] -= requests[i].intValue();
                }
                updateState();
                success = true;
            } else {
                // get rid of the commitment to keep these seats
                for (int i = 0; i < NUM_SEAT_AREAS; i++)
                {
                    nBookedSeats[i] -= requests[i].intValue();
                }
                success = false;
            }
        }
        else
        {
            success = false; // error: transaction not registered
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
            Integer[] requests = (Integer[]) compensatableTransactions.remove(txID);
            for (int i = 0; i < NUM_SEAT_AREAS; i++) {
                nCompensatableSeats[i] -= requests[i].intValue();
            }
            updateState();
            success = true;
        }
        else
        {
            success = false; // error: transaction not registered for compensation
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
     * Get the number of compensatable seats in the given area.
     *
     * @return The number of compensatable seats
     */
    public int getNCompensatableSeats(int area)
    {
        return nCompensatableSeats[area];
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
        nTotalSeats = new int[NUM_SEAT_AREAS];
        nFreeSeats = new int[NUM_SEAT_AREAS];
        nBookedSeats = new int[NUM_SEAT_AREAS];
        nPreparedSeats = new int[NUM_SEAT_AREAS];
        nCommittedSeats = new int[NUM_SEAT_AREAS];
        nCompensatableSeats = new int[NUM_SEAT_AREAS];
        for (int i = 0; i < NUM_SEAT_AREAS; i++)
        {
            nTotalSeats[i] = DEFAULT_SEATING_CAPACITY;
            nFreeSeats[i] = nTotalSeats[i];
            nBookedSeats[i] = 0;
            nPreparedSeats[i] = 0;
            nCommittedSeats[i] = 0;
            nCompensatableSeats[i] = 0;
        }
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
     */
    public synchronized static TheatreManager getSingletonInstance()
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
     * The number of compensatable seats in each area.
     */
    private int[] nCompensatableSeats;

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
     * the name of the file sued to store the restaurant manager state
     */
    final static private String STATE_FILENAME = "theatreManagerRPCState";

    /**
     * the name of the file sued to store the restaurant manager shadow state
     */
    final static private String SHADOW_STATE_FILENAME = "theatreManagerRPCShadowState";

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
        for (int i = 0; i < NUM_SEAT_AREAS; i++) {
            nTotalSeats[i] = ois.readInt();
            nFreeSeats[i] = ois.readInt();
            nPreparedSeats[i] = ois.readInt();
            nCommittedSeats[i] = ois.readInt();
            nCompensatableSeats[i] = ois.readInt();
        }
        compensatableTransactions = new Hashtable();
        String name = (String)ois.readObject();
        while (!"".equals(name)) {
            Integer[] counts = new Integer[NUM_SEAT_AREAS];
            for (int i = 0; i < NUM_SEAT_AREAS; i++) {
                int count = ois.readInt();
                counts[i] = new Integer(count);
            }
            compensatableTransactions.put(name, counts);
            name = (String)ois.readObject();
        }
        preparedTransactions = new Hashtable();
        name = (String)ois.readObject();
        while (!"".equals(name)) {
            Integer[] counts = new Integer[NUM_SEAT_AREAS];
            for (int i = 0; i < NUM_SEAT_AREAS; i++) {
                int count = ois.readInt();
                counts[i] = new Integer(count);
            }
            preparedTransactions.put(name, counts);
            name = (String)ois.readObject();
        }
        unpreparedTransactions = new Hashtable();
        for (int i = 0; i < NUM_SEAT_AREAS; i++) {
            // derive nBookedSeats from invariant
            nBookedSeats[i] = nPreparedSeats[i];
            // assert invariant for total seats
            assert nTotalSeats[i] == nFreeSeats[i] + nPreparedSeats[i] + nCommittedSeats[i];
        }
    }

    /**
     * does the actual work of writing out the saved manager state
     * @param oos
     * @throws IOException
     */
    private void writeState(ObjectOutputStream oos) throws IOException
    {
        for (int i = 0; i < NUM_SEAT_AREAS; i++) {
            // assert invariant for total seats
            assert nTotalSeats[i] == nFreeSeats[i] + nPreparedSeats[i] + nCommittedSeats[i];
            oos.writeInt(nTotalSeats[i]);
            oos.writeInt(nFreeSeats[i]);
            oos.writeInt(nPreparedSeats[i]);
            oos.writeInt(nCommittedSeats[i]);
            oos.writeInt(nCompensatableSeats[i]);
        }
        Enumeration keys = compensatableTransactions.keys();
        while (keys.hasMoreElements()) {
            String name = (String)keys.nextElement();
            Integer[] counts = (Integer[]) compensatableTransactions.get(name);
            oos.writeObject(name);
            for (int i = 0; i < NUM_SEAT_AREAS; i++) {
                oos.writeInt(counts[i].intValue());
            }
        }
        oos.writeObject("");
        keys = preparedTransactions.keys();
        while (keys.hasMoreElements()) {
            String name = (String)keys.nextElement();
            Integer[] counts = (Integer[]) preparedTransactions.get(name);
            oos.writeObject(name);
            for (int i = 0; i < NUM_SEAT_AREAS; i++) {
                oos.writeInt(counts[i].intValue());
            }
        }
        oos.writeObject("");
    }
}
