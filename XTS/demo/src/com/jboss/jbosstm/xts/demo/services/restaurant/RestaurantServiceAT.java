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

package com.jboss.jbosstm.xts.demo.services.restaurant;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.jboss.jbosstm.xts.demo.restaurant.IRestaurantServiceAT;
import com.jboss.jbosstm.xts.demo.services.recovery.DemoATRecoveryModule;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;

/**
 * An adapter class that exposes the RestaurantManager business API as a
 * transactional Web Service. Also logs events to a RestaurantView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
@WebService(serviceName="RestaurantServiceATService", portName="RestaurantServiceAT",
        name = "IRestaurantServiceAT", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Restaurant",
        wsdlLocation = "/WEB-INF/wsdl/RestaurantServiceAT.wsdl")
@HandlerChain(file = "../context-handlers.xml", name = "Context Handlers")                  
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class RestaurantServiceAT implements IRestaurantServiceAT
{
    /**
     * ensure that the recovery module for the dmeo is installed
     */
    @PostConstruct
    void postConstruct()
    {
        // ensure that the xts-demo AT recovery helper module is registered
        DemoATRecoveryModule.register();
    }

    /**
     * ensure that the recovery module for the dmeo is deinstalled
     */
    @PreDestroy
    void preDestroy()
    {
        // ensure that the xts-demo AT recovery helper module is registered
        DemoATRecoveryModule.unregister();
    }

    /**
     * Book a number of seats in the restaurant
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     *
     * @param how_many The number of seats to book
     */
    @WebMethod
    public void bookSeats(
            @WebParam(name = "how_many", partName = "how_many")
            int how_many)
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
                TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(restaurantParticipant, "org.jboss.jbossts.xts-demo:restaurantAT:" + new Uid().toString());
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
