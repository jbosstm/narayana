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
 * TheatreServiceBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd.
 *
 * $Id: TheatreServiceBA.java,v 1.5 2004/12/01 16:27:21 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.theatre;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst.SystemException;
import com.jboss.jbosstm.xts.demo.theatre.ITheatreServiceBA;
import static com.jboss.jbosstm.xts.demo.services.theatre.TheatreConstants.*;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;

/**
 * An adapter class that exposes the TheatreManager business API as a
 * transactional Web Service. Also logs events to a TheatreView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.5 $
 */
@WebService(serviceName="TheatreServiceBAService", portName="TheatreServiceBA",
        name = "ITheatreServiceBA", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Theatre",
        wsdlLocation = "/WEB-INF/wsdl/TheatreServiceBA.wsdl")
@SOAPBinding(style=SOAPBinding.Style.RPC)
@HandlerChain(file = "/context-handlers.xml", name = "Context Handlers")
public class TheatreServiceBA implements ITheatreServiceBA
{
    /**
     * Book a number of seats in the Theatre
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     *
     * @param how_many   The number of seats to book.
     * @param which_area The area of the theatre to book seats in.
     * @return true on success, false otherwise.
     */
    @WebMethod
    @WebResult(name = "bookSeatsBAResponse", partName = "bookSeatsBAResponse")
    public boolean bookSeats(
            @WebParam(name = "how_many", partName = "how_many")
            int how_many,
            @WebParam(name = "which_area", partName = "which_area")
            int which_area)

    {
        TheatreView theatreView = TheatreView.getSingletonInstance();
        TheatreManager theatreManager = TheatreManager.getSingletonInstance();

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

        System.out.println("TheatreServiceBA transaction id =" + transactionId);

        theatreView.addMessage("******************************");

        theatreView.addMessage("id:" + transactionId + ". Received a theatre booking request for " + how_many + " seats in area " + which_area);
        theatreView.updateFields();

        TheatreParticipantBA theatreParticipant = TheatreParticipantBA.getParticipant(transactionId);
        BAParticipantManager participantManager;

        if (theatreParticipant == null) {
            int[] bookings = new int[NUM_SEAT_AREAS];
            bookings[which_area] = how_many;
            theatreParticipant = new TheatreParticipantBA(transactionId, bookings);
            // enlist the Participant for this service:
            try
            {
                participantManager = activityManager.enlistForBusinessAgreementWithCoordinatorCompletion(theatreParticipant, "org.jboss.jbossts.xts-demo:theatreBA:" + new Uid().toString());
                TheatreParticipantBA.recordParticipant(transactionId, theatreParticipant, participantManager);
            }
            catch (Exception e)
            {
                theatreView.addMessage("id:" + transactionId + ". Participant enrolement failed");
                System.err.println("bookSeats: Participant enrolement failed");
                e.printStackTrace(System.err);
                return false;
            }
        } else if (theatreParticipant.bookings[which_area] == 0) {
            theatreParticipant.bookings[which_area] = how_many;
        } else {
            // hmm, this means we have already completed changes in this transaction and are awaiting a close
            //or compensate request. this service does not support repeated requests in the same activity so
            // we ensure the activity cannot continue by calling cannotComplete and also roll back
            // any local changes
            participantManager = TheatreParticipantBA.getManager(transactionId);
            try {
                participantManager.cannotComplete();
            } catch (Exception e) {
                System.err.println("bookSeats: 'cannotComplete' callback failed");
                e.printStackTrace(System.err);
            }
            theatreManager.rollback(transactionId);
            TheatreParticipantBA.removeParticipant(transactionId);
            theatreView.addMessage("id:" + transactionId + ". repeat booking for area " + which_area);
            theatreView.updateFields();
            System.err.println("bookSeats: request failed");
            return false;
        }

        // invoke the backend business logic:
        theatreManager.bookSeats(transactionId, how_many, which_area);

        // this service employs the coordinator completion protocol which means we don't actually prepare and
        // commit these changes until the coordinator sends a complete request through.
        theatreView.addMessage("Request complete\n");
        theatreView.updateFields();

        return true;
    }
}
