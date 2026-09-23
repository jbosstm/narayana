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
 * TheatreServiceAT.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd.
 *
 * $Id: TheatreServiceAT.java,v 1.3 2004/12/01 16:27:21 kconner Exp $
 *
 */

package com.arjuna.xts.nightout.services.Theatre;

import com.arjuna.mw.wst.*;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * An adapter class that exposes the TheatreManager business API as a
 * transactional Web Service. Also logs events to a TheatreView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TheatreServiceAT implements ITheatreService
{
    /**
     * Book a number of seats in the Theatre
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     *
     * @param how_many   The number of seats to book
     * @param which_area The area of the theatre to book seats in
     */
    public void bookSeats(int how_many, int which_area)
    {
        TheatreView theatreView = TheatreView.getSingletonInstance();
        TheatreManager theatreManager = TheatreManager.getSingletonInstance();

        String transactionId = null;
        try
        {
            // get the transaction context of this thread:
            transactionId = UserTransactionFactory.userTransaction().toString();
            System.out.println("TheatreServiceAT transaction id =" + transactionId);

            if (!theatreManager.knowsAbout(transactionId))
            {
                System.out.println("theatreService - enrolling...");
                // enlist the Participant for this service:
                TheatreParticipantAT theatreParticipant = new TheatreParticipantAT(transactionId);
                TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(theatreParticipant, "org.jboss.jbossts.xts-demorpc:theatreAT:" + new Uid().toString());
            }
        }
        catch (Exception e)
        {
            System.err.println("bookSeats: Participant enrolment failed");
            e.printStackTrace(System.err);
            return;
        }

        theatreView.addMessage("******************************");

        theatreView.addMessage("id:" + transactionId.toString() + ". Received a theatre booking request for " + how_many + " seats in area " + which_area);

        TheatreManager.getSingletonInstance().bookSeats(transactionId, how_many, which_area);

        theatreView.addMessage("Request complete\n");
        theatreView.updateFields();
    }
}
