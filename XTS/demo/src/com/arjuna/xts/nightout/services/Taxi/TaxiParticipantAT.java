/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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

package com.arjuna.xts.nightout.services.Taxi;

import com.arjuna.wst.*;

/**
 * An adapter class that exposes the TaxiManager transaction lifecycle
 * API as a WS-T Atomic Transaction participant.
 * Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TaxiParticipantAT implements Durable2PCParticipant
{
    /**
     * Participant instances are related to transaction instances
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public TaxiParticipantAT(String txID)
    {
        // Binds to the singleton TaxiView and TaxiManager
        taxiManager = TaxiManager.getSingletonInstance();
        taxiView = TaxiView.getSingletonInstance();
        // we need to save the txID for later use when calling
        // business logic methods in the taxiManger.
        this.txID = txID;
    }

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

        taxiView.addPrepareMessage("id:" + txID + ". Prepare called on participant: " + this.getClass().toString());

        boolean success = taxiManager.prepareTaxi(txID);

        // Log the outcome and map the return value from
        // the business logic to the appropriate Vote type.

        if (success)
        {
            taxiView.addMessage("Taxi prepared successfully. Returning 'Prepared'\n");
            taxiView.updateFields();
            return new Prepared();
        }
        else
        {
            taxiManager.cancelTaxi(txID) ;
            taxiView.addMessage("Prepare failed (not enough Taxis?) Returning 'Aborted'\n");
            taxiView.updateFields();
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

        taxiView.addMessage("id:" + txID + ". Commit called on participant: " + this.getClass().toString());

        boolean success = taxiManager.commitTaxi(txID);

        // Log the outcome

        if (success)
        {
            taxiView.addMessage("Taxi committed\n");
        }
        else
        {
            taxiView.addMessage("Something went wrong (Transaction not registered?)\n");
        }

        taxiView.updateFields();
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

        taxiView.addMessage("id:" + txID + ". Rollback called on participant: " + this.getClass().toString());

        boolean success = taxiManager.cancelTaxi(txID);

        // Log the outcome

        if (success)
        {
            taxiView.addMessage("Taxi booking cancelled\n");
        }
        else
        {
            taxiView.addMessage("Something went wrong (Transaction not registered?)\n");
        }

        taxiView.updateFields();
    }

    /**
     * Shortcut method which combines the prepare
     * and commit steps in a single operation.
     *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void commitOnePhase() throws WrongStateException, SystemException
    {
        prepare();
        commit();
    }

    public void unknown() throws SystemException
    {
        // used for calbacks during crash recovery. This impl is not recoverable
    }

    public void error() throws SystemException
    {
        // used for calbacks during crash recovery. This impl is not recoverable
    }

    /**
     * Id for the transaction which this participant instance relates to.
     * Set by the service (via contrtuctor) at enrolment time, this value
     * is passed to the backend business logic methods.
     */
    protected String txID;

    /**
     * The TaxiView object to log events through.
     */
    protected static TaxiView taxiView;

    /**
     * The TaxiManager to perform business logic operations on.
     */
    protected static TaxiManager taxiManager;
}
