/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * TheatreParticipantAT.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd.
 *
 * $Id: TheatreParticipantAT.java,v 1.3 2005/02/23 09:58:02 kconner Exp $
 *
 */

package com.arjuna.xts.nightout.services.Theatre;

import com.arjuna.wst.*;

/**
 * An adapter class that exposes the TheatreManager transaction lifecycle
 * API as a WS-T Atomic Transaction participant.
 * Also logs events to a TheatreView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TheatreParticipantAT implements Durable2PCParticipant
{
    /**
     * Participant instances are related to transaction instances
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public TheatreParticipantAT(String txID)
    {
        // Binds to the singleton TheatreView and TheatreManager
        theatreManager = TheatreManager.getSingletonInstance();
        theatreView = TheatreView.getSingletonInstance();
        // we need to save the txID for later use when calling
        // business logic methods in the theatreManger.
        this.txID = txID;
    }

    /**
     * Invokes the prepare step of the business logic,
     * reporting activity and outcome.
     *
     * @return trus on success, false otherwise.
     * @throws WrongStateException
     * @throws SystemException
     */
    public Vote prepare() throws WrongStateException, SystemException
    {
        // Log the event and invoke the prepare operation
        // on the backend business logic.

        System.out.println("TheatreParticipantAT.prepare");

        theatreView.addPrepareMessage("id:" + txID + ". Prepare called on participant: " + this.getClass().toString());

        boolean success = theatreManager.prepareSeats(txID);

        // Log the outcome and map the return value from
        // the business logic to the appropriate Vote type.


        if (success)
        {
            theatreView.addMessage("Theatre prepared successfully. Returning 'Prepared'\n");
            theatreView.updateFields();
            return new Prepared();
        }
        else
        {
            theatreManager.cancelSeats(txID) ;
            theatreView.addMessage("Prepare failed (not enough seats?) Returning 'Aborted'\n");
            theatreView.updateFields();
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

        System.out.println("TheatreParticipantAT.commit");

        theatreView.addMessage("id:" + txID + ". Commit called on participant: " + this.getClass().toString());

        boolean success = theatreManager.commitSeats(txID);

        // Log the outcome

        if (success)
        {
            theatreView.addMessage("Theatre tickets committed\n");
        }
        else
        {
            theatreView.addMessage("Something went wrong (Transaction not registered?)\n");
        }

        theatreView.updateFields();
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

        System.out.println("TheatreParticipantAT.rollback");

        theatreView.addMessage("id:" + txID + ". Rollback called on participant: " + this.getClass().toString());

        boolean success = theatreManager.cancelSeats(txID);

        // Log the outcome

        if (success)
        {
            theatreView.addMessage("Theatre booking cancelled\n");
        }
        else
        {
            theatreView.addMessage("Something went wrong (Transaction not registered?)\n");
        }

        theatreView.updateFields();
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
     * The TheatreView object to log events through.
     */
    protected static TheatreView theatreView;

    /**
     * The TheatreManager to perform business logic operations on.
     */
    protected static TheatreManager theatreManager;
}
