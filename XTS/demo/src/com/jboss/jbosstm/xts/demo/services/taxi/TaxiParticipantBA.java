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

import com.arjuna.wst.*;

import java.io.Serializable;

/**
 * An adapter class that exposes the TaxiManager transaction lifecycle
 * API as a WS-T Business Activity participant.
 * Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.2 $
 */
public class TaxiParticipantBA
        implements BusinessAgreementWithParticipantCompletionParticipant,
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
        // nothing to do here as the taxi is already booked


        System.out.println("TaxiParticipantBA.close");

        getTaxiView().addMessage("id:" + txID + ". Close called on participant: " + this.getClass());

        getTaxiView().updateFields();
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

        getTaxiManager().rollbackTaxi(txID);

        getTaxiView().addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());

        getTaxiView().updateFields();
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
    }

    public String status () throws SystemException
    {
        return null;
    }

    public void unknown() throws SystemException
    {
        // used for calbacks during crash recovery. This impl is not recoverable
    }

    public void error() throws SystemException
    {
        System.out.println("TaxiParticipantBA.error");

        getTaxiView().addMessage("id:" + txID + ". Received error for participant: " + this.getClass().toString());

        getTaxiView().updateFields();

        // tell the manager we had an error

        getTaxiManager().error(txID);

        getTaxiView().addMessage("id:" + txID + ". Notified error for participant: " + this.getClass().toString());

        getTaxiView().updateFields();
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

    public TaxiView getTaxiView() {
        return TaxiView.getSingletonInstance();
    }

    public TaxiManager getTaxiManager() {
        return TaxiManager.getSingletonInstance();
    }
}
