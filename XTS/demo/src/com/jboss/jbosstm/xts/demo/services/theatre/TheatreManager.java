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

package com.jboss.jbosstm.xts.demo.services.theatre;

import com.jboss.jbosstm.xts.demo.services.state.ServiceStateManager;

import javax.xml.ws.WebServiceException;

import static com.jboss.jbosstm.xts.demo.services.theatre.TheatreConstants.*;

import java.io.*;

/**
 * The transactional application logic for the Theatre Service.
 * <p/>
 * Stores and manages seating reservations.
 * <p/>
 * The manager extends class ServiceStateManager which implements a very simple
 * transactional resource manager. It gives the theatre manager the ability to
 * persist the web service state in a local disk file and to make transactional
 * updates to that persistent state. The unit of locking is the whole of the
 * service state so although bookings can be attempted by concurrent transactions
 * only one such booking will commit, forcing other concurrent transactions to
 * roll back. Conflict detection is implemented using a simple versioning scheme.
 *
 * The theatre manager provides a book method allowing the web service endpoint to book
 * or unbook seats. It also exposes prepare, commit and rollback operations used by both
 * WSAT and WSBA participants to drive prepare, commit and rollback of changes to the
 * persistent state. Finally it exposes recovery logic used by the WSAT and WSBA recovery
 * modules to tie recovery of WSAT and WSBA participants to recovery and rollback of the
 * local service state.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @author Andrew Dinn (adinn@redhat.com)
 * @version $Revision: 1.4 $
 */
public class TheatreManager extends ServiceStateManager<TheatreState>
{
    /*****************************************************************************/
    /* Support for the Web Service API                                           */
    /*****************************************************************************/

    /**
     * Accessor to obtain the singleton theatre manager instance.
     *
     * @return the singleton TheatreManager instance.
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
     * Book a number of seats in a specific area of the theatre.
     *
     * @param txID   The transaction identifier
     * @param nSeats The number of seats requested
     * @param area   The type of seating requested
     */
    public synchronized void bookSeats(Object txID, int nSeats, int area)
    {
        // we cannot proceed while a prepare is in progress

        while (isLocked()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        if (theatreState.freeSeats[area] < nSeats ||
                theatreState.bookedSeats[area] + nSeats > theatreState.totalSeats[area]) {
            throw new WebServiceException("requested number of seats (" + nSeats + ") not available");
        }

        // create a state derived from the current state which reflects the new booking count

        TheatreState childState =  theatreState.derivedState();

        // update the number of booked and free seats in the derived state

        childState.freeSeats[area] -= nSeats;
        childState.bookedSeats[area] += nSeats;

        // install this as the current transaction state
        
        putState(txID, childState);
    }

    /**
     * check whether we have already seen a web service request in a given transaction
     */

    public boolean knowsAbout(Object txID)
    {
        return getState(txID) != null;
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
    public boolean prepareSeats(Object txID)
    {
        // ensure that we have seen this transaction before
        TheatreState childState = getState(txID);
        if (childState == null) {
            return false;
        }
        // we have a single monolithic state element which means that only one transaction can prepare
        // at any given time. we lock this state at prepare by providing the txId as a locking id. it only
        // gets unlocked when we reach commit or rollback. the equivalent to the lock in memory is the
        // shadow state file on disk.
        synchronized (this) {
            while (isLocked()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            // check no other bookings have been committed

            if (!theatreState.isParentOf(childState)) {
                removeState(txID);
                return false;
            }

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
                    removeState(txID);
                    return false;
                }
            }

            // ok, so lock the state against other prepare/commits

            lock(txID);
        }
        // if we got here then no other changes have invalidated our booking and we have locked out
        // further changes until commit or rollback occurs. we write the derived child state to the
        // shadow state file before returning. if we crash after the write we will detect the shadow
        // state at reboot and restore the lock.
        try {
            writeShadowState(txID, childState);
            return true;
        } catch (Exception e) {
            clearShadowState(txID);
            synchronized (this) {
                removeState(txID);
                unlock();
            }
            System.err.println("RestaurantManager.prepareSeats(): Error attempting to prepare transaction: " + e);
            return false;
        }
    }

    /**
     * commit local state changes for the supplied transaction
     *
     * @param txID
     */
    public void commitSeats(Object txID)
    {
        synchronized (this) {
            // if there is a shadow state with this id then we need to copy the shadow state file over to the
            // real state file. it may be that there is no shadow state because this is a repeated commit
            // request. if so then we must have committed earlier so there is no harm done.
            if (isLockID(txID)) {
                commitShadowState(txID);
                // update the current state with the prepared state.
                theatreState = getPreparedState();
                unlock();
            }
            removeState(txID);
        }
    }

    /**
     * roll back local state changes for the supplied transaction
     * @param txID
     */
    public void rollbackSeats(Object txID)
    {
        synchronized (this) {
            removeState(txID);
            if (isLockID(txID)) {
                clearShadowState(txID);
                unlock();
            }
            this.notifyAll();
        }
    }

    /**
     * Handle BA error for a specific booking.
     *
     * @param txID The transaction identifier
     */
    public synchronized void error(Object txID)
    {
        rollbackSeats(txID);
    }

    /*****************************************************************************/
    /* Accessors for the GUI to view and reset the service state                 */
    /*****************************************************************************/

    /**
     * Reset to the initial state.
     *
     * @param txID   The transaction identifier
     * @param nSeats The number of seats requested
     */
    public synchronized void reset()
    {
        // we cannot proceed while a prepare is in progress

        while (isLocked()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // undo all existing bookings

        TheatreState resetState = theatreState.derivedState();

        for (int area = 0; area < NUM_SEAT_AREAS; area++) {
            resetState.totalSeats[area] = DEFAULT_SEATING_CAPACITY;
            resetState.bookedSeats[area] = 0;
            resetState.freeSeats[area] = DEFAULT_SEATING_CAPACITY;
        }

        Object txId = "reset-transaction";
        try {
            writeShadowState(txId, resetState);
            commitShadowState(txId);
        } catch (IOException e) {
            clearShadowState(txId);
             System.out.println("error : unable to reset theatre manager state " + e);
        }
        // remove any in-progress transactions

        clearTransactions();

        theatreState = resetState;
    }
    /**
     * Get the number of free seats in the given area.
     *
     * @param area The area of interest
     * @return The number of free seats
     */
    public int getNFreeSeats(int area)
    {
        return theatreState.freeSeats[area];
    }

    /**
     * Get the total number of seats in the given area.
     *
     * @param area The area of interest
     * @return The total number of seats
     */
    public int getNTotalSeats(int area)
    {
        return theatreState.totalSeats[area];
    }

    /**
     * Get the number of booked seats in the given area.
     *
     * @param area The area of interest
     * @return The number of booked seats
     */
    public int getNBookedSeats(int area)
    {
        return theatreState.bookedSeats[area];
    }

    /**
     * Get the number of prepared seats in the given area.
     *
     * @param area The area of interest
     * @return The number of prepared seats
     */
    public int getNPreparedSeats(int area)
    {
        if (isLocked()) {
            TheatreState childState = getPreparedState();
            return childState.bookedSeats[area] - theatreState.bookedSeats[area];
        } else {
            return 0;
        }
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
    /* Implementation of inherited abstract sate management API                  */
    /*****************************************************************************/

    /**
     * identify the name of file used to store the current service state
      * @return the name of the file used to store the current service state
     */
    public String getStateFilename() {
        return STATE_FILENAME;
    }

    /**
     * identify the name of file used to store the shadow service state
      * @return the name of the file used to store the shadow service state
     */
    public String getShadowStateFilename() {
        return SHADOW_STATE_FILENAME;
    }

    /*****************************************************************************/
    /* Recovery methods maintaining consistency of local and  WSAT/WSBA state    */
    /*****************************************************************************/

    /**
     * called by the AT recovery module when an AT participant is recovered from a log record
     */
    public void recovered(TheatreParticipantAT participant)
    {
        // if this AT participant matches the prepared TX id then we need to leave it prepared and locked
        // at the end of scanning so it can be completed at commit time
        if (isLockID(participant.txID)) {
            rollbackPreparedTx = false;
        }
    }

    /**
     * called by the BA recovery module when an AT participant is recovered from a log record
     */
    public void recovered(TheatreParticipantBA participant)
    {
        // if this AT participant matches the prepared TX id then we roll it forward here by calling
        // confirmCompleted so once again we don't need to roll back the prepared state
        if (isLockID(participant.txID)) {
            participant.confirmCompleted(true);
            rollbackPreparedTx = false;
        }
    }

    public void recoveryScanCompleted(int txType)
    {
        super.recoveryScanCompleted(txType);
        if (completedScans == TX_TYPE_BOTH && rollbackPreparedTx) {
            rollbackSeats(getLockID());
        }
    }

    /*****************************************************************************/
    /* Private implementation                                                    */
    /*****************************************************************************/

    /**
     * A singleton instance of this class.
     */
    private static TheatreManager singletonInstance;

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
     * Flag which determines whether we have to roll back any prepared changes to the server. We roll back
     * changes for an AT or BA participant if there is no associated log record because the participant never
     * prepared or completed, respectvely.. If we see a log record for an AT participant we leave the prepared
     * state behind since it will
     */
    private boolean rollbackPreparedTx;

    /**
     * the latest version of the theatre state which includes a version id
     * this state object is always stored on disk in the theatre state file
     * a prepared version of a derived child state may also exist on disk in the
     * theatre shadow state file
     */
    private TheatreState theatreState;

    /**
     * Create and initialise a new TheatreManager instance.
     */
    private TheatreManager()
    {
        TheatreState restoredState = restoreState();
        if (restoredState == null) {
            // we need to create a new initial state and persist it to disk
            restoredState = TheatreState.initialState();
            Object txId = "initialisation-transaction-" + System.currentTimeMillis();
            try {
                writeShadowState(txId, restoredState);
                commitShadowState(txId);
            } catch (IOException e) {
                clearShadowState(txId);
                System.out.println("error : unable to initialise theatre manager state " + e);
            }
        }
        theatreState = restoredState;

        preparation = new Object();
        // we will roll back any locally prepared changes to web service state unless we discover that
        // they are needed during recovery
        rollbackPreparedTx = isLocked();

        isCommit = true;
        autoCommitMode = true;
        isPreparationWaiting = false;
    }
}
