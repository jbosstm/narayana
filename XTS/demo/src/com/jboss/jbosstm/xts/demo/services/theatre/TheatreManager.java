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
 * The application logic for the Theatre Service.
 * <p/>
 * Stores and manages seating reservations. The theatre manager provides a book method
 * allowing the web service endpoint to book or unbook seats. It also provides getters
 * which allow the GUI to  monitor the state of the service while transactions are in
 * progress.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @author Andrew Dinn (adinn@redhat.com)
 * @version $Revision: 1.4 $
 */
public class TheatreManager extends ServiceStateManager<TheatreState>
{
    /*****************************************************************************/
    /* Support for the Web Services                                              */
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

        // see if we already have a derived state from booking a previous area

        TheatreState childState = getDerivedState(txID);

        if (childState != null) {
            // see if we can extend the booking for this new area
            if (childState.freeSeats[area] < nSeats ||
                    childState.bookedSeats[area] + nSeats > childState.totalSeats[area]) {
                throw new WebServiceException("requested number of seats (" + nSeats + ") not available");
            }

            // update the number of booked and free seats in the derived state to reflect this request

            childState.freeSeats[area] -= nSeats;
            childState.bookedSeats[area] += nSeats;
        } else {
            // see if we can derive a new state from the current state which will satisfy this request
            if (currentState.freeSeats[area] < nSeats ||
                    currentState.bookedSeats[area] + nSeats > currentState.totalSeats[area]) {
                throw new WebServiceException("requested number of seats (" + nSeats + ") not available");
            }

            childState = currentState.derivedState();

            // install this as the current transaction state
            putDerivedState(txID, childState);

            // update the number of booked and free seats in the derived state to reflect this request

            childState.freeSeats[area] -= nSeats;
            childState.bookedSeats[area] += nSeats;
        }
    }

    /*****************************************************************************/
    /* Implementation of inherited abstract state management API                 */
    /*****************************************************************************/

    /**
     * method called during prepare of local state changes allowing the user to force a prepare failue
     * @return true if the prepare shoudl succeed and false if it should fail
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
    /* Accessors for the GUI to view and reset the service state                 */
    /*****************************************************************************/

    /**
     * Reset to the initial state.
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

        TheatreState resetState = currentState.derivedState();

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

        currentState = resetState;
    }
    /**
     * Get the number of free seats in the given area.
     *
     * @param area The area of interest
     * @return The number of free seats
     */
    public int getNFreeSeats(int area)
    {
        return currentState.freeSeats[area];
    }

    /**
     * Get the total number of seats in the given area.
     *
     * @param area The area of interest
     * @return The total number of seats
     */
    public int getNTotalSeats(int area)
    {
        return currentState.totalSeats[area];
    }

    /**
     * Get the number of booked seats in the given area.
     *
     * @param area The area of interest
     * @return The number of booked seats
     */
    public int getNBookedSeats(int area)
    {
        return currentState.bookedSeats[area];
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
            return childState.bookedSeats[area] - currentState.bookedSeats[area];
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
     * Create and initialise a new TheatreManager instance. If the super constructor does
     * not restore a previously persisted current state then create and persist an initial state
     * using appropriate default values.
     */
    private TheatreManager()
    {
        super();
        if (currentState == null) {
            // we need to create a new initial state and persist it to disk
            currentState = TheatreState.initialState();
            Object txId = "initialisation-transaction-" + System.currentTimeMillis();
            try {
                writeShadowState(txId, currentState);
                commitShadowState(txId);
            } catch (IOException e) {
                clearShadowState(txId);
                System.out.println("error : unable to initialise theatre manager state " + e);
            }
        }

        preparation = new Object();

        isCommit = true;
        autoCommitMode = true;
        isPreparationWaiting = false;
    }
}
