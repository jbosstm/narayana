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
 * TaxiServiceBA.java
 *
 * Copyright (c) 2004 Arjuna Technologies Ltd.
 *
 * $Id: TaxiServiceBA.java,v 1.5 2004/12/01 16:27:01 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.taxi;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst.SystemException;
import com.jboss.jbosstm.xts.demo.taxi.ITaxiServiceBA;

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * An adapter class that exposes the TaxiManager business API as a
 * transactional Web Service. Also logs events to a TaxiView object.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.5 $
 */
@WebService(serviceName="TaxiServiceBAService", portName="TaxiServiceBA",
        name = "ITaxiServiceBA", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Taxi",
        wsdlLocation = "/WEB-INF/wsdl/TaxiServiceBA.wsdl")
@HandlerChain(file = "/context-handlers.xml", name = "Context Handlers")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class TaxiServiceBA implements ITaxiServiceBA
{
    /**
     * Book a taxi
     * Enrols a Participant if necessary and passes
     * the call through to the business logic.
     *
     * @return true on success, false otherwise.
     */
    @WebMethod
    @WebResult(name = "bookTaxiBAResponse", partName = "bookTaxiBAResponse")
    public boolean bookTaxi()
    {
        TaxiView taxiView = TaxiView.getSingletonInstance();
        TaxiManager taxiManager = TaxiManager.getSingletonInstance();

        BusinessActivityManager activityManager = BusinessActivityManagerFactory.businessActivityManager();

        // get the transaction context of this thread:
        String transactionId = null;
        try
        {
            transactionId = activityManager.currentTransaction().toString();
        }
        catch (SystemException e)
        {
            System.err.println("bookTaxi: unable to obtain a transaction context!");
            e.printStackTrace(System.err);
            return false;
        }

        // log the event:
        System.out.println("TaxiServiceBA transaction id =" + transactionId);

        taxiView.addMessage("******************************");

        taxiView.addMessage("id:" + transactionId.toString() + ". Received a taxi booking request");
        taxiView.updateFields();

        TaxiParticipantBA taxiParticipant = TaxiParticipantBA.getParticipant(transactionId);
        BAParticipantManager participantManager;
        if (taxiParticipant != null) {
            // hmm, this means we have already completed changes in this transaction and are awaiting a close
            //or compensate request. this service does not support repeated requests in the same activity so
            // we fail this request.

            taxiView.addMessage("id:" + transactionId + ". Participant already enrolled!");
            taxiView.updateFields();
            System.err.println("bookSeats: request failed");
            return false;
        }

        taxiParticipant = new TaxiParticipantBA(transactionId);

        // enlist the Participant for this service:
        try
        {
            participantManager = activityManager.enlistForBusinessAgreementWithCoordinatorCompletion(taxiParticipant, "org.jboss.jbossts.xts-demo:taxiBA:" + new Uid().toString());
        }
        catch (Exception e)
        {
            taxiView.addMessage("id:" + transactionId + ". Participant enrolement failed");
            taxiManager.rollback(transactionId);
            System.err.println("bookTaxi: Participant enrolment failed");
            e.printStackTrace(System.err);
            return false;
        }

        TaxiParticipantBA.recordParticipant(transactionId, taxiParticipant, participantManager);

        // invoke the backend business logic:
        taxiManager.bookTaxi(transactionId);

        taxiView.addMessage("Request complete\n");
        taxiView.updateFields();

        return true;
    }
}




