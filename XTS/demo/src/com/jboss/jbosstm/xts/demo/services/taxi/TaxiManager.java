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
import java.io.*;

/**
 * The application logic for the Taxi Service
 * <p/>
 * Manages taxi reservations, providing prepare, commit and rollback calls for
 * modifying bookings <em>in memory only</em>. Taxis are an unlimited resource and it does
 * not really matter if a taxi does not turn up (there's always another one round the
 * corner) nor does it matter of the clients don't appear (someone else will take
 * the ride). so this manager does not maintain any persistent state and the bookings
 * it makes are not resilient to crashes.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @author Andrew Dinn (adinn@redhat.com)
 * @version $Revision: 1.3$
 */
public class TaxiManager implements Serializable
{
    /*****************************************************************************/
    /* Support for the Web Services                                              */
    /*****************************************************************************/

    /**
     * Accessor to obtain the singleton taxi manager instance.
     *
     * @return the singleton TaxiManager instance.
     */
    public synchronized static TaxiManager getSingletonInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new TaxiManager();
        }

        return singletonInstance;
    }

    /**
     * Book a taxi.
     * @param txID The transaction identifier
     */
    public synchronized void bookTaxi(Object txID)
    {
        // locate any pre-existing request for the same transaction
        Integer request = (Integer) transactions.get(txID);
        if (request == null)
        {
            // this is the first request for this
            // transaction - setup a record for it
            request = new Integer(0);
        }

        // record the request, keyed to its transaction scope
        transactions.put(txID, new Integer(request.intValue()));
    }

    /**
     * check whether we have already seen a web service request in a given transaction
     * @param txID The transaction identifier
     */

    public synchronized boolean knowsAbout(Object txID)
    {
        return transactions.get(txID) != null;
    }

    /*****************************************************************************/
    /* Support for the AT and BA Participant API implementation                  */
    /*****************************************************************************/

    /**
     * Prepare local state changes for the supplied transaction
     *
     * @param txID The transaction identifier
     * @return true on success, false otherwise
     */
    public synchronized boolean prepare(Object txID)
    {
        // ensure that we have seen this transaction before
        Integer request = (Integer) transactions.get(txID);
        if (request == null)
        {
            return false;
        }
        else
        {
            // see if we need user confirmation

            if (!confirmPrepare()) {
                transactions.remove(txID);
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * commit local state changes for the supplied transaction
     *
     * @param txID The transaction identifier
     */
    public synchronized void commit(Object txID)
    {
        // just need to remove the transaction from the hash map
        transactions.remove(txID);
    }

    /**
     * roll back local state changes for the supplied transaction
     *
     * @param txID The transaction identifier
     */
    public synchronized void rollback(Object txID)
    {
        // just need to remove the transaction from the hash map
        transactions.remove(txID);
    }

    /**
     * method called during prepare of local state changes allowing the user to force a prepare failure
     * @return true if the prepare should succeed and false if it should fail
     */
    public boolean confirmPrepare()
    {
        if (autoCommitMode) {
            return true;
        } else {
            // need to wait for the user to decide whether to go ahead or not with this participant
            isPreparationWaiting = true;
            synchronized (preparation) {
                try {
                    preparation.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            isPreparationWaiting = false;

            return isCommit;
        }
    }

    /*****************************************************************************/
    /* Accessors for the GUI to view and reset the service state                 */
    /*****************************************************************************/

    /**
     * Reset to the initial state.
     */
    public synchronized void reset()
    {
        // clear the hash map
        transactions.clear();
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

    /*****************************************************************************/
    /* Private implementation                                                    */
    /*****************************************************************************/

    /**
     * A singleton instance of this class.
     */
    private static TaxiManager singletonInstance;

    /**
     * The transactions we know about but which have not been prepared.
     */
    private Hashtable transactions;

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
     * The object used for wait/notify in manual commit mode.
     */
    private Object preparation;

    /**
     * The waiting status, when in manual commit mode.
     */
    private boolean isPreparationWaiting;

    /**
     * Create and initialise a new TaxiManager instance.
     */
    private TaxiManager()
    {
        transactions = new Hashtable();
        preparation = new Object();

        isCommit = true;
        autoCommitMode = true;
        isPreparationWaiting = false;
    }
}
