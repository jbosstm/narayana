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

package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst.SystemException;
import com.jboss.jbosstm.xts.demo.restaurant.IRestaurantServiceBA;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;

/**
 * An adapter class that exposes the RestaurantManager business API as a
 * transactional Web Service. Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.5 $
 */
@WebService(serviceName="RestaurantServiceBAService", portName="RestaurantServiceBA",
        name = "IRestaurantServiceBA", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Restaurant",
        wsdlLocation = "/WEB-INF/wsdl/RestaurantServiceBA.wsdl")
@HandlerChain(file = "/context-handlers.xml", name = "Context Handlers")
@SOAPBinding(style=SOAPBinding.Style.RPC)
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
    @WebMethod
    @WebResult(name = "bookSeatsBAResponse", partName = "bookSeatsBAResponse")
    public boolean bookSeats(
            @WebParam(name = "how_many", partName = "how_many")
            int how_many)
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

        restaurantView.addMessage("id:" + transactionId + ". Received a booking request for one table of " + how_many + " people");
        restaurantView.updateFields();

        RestaurantParticipantBA restaurantParticipant = RestaurantParticipantBA.getParticipant(transactionId);

        if (restaurantParticipant != null) {
            // hmm, this means we have already completed changes in this transaction and are awaiting a close
            //or compensate request. this service does not support repeated requests in the same activity so
            // we fail this request.

            restaurantView.addMessage("id:" + transactionId + ". Participant already enrolled!");
            restaurantView.updateFields();
            System.err.println("bookSeats: request failed");
            return false;
        }

        BAParticipantManager participantManager;

        // enlist the Participant for this service:
        try
        {
            restaurantParticipant = new RestaurantParticipantBA(transactionId, how_many);
            participantManager = activityManager.enlistForBusinessAgreementWithParticipantCompletion(restaurantParticipant, "org.jboss.jbossts.xts-demo:restaurantBA:" + new Uid().toString());
            RestaurantParticipantBA.recordParticipant(transactionId, restaurantParticipant);
        }
        catch (Exception e)
        {
            restaurantView.addMessage("id:" + transactionId + ". Participant enrolement failed");
            System.err.println("bookSeats: Participant enlistment failed");
            e.printStackTrace(System.err);
            return false;
        }

        restaurantView.addPrepareMessage("id:" + transactionId + ". Attempting to prepare seats");
        restaurantView.updateFields();
        // invoke the backend business logic:
        restaurantManager.bookSeats(transactionId, how_many);

        // this service employs the participant completion protocol which means it decides when it wants to
        // commit local changes. so we prepare and commit those changes now. if any other participant fails
        // or the client decides to cancel we can rely upon being told to compensate.

        if (restaurantManager.prepare(transactionId))
        {
            restaurantView.addMessage("id:" + transactionId + ". Seats prepared, trying to commit");
            restaurantView.updateFields();

            try
            {
                // tell the participant manager we have finished our work
                // this will call back to the participant once a compensation recovery record has been written
                // allowing it to commit or roll back the restaurant manager
                participantManager.completed();
            }
            catch (Exception e)
            {
                System.err.println("bookSeats: 'completed' callback failed");
                restaurantManager.rollback(transactionId);
                e.printStackTrace(System.err);
                RestaurantParticipantBA.removeParticipant(transactionId);
                return false;
            }
        }
        else
        {
            restaurantView.addMessage("id:" + transactionId + ". Failed to reserve seats.");
            restaurantView.updateFields();
            try
            {
                // tell the participant manager we cannot complete. this will force the activity to fail
                participantManager.cannotComplete();
            }
            catch (Exception e)
            {
                System.err.println("bookSeats: 'cannotComplete' callback failed");
                e.printStackTrace(System.err);
                return false;
            }
            RestaurantParticipantBA.removeParticipant(transactionId);
            return false;
        }

        restaurantView.addMessage("Request complete\n");
        restaurantView.updateFields();

        return true;
    }
}
