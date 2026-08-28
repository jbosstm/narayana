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
 * TheatreParticipantBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd.
 *
 * $Id: TheatreParticipantBA.java,v 1.3 2004/09/09 15:18:10 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.theatre;

import com.arjuna.wst.*;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import static com.jboss.jbosstm.xts.demo.services.theatre.TheatreConstants.*;
import java.io.Serializable;
import java.util.HashMap;

/**
 * An adapter class that exposes the TheatreManager as a WS-T Coordinator Completion
 * Business Activity participant. Also logs events to a TheatreView object.
 *
 * The Theatre Service allows up to three bookings in any given transaction, one for
 * each seating area. This means that teh service cannot decide when to complete.
 * After the first or, possibly, second booking request the service cannot determine
 * whether the client still wants to send another request. Hence it uses a participant
 * which implements the coordinator completion protocol. WHen the client asks the
 * coordinator to close the transaction the coordinator will tell the participant
 * to complete.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TheatreParticipantBA implements
        BusinessAgreementWithCoordinatorCompletionParticipant,
        ConfirmCompletedParticipant,
        Serializable
{
    /************************************************************************/
    /* public methods                                                   */
    /************************************************************************/
    /**
     * Participant instances are related to business method calls
     * in a one to one manner.
     *
     * @param txID  uniq id String for the transaction instance.
     * @param bookings holds counts of the seats booked in each of the three seating areas
     */
    public TheatreParticipantBA(String txID, int[] bookings)
    {
        // we need to save the txID for later use when logging
        // and the seat counts for use during compensation
        this.txID = txID;
        this.bookings = bookings;
    }

    /**
     * accessor for participant transaction id
     * @return the participant transaction id
     */
    public String getTxID() {
        return txID;
    }

    /************************************************************************/
    /* BusinessAgreementWithCoordinatorCompletionParticipant methods        */
    /************************************************************************/
    /**
     * The coordinator is informing the participant that all work it needs to
     * do within the scope of this business activity has been received.
     */
    public void complete() throws WrongStateException, SystemException
    {
        BAParticipantManager participantManager = managers.get(txID);
        getTheatreView().addPrepareMessage("id:" + txID + ". Attempting to prepare seats.");
        getTheatreView().updateFields();
        if (!getTheatreManager().prepare(txID))
        {
            // tell the participant manager we cannot complete. this will force the activity to fail
            getTheatreView().addMessage("id:" + txID + ". Failed to reserve seats. Cancelling.");
            getTheatreView().updateFields();
            try
            {
                participantManager.cannotComplete();
            }
            catch (Exception e)
            {
                System.err.println("bookSeats: 'cannotComplete' callback failed");
                e.printStackTrace(System.err);
            }
            removeParticipant(txID);
        }
        else
        {
            // we just need to return here. the XTS implementation will call confirmComplete
            // identifying whether or not to roll forward or roll back these prepared changes

            getTheatreView().addMessage("id:" + txID + ". Seats prepared");
            getTheatreView().updateFields();
        }
    }

    /**
     * The transaction has completed successfully. The participant previously
     * informed the coordinator that it was ready to complete.
     */
    public void close() throws WrongStateException, SystemException
    {
        // nothing to do here as the seats are already booked

        System.out.println("TheatreParticipantBA.close");

        getTheatreView().addMessage("id:" + txID + ". Close called on participant: " + this.getClass());

        getTheatreView().updateFields();

        removeParticipant(txID);
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     */
    public void cancel() throws WrongStateException, SystemException
    {
        // let the manager know that this activity has been cancelled

        System.out.println("TheatreParticipantBA.cancel");

        getTheatreManager().rollback(txID);

        getTheatreView().addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());

        getTheatreView().updateFields();

        removeParticipant(txID);
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     */

    public void compensate() throws FaultedException, WrongStateException, SystemException
    {
        System.out.println("TheatreParticipantBA.compensate");

        getTheatreView().addPrepareMessage("id:" + txID + ". Attempting to compensate participant: " + this.getClass().toString());

        getTheatreView().updateFields();

        // we perform the compensation by preparing and then committing local changes which
        // decrement the booked seat counts

        String compensationTxID = txID + "-compensation";

        for (int seatingArea = 0; seatingArea < NUM_SEAT_AREAS; seatingArea++) {
            if (bookings[seatingArea] != 0) {
                getTheatreManager().bookSeats(compensationTxID, -bookings[seatingArea], seatingArea);
            }
        }

        if (!getTheatreManager().prepare(compensationTxID)) {
            getTheatreView().addMessage("id:" + txID + ". Failed to compensate participant: " + this.getClass().toString());

            removeParticipant(txID);

            throw new FaultedException("Failed to compensate participant " + txID);
        }
        getTheatreManager().commit(compensationTxID);

        getTheatreView().addMessage("id:" + txID + ". Compensated participant: " + this.getClass().toString());

        getTheatreView().updateFields();

        removeParticipant(txID);
    }
    
    public String status()
    {
        return null ;
    }

    public void unknown() throws SystemException
    {
        removeParticipant(txID);
    }

    /**
     * If the participant enquired as to the status of the transaction it was
     * registered with and an unrecoverable error occurs then this operation will be
     * invoked.
     */
    public void error() throws SystemException
    {
        System.out.println("TheatreParticipantBA.error");

        getTheatreView().addMessage("id:" + txID + ". Received error for participant: " + this.getClass().toString());

        getTheatreView().updateFields();

        // roll back any prepared local state

        getTheatreManager().rollback(txID);

        getTheatreView().addMessage("id:" + txID + ". Notified error for participant: " + this.getClass().toString());

        getTheatreView().updateFields();

        removeParticipant(txID);
    }

    /************************************************************************/
    /* ConfirmCompletedParticipant methods                                  */
    /************************************************************************/

    /**
     * method called to perform commit or rollback of prepared changes to the underlying manager state after
     * the participant recovery record has been written
     *
     * @param confirmed true if the log record has been written and changes should be rolled forward and false
     * if it has not been written and changes should be rolled back
     */
    public void confirmCompleted(boolean confirmed) {
        if (confirmed) {
            getTheatreManager().commit(txID);
            getTheatreView().addMessage("id:" + txID + ". Seats committed");
            getTheatreView().updateFields();
        } else {
            getTheatreManager().rollback(txID);
            getTheatreView().addMessage("id:" + txID + ". Seats rolled back");
            getTheatreView().updateFields();
        }
    }

    /************************************************************************/
    /* tracking active participants                                         */
    /************************************************************************/
    /**
     * keep track of a participant and its participant manager
     * @param txID the participant's transaction identifier
     * @param participant the participant to be recorded
     * @param manager the participant manager to be recorded
     */
    public static synchronized void recordParticipant(String txID, TheatreParticipantBA participant, BAParticipantManager manager)
    {
        participants.put(txID, participant);
        managers.put(txID, manager);
    }

    /**
     * forget about a participant and its participant manager
     * @param txID the participant's transaction identifier
     * @return the removed participant
     */
    public static synchronized TheatreParticipantBA removeParticipant(String txID)
    {
        managers.remove(txID);
        return participants.remove(txID);
    }

    /**
     * lookup a participant
     * @param txID the participant's transaction identifier
     * @return the participant
     */
    public static synchronized TheatreParticipantBA getParticipant(String txID)
    {
        return participants.get(txID);
    }

    /**
     * lookup a participant manager
     * @param txID the participant's transaction identifier
     * @return the participant's manager
     */
    public static synchronized BAParticipantManager getManager(String txID)
    {
        return managers.get(txID);
    }

    /************************************************************************/
    /* private implementation                                               */
    /************************************************************************/
    /**
     * Id for the transaction which this participant instance relates to.
     * Set by the service (via contrtuctor) at enrolment time, this value
     * is used in informational log messages.
     */
    protected String txID;

    /**
     * array containing bookings for each of the seating areas. each area is booked in its own
     * service request but we need this info in order to be able to detect repeated bookings.
     */
    protected int[] bookings;

    private TheatreView getTheatreView() {
        return TheatreView.getSingletonInstance();
    }

    private TheatreManager getTheatreManager() {
        return TheatreManager.getSingletonInstance();
    }

    /**
     * table of currently active participants
     */
    private static HashMap<String, TheatreParticipantBA> participants = new HashMap<String, TheatreParticipantBA>();
    /**
     * table of currently active participant managers
     */
    private static HashMap<String, BAParticipantManager> managers = new HashMap<String, BAParticipantManager>();
}
