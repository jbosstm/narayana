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
 * RestaurantParticipantBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd.
 *
 * $Id: RestaurantParticipantBA.java,v 1.3 2004/09/09 15:18:09 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.arjuna.wst.*;
import com.arjuna.wst11.ConfirmCompletedParticipant;

import java.io.Serializable;

/**
 * An adapter class that exposes the RestaurantManager transaction lifecycle
 * API as a WS-T Business Activity participant.
 * Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantParticipantBA
        implements BusinessAgreementWithParticipantCompletionParticipant,
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
     * @param txID     uniq id String for the transaction instance.
     * @param how_many seats to book/compensate.
     */
    public RestaurantParticipantBA(String txID, int how_many)
    {
        // we need to save the txID for later use when logging.
        this.txID = txID;
        this.seatCount = how_many;
    }

    /************************************************************************/
    /* BusinessAgreementWithParticipantCompletionParticipant methods        */
    /************************************************************************/
    /**
     * The transaction has completed successfully. The participant previously
     * informed the coordinator that it was ready to complete.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException never in this implementation.
     */

    public void close() throws WrongStateException, SystemException
    {
        // nothing to do here as the seats are already booked

        System.out.println("RestaurantParticipantBA.close");

        getRestaurantView().addMessage("id:" + txID + ". Close called on participant: " + this.getClass());

        getRestaurantView().updateFields();
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

        System.out.println("RestaurantParticipantBA.cancel");

        getRestaurantManager().rollbackSeats(txID);
        
        getRestaurantView().addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());

        getRestaurantView().updateFields();
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException if unable to perform the compensating transaction.
     */

    public void compensate() throws FaultedException, WrongStateException, SystemException
    {
        System.out.println("RestaurantParticipantBA.compensate");

        getRestaurantView().addPrepareMessage("id:" + txID + ". Attempting to compensate participant: " + this.getClass().toString());

        getRestaurantView().updateFields();

        // we perform the compensation by preparing and then committing a change which
        // decrements the bookings

        String compensationTxID = txID + "-compensation";

        getRestaurantManager().bookSeats(compensationTxID, -seatCount);
        
        if (!getRestaurantManager().prepareSeats(compensationTxID)) {
            getRestaurantView().addMessage("id:" + txID + ". Failed to compensate participant: " + this.getClass().toString());

            throw new FaultedException("Failed to compensate participant: " + this.getClass().toString());
        }
        getRestaurantManager().commitSeats(compensationTxID);
        
        getRestaurantView().addMessage("id:" + txID + ". Compensated participant: " + this.getClass().toString());

        getRestaurantView().updateFields();
    }

    public String status()
    {
        return null ;
    }
    
    public void unknown() throws SystemException
    {
    }

    public void error() throws SystemException
    {
        System.out.println("RestaurantParticipantBA.error");

        getRestaurantView().addMessage("id:" + txID + ". Received error for participant: " + this.getClass().toString());

        getRestaurantView().updateFields();

        // tell the manager we had an error

        getRestaurantManager().error(txID);

        getRestaurantView().addMessage("id:" + txID + ". Notified error for participant: " + this.getClass().toString());

        getRestaurantView().updateFields();
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
            getRestaurantManager().commitSeats(txID);
        } else {
            getRestaurantManager().rollbackSeats(txID);
        }
    }

    /************************************************************************/
    /* private implementation                                               */
    /************************************************************************/
    /**
     * Id for the transaction which this participant instance relates to.
     * Set by the service (via constructor) at enrolment time, this value
     * is used in informational log messages.
     */
    protected String txID;

    /**
     * Copy of business state information, may be needed during compensation.
     */
    private int seatCount;

    private RestaurantView getRestaurantView() {
        return RestaurantView.getSingletonInstance();
    }

    private RestaurantManager getRestaurantManager() {
        return RestaurantManager.getSingletonInstance();
    }
}

