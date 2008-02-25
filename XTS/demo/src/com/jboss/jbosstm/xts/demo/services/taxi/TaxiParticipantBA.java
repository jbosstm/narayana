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
 * TaxiParticipantBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd.
 *
 * $Id: TaxiParticipantBA.java,v 1.2 2004/09/09 15:18:11 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.taxi;

import com.arjuna.wst.*;

/**
 * An adapter class that exposes the TaxiManager transaction lifecycle
 * API as a WS-T Business Activity participant.
 * Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.2 $
 */
public class TaxiParticipantBA implements BusinessAgreementWithParticipantCompletionParticipant
{
    /**
     * Participant instances are related to business method calls
     * in a one to one manner.
     *
     * @param txID uniq id String for the transaction instance.
     */
    public TaxiParticipantBA(String txID)
    {
        // Binds to the singleton TaxiView and TaxiManager
        taxiManager = TaxiManager.getSingletonInstance();
        taxiView = TaxiView.getSingletonInstance();
        // we need to save the txID for later use when logging.
        this.txID = txID;
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

        System.out.println("TaxiParticipantBA.close");

        taxiView.addMessage("id:" + txID + ". Close called on participant: " + this.getClass());
        taxiView.updateFields();
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

        System.out.println("TaxiParticipantBA.cancel");

        taxiView.addMessage("id:" + txID + ". Cancel called on participant: " + this.getClass().toString());
        taxiView.updateFields();
    }

    /**
     * The transaction has cancelled. The participant previously
     * informed the coordinator that it had finished work but could compensate
     * later if required, so it is now requested to do so.
     *
     * @throws WrongStateException never in this implementation.
     * @throws SystemException always, because this implementation does not support compensation.
     */

    public void compensate() throws WrongStateException, SystemException
    {
        System.out.println("TaxiParticipantBA.compensate");

        // This impl does not support compensation, in order
        // to allow illustration of heuristic outcomes.
        // It just log the event and throws an exception.

        taxiView.addMessage("id:" + txID + ". Compensate called on participant: " + this.getClass().toString());

        taxiView.addMessage("Compensation not supported by ths implementation!");
        taxiView.updateFields();

        throw new SystemException("Compensation not supported!");
    }

    public String status () throws SystemException
    {
        return Status.STATUS_ACTIVE;
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
     * The TaxiView object to log events through.
     */
    protected static TaxiView taxiView;

    /**
     * The TaxiManager to perform business logic operations on.
     */
    protected static TaxiManager taxiManager;
}
