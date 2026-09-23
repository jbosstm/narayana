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

package com.arjuna.xts.nightout.services.Theatre;

import com.arjuna.mw.wst.*;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.wst.*;

/**
 * An adapter class that exposes the TheatreManager business API as a
 * transactional Web Service. Also logs events to a TheatreView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.5 $
 */
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
    public boolean bookSeats(int how_many, int which_area)
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

        theatreView.addPrepareMessage("id:" + transactionId + ". Received a theatre booking request for " + how_many + " seats in area " + which_area);
        theatreView.updateFields();

        theatreManager.bookSeats(transactionId, how_many, which_area);

        if (theatreManager.prepareSeats(transactionId))
        {
            theatreView.addMessage("id:" + transactionId + ". Seats prepared, trying to commit and enlist compensation Participant");
            theatreView.updateFields();

            TheatreParticipantBA theatreParticipant = new TheatreParticipantBA(transactionId, how_many, which_area);
            // enlist the Participant for this service:
            com.arjuna.wst.BAParticipantManager participantManager = null;
            try
            {
                participantManager = activityManager.enlistForBusinessAgreementWithParticipantCompletion(theatreParticipant, "com.arjuna.xts-demorpc:theatreBA:" + new Uid().toString());
            }
            catch (Exception e)
            {
                theatreView.addMessage("id:" + transactionId + ". Participant enrolement failed");
                theatreManager.cancelSeats(transactionId);
                System.err.println("bookSeats: Participant enrolement failed");
                e.printStackTrace(System.err);
                return false;
            }

            // finish the booking in the backend ensuring it is compensatable:
            theatreManager.commitSeats(transactionId, true);

            try
            {
                participantManager.completed();
            }
            catch (Exception e)
            {
                System.err.println("bookSeats: 'completed' callback failed");
                theatreManager.cancelSeats(transactionId);
                e.printStackTrace(System.err);
                return false;
            }
        }
        else
        {
            theatreView.addMessage("id:" + transactionId + ". Failed to reserve seats. Cancelling.");
            theatreManager.cancelSeats(transactionId);
            theatreView.updateFields();
            return false;
        }

        theatreView.addMessage("Request complete\n");
        theatreView.updateFields();

        return true;
    }
}
