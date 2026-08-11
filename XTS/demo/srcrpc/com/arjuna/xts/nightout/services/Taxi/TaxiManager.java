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
 * TaxiManager.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 *
 * $Id: TaxiManager.java,v 1.3 2004/04/21 13:09:19 jhalliday Exp $
 *
 */

package com.arjuna.xts.nightout.services.Taxi;

import com.arjuna.wst.FaultedException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

/**
 * The transactional application logic for the Taxi Service
 * <p/>
 * Stores and manages taxi reservations. Knows nothing about Web Services.
 * Understands transactional booking lifecycle: unprepared, prepared, finished.
 * </p>
 * Extended to include support for BA compensation based rollback
 * </p>
 * The manager now maintains an extra list compensatableList
 * </p>
 * changes to preparedList and compensatableList are
 * always shadowed in persistent storage before returning control to clients.
 * </p>
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TaxiManager implements Serializable
{
    /**
     * Create and initialise a new TaxiManager instance.
     */
    private TaxiManager()
    {
        setToDefault(false);
        restoreState();
    }

    /**
     * Book a taxi.
     *
     * @param txID The transaction identifier
     */
    public synchronized void bookTaxi(Object txID)
    {
        // locate any pre-existing request for the same transaction
        Integer request = (Integer) unpreparedTransactions.get(txID);
        if (request == null)
        {
            // this is the first request for this
            // transaction - setup a record for it
            request = new Integer(0);
        }

        // record the request, keyed to its transaction scope
        unpreparedTransactions.put(txID, new Integer(request.intValue()));
        // we don't actually need to update until prepare
    }

    /**
     * Attempt to ensure availability of the requested taxi.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean prepareTaxi(Object txID)
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
                // record the prepared transaction
                preparedTransactions.put(txID, request);
                unpreparedTransactions.remove(txID);
                updateState();
                return true;
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
                    if (isCommit)
                    {
                        // record the prepared transaction
                        preparedTransactions.put(txID, request);
                        unpreparedTransactions.remove(txID);
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
                    System.err.println("TaxiManager.prepareTaxi(): Unable to stop preparation.");
                    return false;
                }
            }
        }
    }

    /**
     * Release a booked or prepared taxi.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean cancelTaxi(Object txID)
    {
        boolean success = false;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {
            // undo the prepare operations
            preparedTransactions.remove(txID);
            updateState();
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // undo the booking operations
            unpreparedTransactions.remove(txID);
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
     * Compensate a committed booking.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean compensateTaxi(Object txID)
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
                    System.err.println("TaxiManager.compensateTaxi(): Unexpected error during compensation.");
                    throw new FaultedException("TaxiManager.compensateTaxi(): compensation fault");
                }
            }

            // compensate the committed operation
            compensatableTransactions.remove(txID);
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
     * Commit taxi booking.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean commitTaxi(Object txID)
    {
        return commitTaxi(txID, false);
    }

    /**
     * Commit taxi booking, possibly allowing subsequent compensation.
     *
     * @param txID The transaction identifier
     * @param compensatable true if it may be necessary to compensate this commit laer
     * @return true on success, false otherwise
     */
    public synchronized boolean commitTaxi(Object txID, boolean compensatable)
    {
        boolean success = false;
        hasCommitted = true;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {
            // complete the prepared transaction
            Integer request =  (Integer)preparedTransactions.remove(txID);
            if (compensatable) {
                compensatableTransactions.put(txID, request);
            }
            updateState();
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            Integer request = (Integer)unpreparedTransactions.remove(txID);
            boolean doCommit;
            // check we are ok to go ahead and if so
            // use one phase commit optimisation, skipping prepare

            if (autoCommitMode)
            {
                doCommit = true;
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
                    compensatableTransactions.put(txID, request);
                    // we have to update state in this case
                    updateState();
                    success = true;
                } else {
                    // we don't have to update anything
                    success = true;
                }
            } else {
                // we don't have to update anything
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
     * Close taxi bookings, removing possibility for compensation.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean closeTaxi(Object txID)
    {
        boolean success;

        // the transaction may be compensatable or unknown

        if (compensatableTransactions.containsKey(txID))
        {
            // complete the prepared transaction
            compensatableTransactions.remove(txID);
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
        compensatableTransactions = new Hashtable();
        preparedTransactions = new Hashtable();
        unpreparedTransactions = new Hashtable();
        autoCommitMode = true;
        preparation = new Object();
        isPreparationWaiting = false;
        isCommit = false;
        hasCommitted = false;
        if (deleteSavedState) {
            // just write the current state.
            updateState();
        }
    }

    /**
     * Allow use of a singleton model for web services demo.
     */
    public synchronized static TaxiManager getSingletonInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new TaxiManager();
        }

        return singletonInstance;
    }

    public boolean hasBeenCommitted()
    {
        return hasCommitted;
    }

    public Hashtable getPreparedTransactions()
    {
        return preparedTransactions;
    }

    public Hashtable getUnpreparedTransactions()
    {
        return unpreparedTransactions;
    }

    /**
     * A singleton instance of this class.
     */
    private static TaxiManager singletonInstance;

    /**
     * The transactions we know about but which have not been prepared.
     */
    private Hashtable unpreparedTransactions;

    /**
     * The transactions we know about and are prepared to commit.
     */
    private Hashtable preparedTransactions;

    /**
     * The transactions we know about and are prepared to commit.
     */
    private Hashtable compensatableTransactions;

    /**
     * The auto commit mode.
     * <p/>
     * true = automatically commit, false = manually commit
     */
    private boolean autoCommitMode;

    /**
     * The user specified outcome when in manual commit mode.
     */
    private boolean isCommit;

    /**
     * If the participant has already been commmitted or not.
     */
    private boolean hasCommitted = false;

    /**
     * The object used for wait/notify in manual commit mode.
     */
    private Object preparation;

    /**
     * The waiting status, when in manual commit mode.
     */
    private boolean isPreparationWaiting;

    /**
     * the name of the file sued to store the restaurant manager state
     */
    final static private String STATE_FILENAME = "taxiManagerRPCState";

    /**
     * the name of the file sued to store the restaurant manager shadow state
     */
    final static private String SHADOW_STATE_FILENAME = "taxiManagerRPCShadowState";

    /**
     * load any previously saved manager state.
     *
     * n.b. can only be called once from the singleton constructor before save can be called
     * so there is no need for any synchronization here
     */

    private synchronized void restoreState()
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
    }

    /**
     * does the actual work of writing out the saved manager state
     * @param oos
     * @throws IOException
     */
    private void writeState(ObjectOutputStream oos) throws IOException
    {
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
