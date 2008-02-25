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
 * TheatreParticipantBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd.
 *
 * $Id: TheatreParticipantBA.java,v 1.3 2004/09/09 15:18:10 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.theatre;

import com.arjuna.wst.*;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * An adapter class that exposes the TheatreManager transaction lifecycle
 * API as a WS-T Business Activity participant.
 * Also logs events to a TheatreView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TheatreParticipantBA implements BusinessAgreementWithParticipantCompletionParticipant
{
    /**
     * Participant instances are related to business method calls
     * in a one to one manner.
     *
     * @param txID       uniq id String for the transaction instance.
     * @param how_many   seats to book/compensate.
     * @param which_area of the theatre the seats are in.
     */
    public TheatreParticipantBA(String txID, int how_many, int which_area)
    {
        // Binds to the singleton TheatreView and TheatreManager
        theatreManager = TheatreManager.getSingletonInstance();
        theatreView = TheatreView.getSingletonInstance();
        // we need to save the txID for later use when logging
        this.txID = txID;
        // we also need the business paramater(s) in case of compensation
        this.seatCount = how_many;
        this.seatingArea = which_area;
    }

    /**
     * The transaction has completed successfully. The participant previously
     * informed the coordinator that it was ready to complete.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException never in this implementation.
     */

    public void close() throws WrongStateException, SystemException
    {
        // for logging only. This impl does not do anything else here.

        System.out.println("TheatreParticipantBA.close");

        theatreView.addMessage("id:" + txID + ". Close called on participant: " + this.getClass());
        theatreView.updateFields();
    }

    /**
     * The transaction has cancelled, and the participant should undo any work.
     * The participant cannot have informed the coordinator that it has
     * completed.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException never in this implementation.
     */

    public void cancel() throws WrongStateException, SystemException
    {
        // we will always have called completed or error, so this can be a null op.

        System.out.println("TheatreParticipantBA.cancel");

        theatreView.addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());
        theatreView.updateFields();
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException if unable to perform the compensating transaction.
     */

    public void compensate() throws WrongStateException, SystemException
    {
        System.out.println("TheatreParticipantBA.compensate");

        // Log the event and perform a compensating transaction
        // on the backend business logic if needed.

        theatreView.addPrepareMessage("id:" + txID + ". Compensate called on participant: " + this.getClass().toString());
        theatreView.updateFields();

        if (seatCount > 0)
        {
            String compensatingTxID = new Uid().toString();
            // use a negative number of seats to 'reverse' the previous booking
            // This technique (hack) prevents us needing new business logic to support compensation.
            theatreManager.bookSeats(compensatingTxID, seatCount * -1, seatingArea);
            theatreView.updateFields();

            boolean success = false;
            if(theatreManager.prepareSeats(compensatingTxID))
            {
                if (theatreManager.commitSeats(compensatingTxID))
                {
                    theatreView.addMessage("id:" + txID + " Compensating transaction completed sucessfully.");
                    theatreView.updateFields();
                    success = true;
                }
            }
            else
            {
                theatreManager.cancelSeats(compensatingTxID);
            }

            if(!success)
            {
                theatreView.addMessage("id:" + txID + " Compensation failed. Throwing SystemException\n");
                theatreView.updateFields();
                throw new SystemException("Compensating transaction failed.");
            }
        }
    }
    
    public String status()
    {
        return Status.STATUS_ACTIVE ;
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
     * is used in informational log messages.
     */
    protected String txID;

    /**
     * Copy of business state information, may be needed during compensation.
     */
    protected int seatCount;

    /**
     * Copy of business state information, may be needed during compensation.
     */
    protected int seatingArea;

    /**
     * The TheatreView object to log events through.
     */
    protected static TheatreView theatreView;

    /**
     * The TheatreManager to perform business logic operations on.
     */
    protected static TheatreManager theatreManager;
}
