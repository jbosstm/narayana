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
 * The transactional application logic for the Taxi Service
 * <p/>
 * Manages taxi reservations. Knows nothing about Web Services.
 * <p/>
 * Taxis are an unlimited resource so this manager does not maintain any
 * persistent state.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TaxiManager implements Serializable
{
    /*****************************************************************************/
    /* Support for the Web Service API                                           */
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
     *
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
    public synchronized boolean prepareTaxi(Object txID)
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

            if (!autoCommitMode) {
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

                // process the user decision
                if (!isCommit) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * commit local state changes for the supplied transaction
     *
     * @param txID
     */
    public synchronized void commitTaxi(Object txID)
    {
        // just need to remove the transaction from the hash map
        transactions.remove(txID);
    }

    /**
     * roll back local state changes for the supplied transaction
     *
     * @param txID
     */
    public synchronized void rollbackTaxi(Object txID)
    {
        // just need to remove the transaction from the hash map
        transactions.remove(txID);
    }

    /**
     * handle a recovery error by rolling back the changes associated with the transaction
     * @param txID
     */
    public void error(String txID)
    {
        rollbackTaxi(txID);
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
    /* Recovery methods maintaining consistency of local and  WSAT/WSBA state    */
    /*****************************************************************************/

    /**
     * called by the AT recovery module when an AT participant is recovered from a log record
     */
    public void recovered(TaxiParticipantAT participant)
    {
        // nothing needed here
    }

    /**
     * called by the BA recovery module when an AT participant is recovered from a log record
     */
    public void recovered(TaxiParticipantBA participant)
    {
        // nothing needed here
    }

    public void recoveryScanCompleted(int txType)
    {
        // nothing needed here
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
