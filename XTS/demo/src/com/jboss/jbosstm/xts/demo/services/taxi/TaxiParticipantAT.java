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
 * TaxiParticipantAT.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd.
 *
 * $Id: TaxiParticipantAT.java,v 1.3 2005/02/23 09:58:02 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.taxi;

import com.arjuna.wst.*;

import java.io.Serializable;

/**
 * An adapter class that exposes the TaxiManager transaction lifecycle
 * API as a WS-T Atomic Transaction participant.
 * Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TaxiParticipantAT implements Durable2PCParticipant, Serializable
{
    /************************************************************************/
    /* public methods                                                       */
    /************************************************************************/
    /**
     * Participant instances are related to transaction instances
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public TaxiParticipantAT(String txID)
    {
        // we need to save the txID for later use when calling
        // business logic methods in the taxiManger.
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
    /* Durable2PCParticipant methods                                        */
    /************************************************************************/
    /**
     * Invokes the prepare step of the business logic,
     * reporting activity and outcome.
     *
     * @return Prepared where possible, Aborted where necessary.
     * @throws WrongStateException
     * @throws SystemException
     */
    public Vote prepare() throws WrongStateException, SystemException
    {
        // Log the event and invoke the prepare operation
        // on the backend business logic.

        System.out.println("TaxiParticipantAT.prepare");

        getTaxiView().addPrepareMessage("id:" + txID + ". Prepare called on participant: " + this.getClass().toString());

        boolean success = getTaxiManager().prepare(txID);

        // Log the outcome and map the return value from
        // the business logic to the appropriate Vote type.

        if (success)
        {
            getTaxiView().addMessage("Taxi prepared successfully. Returning 'Prepared'\n");
            getTaxiView().updateFields();
            return new Prepared();
        }
        else
        {
            getTaxiView().addMessage("Prepare failed (not enough Taxis?) Returning 'Aborted'\n");
            getTaxiView().updateFields();
            return new Aborted();
        }
    }

    /**
     * Invokes the commit step of the business logic,
     * reporting activity and outcome.
     *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void commit() throws WrongStateException, SystemException
    {
        // Log the event and invoke the commit operation
        // on the backend business logic.

        System.out.println("TaxiParticipantAT.commit");

        getTaxiView().addMessage("id:" + txID + ". Commit called on participant: " + this.getClass().toString());

        getTaxiManager().commit(txID);

        getTaxiView().addMessage("Taxi committed\n");

        getTaxiView().updateFields();
    }

    /**
     * Invokes the rollback operation on the business logic,
     * reporting activity and outcome.
     *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void rollback() throws WrongStateException, SystemException
    {
        // Log the event and invoke the rollback operation
        // on the backend business logic.

        System.out.println("TaxiParticipantAT.rollback");

        getTaxiView().addMessage("id:" + txID + ". Rollback called on participant: " + this.getClass().toString());

        getTaxiManager().rollback(txID);

        getTaxiView().addMessage("Taxi booking cancelled\n");

        getTaxiView().updateFields();
    }

    public void unknown() throws SystemException
    {
        // used for callbacks during crash recovery. This impl is not recoverable
    }

    public void error() throws SystemException
    {
        // used for callbacks during crash recovery. This impl is not recoverable
    }

    /**
     * Id for the transaction which this participant instance relates to.
     * Set by the service (via contrtuctor) at enrolment time, this value
     * is passed to the backend business logic methods.
     */
    protected String txID;

    public TaxiView getTaxiView() {
        return TaxiView.getSingletonInstance();
    }

    public TaxiManager getTaxiManager() {
        return TaxiManager.getSingletonInstance();
    }
}
