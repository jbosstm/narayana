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
 * RestaurantServiceBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd
 *
 * $Id: RestaurantServiceBA.java,v 1.5 2004/12/01 16:26:44 kconner Exp $
 *
 */

package com.arjuna.xts.nightout.services.Restaurant;

import com.arjuna.wst.*;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.BusinessActivityManagerFactory;

/**
 * An adapter class that exposes the RestaurantManager business API as a
 * transactional Web Service. Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.5 $
 */
public class RestaurantServiceBA implements IRestaurantServiceBA
{
    /**
     * Book a number of seats in the restaurant
     * Enrols a Participant if necessary and passes
     * the call through to the business logic.
     *
     * @param how_many The number of seats to book.
     * @return true on success, false otherwise.
     */
    public boolean bookSeats(int how_many)
    {
        RestaurantView restaurantView = RestaurantView.getSingletonInstance();
        RestaurantManager restaurantManager = RestaurantManager.getSingletonInstance();

        BusinessActivityManager activityManager = BusinessActivityManagerFactory.businessActivityManager();

        // get the transaction context of this thread:
        String transactionId = null;
        try
        {
            transactionId = activityManager.currentTransaction().toString();
        }
        catch (SystemException e)
        {
            System.err.println("bookSeats: unable to obtain a transaction context!");
            e.printStackTrace(System.err);
            return false;
        }

        // log the event:
        System.out.println("RestaurantServiceBA transaction id =" + transactionId);

        restaurantView.addMessage("******************************");

        restaurantView.addPrepareMessage("id:" + transactionId + ". Received a booking request for one table of " + how_many + " people");
        restaurantView.updateFields();

        // invoke the backend business logic:
        restaurantManager.bookSeats(transactionId, how_many);

        // attempt to finalise the booking
        // (it will be compensated later if necessary)
        if (restaurantManager.prepareSeats(transactionId))
        {
            restaurantView.addMessage("id:" + transactionId + ". Seats prepared, trying to commit and enlist compensation Participant");
            restaurantView.updateFields();

            // it worked, so now we need a participant enlisted in case of compensation:

            RestaurantParticipantBA restaurantParticipant = new RestaurantParticipantBA(transactionId, how_many);
            // enlist the Participant for this service:
            BAParticipantManager participantManager = null;
            try
            {
                participantManager = activityManager.enlistForBusinessAgreementWithParticipantCompletion(restaurantParticipant, "com.arjuna.xts-demorpc:restaurantBA:" + new Uid().toString());
            }
            catch (Exception e)
            {
                restaurantView.addMessage("id:" + transactionId + ". Participant enrolement failed");
                restaurantManager.cancelSeats(transactionId);
                System.err.println("bookSeats: Participant enlistment failed");
                e.printStackTrace(System.err);
                return false;
            }

            // finish the booking in the backend ensuring it is compensatable:
            restaurantManager.commitSeats(transactionId, true);

            try
            {
                // tell the manager we have finished our work:
                participantManager.completed();
            }
            catch (Exception e)
            {
                System.err.println("bookSeats: 'completed' callback failed");
                restaurantManager.cancelSeats(transactionId);
                e.printStackTrace(System.err);
                return false;
            }
        }
        else
        {
            restaurantView.addMessage("id:" + transactionId + ". Failed to reserve seats. Cancelling.");
            restaurantManager.cancelSeats(transactionId);
            restaurantView.updateFields();
            return false;
        }

        restaurantView.addMessage("Request complete\n");
        restaurantView.updateFields();

        return true;
    }
}
