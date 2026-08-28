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
 * TaxiParticipantBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd.
 *
 * $Id: TaxiParticipantBA.java,v 1.2 2004/09/09 15:18:11 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.taxi;

import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst11.ConfirmCompletedParticipant;

import java.io.Serializable;
import java.util.HashMap;

/**
 * An adapter class that exposes the TaxiManager transaction lifecycle
 * API as a WS-T Coordinator CompletionBusiness Activity participant.
 * Also logs events to a TaxiView object.
 *
 * The Taxi Service does not actually manage any persistent local state since
 * it does not really matter if a taxi or the clients fail to turn up (there
 * will always be another taxi or another client round the corner). It uses
 * a participant which employs the coordinator completion protocol but only
 * because this makes the demo more interesting.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.2 $
 */
public class TaxiParticipantBA
        implements BusinessAgreementWithCoordinatorCompletionParticipant,
        ConfirmCompletedParticipant,
        Serializable
{
    /************************************************************************/
    /* public methods                                                       */
    /************************************************************************/
    /**
     * Participant instances are related to business method calls
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public TaxiParticipantBA(String txID)
    {
        // we need to save the txID for later use when logging.
        this.txID = txID;
    }

    /**
     * accessor for participant transaction id
     * @return the participant transaction id
     */
    public String getTxID() {
        return txID;
    }

    /************************************************************************/
    /* BusinessAgreementWithParticipantCompletionParticipant methods        */
    /************************************************************************/

    
    public void complete() throws WrongStateException, SystemException {
        // prepare and then complete all local changes
        getTaxiView().addPrepareMessage("id:" + txID + ". Attempting to prepare taxi.");
        getTaxiView().updateFields();
        if (!getTaxiManager().prepare(txID))
        {
            // tell the participant manager we cannot complete. this will force the activity to fail
            getTaxiView().addMessage("id:" + txID + ". Failed to prepare taxi. Cancelling.");
            getTaxiView().updateFields();
            BAParticipantManager participantManager = getManager(txID);
            try
            {
                participantManager.cannotComplete();
            }
            catch (Exception e)
            {
                System.err.println("bookTaxi: 'cannotComplete' callback failed");
                e.printStackTrace(System.err);
            }
            removeParticipant(txID);
        }
        else
        {
            // we just need to return here. the XTS implementation will call confirmComplete
            // identifying whether or not to roll forward or roll back these prepared changes

            getTaxiView().addMessage("id:" + txID + ". Taxi prepared");
            getTaxiView().updateFields();
        }
    }
    /**
     * The activity has ended successfully. The participant previously
     * completed its local changes.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException never in this implementation.
     */

    public void close() throws WrongStateException, SystemException
    {
        // nothing to do here as the taxi is already booked


        System.out.println("TaxiParticipantBA.close");

        getTaxiView().addMessage("id:" + txID + ". Close called on participant: " + this.getClass());

        getTaxiView().updateFields();

        removeParticipant(txID);
    }

    /**
     * The transaction has cancelled, and the participant should undo any work.
     * The participant cannot have informed the coordinator that it has
     * completed.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException never in this implementation.
     */

    public void cancel() throws WrongStateException, SystemException
    {
        // let the manager know that this activity has been cancelled

        System.out.println("TaxiParticipantBA.cancel");

        getTaxiManager().rollback(txID);

        getTaxiView().addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());

        getTaxiView().updateFields();

        removeParticipant(txID);
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException always, because this implementation does not support compensation.
     */

    public void compensate() throws FaultedException, WrongStateException, SystemException
    {
        System.out.println("TaxiParticipantBA.compensate");

        getTaxiView().addPrepareMessage("id:" + txID + ". Attempting to compensate participant: " + this.getClass().toString());

        getTaxiView().updateFields();

        // there is no need to compensate a completed taxi booking as the taxi will just pick up another punter
        
        getTaxiView().addMessage("id:" + txID + ". Compensated participant: " + this.getClass().toString());
        getTaxiView().updateFields();

        removeParticipant(txID);
    }

    public String status () throws SystemException
    {
        return null;
    }

    public void unknown() throws SystemException
    {
        // used for callbacks during crash recovery. This impl is not recoverable
    }

    public void error() throws SystemException
    {
        System.out.println("TaxiParticipantBA.error");

        getTaxiView().addMessage("id:" + txID + ". Received error for participant: " + this.getClass().toString());

        getTaxiView().updateFields();

        // roll back any prepared local state

        getTaxiManager().rollback(txID);

        getTaxiView().addMessage("id:" + txID + ". Notified error for participant: " + this.getClass().toString());

        getTaxiView().updateFields();

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
            getTaxiView().addMessage("id:" + txID + ". Taxi committed");
            getTaxiView().updateFields();
            getTaxiManager().commit(txID);
        } else {
            getTaxiView().addMessage("id:" + txID + ". Taxi rolled back");
            getTaxiView().updateFields();
            getTaxiManager().rollback(txID);
        }
    }

    /************************************************************************/
    /* tracking active participants                                         */
    /************************************************************************/
    /**
     * keep track of a participant
     * @param txID the participant's transaction id
     * @param participant
     */
    public static synchronized void recordParticipant(String txID, TaxiParticipantBA participant, BAParticipantManager manager)
    {
        participants.put(txID, participant);
        managers.put(txID, manager);
    }

    /**
     * forget about a participant
     * @param txID the participant's transaction id
     */
    public static synchronized TaxiParticipantBA removeParticipant(String txID)
    {
        managers.remove(txID);
        return participants.remove(txID);
    }

    /**
     * lookup a participant
     * @param txID the participant's transaction id
     * @return the participant
     */
    public static synchronized TaxiParticipantBA getParticipant(String txID)
    {
        return participants.get(txID);
    }

    /**
     * lookup a participant manager
     * @param txID the participant's transaction id
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

    private TaxiView getTaxiView() {
        return TaxiView.getSingletonInstance();
    }

    private TaxiManager getTaxiManager() {
        return TaxiManager.getSingletonInstance();
    }

    /**
     * table of currently active participants
     */
    private static HashMap<String, TaxiParticipantBA> participants = new HashMap<String, TaxiParticipantBA>();
    /**
     * table of currently active participant managers
     */
    private static HashMap<String, BAParticipantManager> managers = new HashMap<String, BAParticipantManager>();
}
