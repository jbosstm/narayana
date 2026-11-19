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
 * RestaurantParticipantAT.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd.
 *
 * $Id: RestaurantParticipantAT.java,v 1.3 2005/02/23 09:58:01 kconner Exp $
 *
 */

package com.arjuna.xts.nightout.services.Restaurant;

import com.arjuna.wst.*;

import java.io.Serializable;
import java.io.IOException;

/**
 * An adapter class that exposes the RestaurantManager transaction lifecycle
 * API as a WS-T Atomic Transaction participant.
 * Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantParticipantAT implements Durable2PCParticipant, Serializable
{
    /**
     * Participant instances are related to transaction instances
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public RestaurantParticipantAT(String txID)
    {
        // we need to save the txID for later use when calling
        // business logic methods in the restaurantManger.
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

        System.out.println("RestaurantParticipantAT.prepare");

        getRestaurantView().addPrepareMessage("id:" + txID + ". Prepare called on participant: " + this.getClass().toString());

        boolean success = getRestaurantManager().prepareSeats(txID);

        // Log the outcome and map the return value from
        // the business logic to the appropriate Vote type.

        if (success)
        {
            getRestaurantView().addMessage("Seats prepared successfully. Returning 'Prepared'\n");
            getRestaurantView().updateFields();
            return new Prepared();
        }
        else
        {
            getRestaurantManager().cancelSeats(txID) ;
            getRestaurantView().addMessage("Prepare failed (not enough seats?) Returning 'Aborted'\n");
            getRestaurantView().updateFields();
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

        System.out.println("RestaurantParticipantAT.commit");

        getRestaurantView().addMessage("id:" + txID + ". Commit called on participant: " + this.getClass().toString());

        boolean success = getRestaurantManager().commitSeats(txID);

        // Log the outcome

        if (success)
        {
            getRestaurantView().addMessage("Seats committed\n");
        }
        else
        {
            getRestaurantView().addMessage("Something went wrong (Transaction not registered?)\n");
        }

        getRestaurantView().updateFields();
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

        System.out.println("RestaurantParticipantAT.rollback");

        getRestaurantView().addMessage("id:" + txID + ". Rollback called on participant: " + this.getClass().toString());

        boolean success = getRestaurantManager().cancelSeats(txID);

        // Log the outcome

        if (success)
        {
            getRestaurantView().addMessage("Seats booking cancelled\n");
        }
        else
        {
            getRestaurantView().addMessage("Something went wrong (Transaction not registered?)\n");
        }

        getRestaurantView().updateFields();
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

    public RestaurantView getRestaurantView() {
        return RestaurantView.getSingletonInstance();
    }

    public RestaurantManager getRestaurantManager() {
        return RestaurantManager.getSingletonInstance();
    }
}

