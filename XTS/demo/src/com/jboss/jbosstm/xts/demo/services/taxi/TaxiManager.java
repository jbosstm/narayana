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

package com.jboss.jbosstm.xts.demo.services.taxi;

import java.util.Hashtable;

/**
 * The transactional application logic for the Taxi Service
 * <p/>
 * Stores and manages taxi reservations. Knows nothing about Web Services.
 * Understands transactional booking lifecycle: unprepared, prepared, finished.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TaxiManager
{
    /**
     * Create and initialise a new TaxiManager instance.
     */
    public TaxiManager()
    {
        setToDefault();
    }

    /**
     * Book a taxi.
     *
     * @param txID The transaction identifier
     */
    public void bookTaxi(Object txID)
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
    }

    /**
     * Attempt to ensure availability of the requested taxi.
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public boolean prepareTaxi(Object txID)
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
    public boolean cancelTaxi(Object txID)
    {
        boolean success = false;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {
            // undo the prepare operations
            preparedTransactions.remove(txID);
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // undo the booking operations
            unpreparedTransactions.remove(txID);
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
    public boolean commitTaxi(Object txID)
    {
        boolean success = false;
        hasCommitted = true;

        // the transaction may be prepared, unprepared or unknown

        if (preparedTransactions.containsKey(txID))
        {
            // complete the prepared transaction
            preparedTransactions.remove(txID);
            success = true;
        }
        else if (unpreparedTransactions.containsKey(txID))
        {
            // use one phase commit optimisation, skipping prepare
            unpreparedTransactions.remove(txID);
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
        preparedTransactions = new Hashtable();
        unpreparedTransactions = new Hashtable();
        autoCommitMode = true;
        preparation = new Object();
        isPreparationWaiting = false;
        isCommit = false;
        hasCommitted = false;
    }

    /**
     * Allow use of a singleton model for web services demo.
     */
    public static TaxiManager getSingletonInstance()
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
}
