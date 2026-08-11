/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2010, Red Hat, and individual contributors
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

package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.jboss.jbosstm.xts.demo.services.state.ServiceStateManager;
import static com.jboss.jbosstm.xts.demo.services.restaurant.RestaurantConstants.*;

import javax.xml.ws.WebServiceException;
import java.io.*;

/**
 * The application logic for the Restaurant Service.
 * <p/>
 * Stores and manages seating reservations. The restaurant manager provides a book method
 * allowing the web service endpoint to book or unbook seats. It also provides getters
 * which allow the GUI to  monitor the state of the service while transactions are in
 * progress.
 * 
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @author Andrew Dinn (adinn@redhat.com)
 * @version $Revision:$
 */
public class RestaurantManager extends ServiceStateManager<RestaurantState> {

    /*****************************************************************************/
    /* Support for the Web Services                                              */
    /*****************************************************************************/

    /**
     * Accessor to obtain the singleton restaurant manager instance.
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
     * Book a number of seats in the restaurant.
     *
     * @param txID   The transaction identifier
     * @param nSeats The number of seats requested
     */
    public synchronized void bookSeats(Object txID, int nSeats)
    {
        // we cannot proceed while a prepare is in progress

        while (isLocked()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        if (currentState.freeSeats < nSeats ||
                currentState.bookedSeats + nSeats > currentState.totalSeats) {
            throw new WebServiceException("requested number of seats (" + nSeats + ") not available");
        }

        // create a state derived from the current state which reflects the new booking count

        RestaurantState childState = currentState.derivedState();

        // update the number of booked and free seats in the derived state

        childState.freeSeats -= nSeats;
        childState.bookedSeats += nSeats;

        // install this as the current transaction state

        putDerivedState(txID, childState);

    }

    /*****************************************************************************/
    /* Implementation of inherited abstract state management API                 */
    /*****************************************************************************/

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

    /**
     * identify the name of file used to store the current service state
      * @return the name of the file used to store the current service state
     */
    @Override
    public String getStateFilename() {
        return STATE_FILENAME;
    }

    /**
     * identify the name of file used to store the shadow service state
      * @return the name of the file used to store the shadow service state
     */
    @Override
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

        RestaurantState resetState = currentState.derivedState();

        resetState.totalSeats = DEFAULT_SEATING_CAPACITY;
        resetState.bookedSeats = 0;
        resetState.freeSeats = resetState.totalSeats;

        Object txId = "reset-transaction";
        try {
            writeShadowState(txId, resetState);
            commitShadowState(txId);
        } catch (IOException e) {
            clearShadowState(txId);
             System.out.println("error : unable to reset restaurant manager state " + e);
        }

        currentState = resetState;
        
        // remove any in-progress transactions

        clearTransactions();
    }

    /**
     * Get the number of free seats.
     *
     * @return The number of free seats
     */
    public int getNFreeSeats()
    {
        return currentState.freeSeats;
    }

    /**
     * Get the total number of seats.
     *
     * @return The total number of seats
     */
    public int getNTotalSeats()
    {
        return currentState.totalSeats;
    }

    /**
     * Get the number of booked seats in the given area.
     *
     * @return The number of booked seats
     */
    public int getNBookedSeats()
    {
        return currentState.bookedSeats;
    }

    /**
     * Get the number of prepared seats in the given area.
     *
     * @return The number of booked seats
     */
    public synchronized int getNPreparedSeats()
    {
        if (isLocked()) {
            RestaurantState childState = getPreparedState();
            return childState.bookedSeats - currentState.bookedSeats;
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
     *
     * @param commit true for commitment, false for rollback
     */
    public void setCommit(boolean commit)
    {
        isCommit = commit;
    }

    /*****************************************************************************/
    /* Private implementation                                                    */
    /*****************************************************************************/

    /**
     * The singleton instance of this class.
     */
    private static RestaurantManager singletonInstance;

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
     * Create and initialise a new RestaurantManager instance. If the super constructor does
     * not restore a previously persisted current state then create and persist an initial state
     * using appropriate default values.
     */
    private RestaurantManager()
    {
        super();
        if (currentState == null) {
            // we need to create a new initial state and persist it to disk
            currentState = RestaurantState.initialState();
            Object txId = "initialisation-transaction-" + System.currentTimeMillis();
            try {
                writeShadowState(txId, currentState);
                commitShadowState(txId);
            } catch (IOException e) {
                clearShadowState(txId);
                System.out.println("error : unable to initialise restaurant manager state " + e);
            }
        }

        preparation = new Object();

        isCommit = true;
        autoCommitMode = true;
        isPreparationWaiting = false;
    }
}
