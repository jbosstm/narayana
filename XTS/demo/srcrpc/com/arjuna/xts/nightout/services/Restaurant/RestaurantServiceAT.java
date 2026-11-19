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
 * RestaurantServiceAT.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd
 *
 * $Id: RestaurantServiceAT.java,v 1.3 2004/12/01 16:26:44 kconner Exp $
 *
 */

package com.arjuna.xts.nightout.services.Restaurant;

import com.arjuna.mw.wst.UserTransactionFactory;
import com.arjuna.mw.wst.TransactionManagerFactory;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * An adapter class that exposes the RestaurantManager business API as a
 * transactional Web Service. Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class RestaurantServiceAT implements IRestaurantService
{
    /**
     * Book a number of seats in the restaurant
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     *
     * @param how_many The number of seats to book
     */
    public void bookSeats(int how_many)
    {
        RestaurantView restaurantView = RestaurantView.getSingletonInstance();
        RestaurantManager restaurantManager = RestaurantManager.getSingletonInstance();

        String transactionId = null;
        try
        {
            // get the transaction context of this thread:
            transactionId = UserTransactionFactory.userTransaction().toString();
            System.out.println("RestaurantServiceAT transaction id =" + transactionId);

            if (!restaurantManager.knowsAbout(transactionId))
            {
                System.out.println("RestaurantServiceAT - enrolling...");
                // enlist the Participant for this service:
                RestaurantParticipantAT restaurantParticipant = new RestaurantParticipantAT(transactionId);
                TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(restaurantParticipant, "org.jboss.jbossts.xts-demorpc:restaurantAT:" + new Uid().toString());
            }
        }
        catch (Exception e)
        {
            System.err.println("bookSeats: Participant enrolment failed");
            e.printStackTrace(System.err);
            return;
        }

        restaurantView.addMessage("******************************");

        restaurantView.addMessage("id:" + transactionId + ". Received a booking request for one table of " + how_many + " people");

        restaurantManager.bookSeats(transactionId, how_many);

        restaurantView.addMessage("Request complete\n");
        restaurantView.updateFields();
    }
}
