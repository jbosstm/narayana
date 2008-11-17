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
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithCoordinatorCompletionStub.java,v 1.1.2.2 2004/06/18 15:06:09 nmcl Exp $
 */

package com.arjuna.wst11.stub;

import java.io.StringWriter;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.soap.SoapUtils;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.webservices11.util.StreamHelper;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.PersistableParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine;
import com.arjuna.wst11.messaging.CoordinatorCompletionCoordinatorProcessorImpl;

public class BusinessAgreementWithCoordinatorCompletionStub implements BusinessAgreementWithCoordinatorCompletionParticipant, PersistableParticipant
{
    private static final QName QNAME_BACCWS_PARTICIPANT = new QName("baccwsParticipant") ;
    private CoordinatorCompletionCoordinatorEngine participant ;

    public BusinessAgreementWithCoordinatorCompletionStub(final CoordinatorCompletionCoordinatorEngine participant)
        throws Exception
    {
        this.participant = participant ;
    }

    /**
     * donstructor for use during recovery
     */
    public BusinessAgreementWithCoordinatorCompletionStub()
    {
        this.participant = null ;
    }

    public synchronized void close ()
        throws WrongStateException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Canceling-Active -> illegal state
         * Canceling-Completing -> illegal state
         * Completing -> illegal state
         * Completed -> illegal state
         * Closing -> no response
         * Compensating -> illegal state
         * Faulting -> illegal state
         * Faulting-Active -> illegal state
         * Faulting-Compensating -> illegal state
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = participant.close() ;

        if (state == State.STATE_CLOSING)
        {
            throw new SystemException() ;
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
    }

    public synchronized void cancel ()
        throws FaultedException, WrongStateException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> no response
         * Canceling-Active -> no response
         * Canceling-Completing -> no response
         * Completing -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> illegal state
         * Faulting -> illegal state
         * Faulting-Active -> illegal state
         * Faulting-Compensating -> illegal state
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = participant.cancel() ;

        if ((state == State.STATE_CANCELING) || (state == State.STATE_CANCELING_ACTIVE) ||
            (state == State.STATE_CANCELING_COMPLETING))
        {
            throw new SystemException() ;
        }
        else if (state == State.STATE_FAILING_CANCELING)
        {
            throw new FaultedException();
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
    }

    public synchronized void compensate ()
        throws FaultedException, WrongStateException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Canceling-Active -> illegal state
         * Canceling-Completing -> illegal state
         * Completing -> illegal state
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> no response
         * Faulting -> illegal state
         * Faulting-Active -> illegal state
         * Faulting-Compensating -> fault
         * Exiting -> illegal state
         * Ended -> ended
         */
        final State state = participant.compensate() ;
        if (state == State.STATE_COMPENSATING)
        {
            throw new SystemException() ;
        }
        else if (state == State.STATE_FAILING_COMPENSATING)
        {
            throw new FaultedException() ;
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
    }

    public synchronized void complete ()
        throws WrongStateException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
         * Canceling-Active -> illegal state
         * Canceling-Completing -> canceling
         * Completing -> no response
         * Completed -> completed
         * Closing -> illegal state
         * Compensating -> illegal state
         * Faulting -> illegal state
         * Faulting-Active -> illegal state
         * Faulting-Compensating -> fault
         * Exiting -> exiting
         * Ended -> illegal state
         */
        final State state = participant.complete() ;
        if (state == State.STATE_COMPLETED)
        {
            return ;
        }
        else if ((state == State.STATE_FAILING_COMPENSATING) || (state == State.STATE_CANCELING_COMPLETING) ||
            (state == State.STATE_EXITING))
        {
            throw new SystemException() ;
        }
        throw new WrongStateException() ;
    }

    public String status ()
        throws SystemException
    {
        final State state = participant.getStatus() ;
        return (state == null ? null : state.getValue().getLocalPart()) ;
    }

    public void unknown ()
        throws SystemException
    {
        error() ;
    }

    public synchronized void error ()
        throws SystemException
    {
        participant.cancel() ;
    }

    /**
     * @message com.arjuna.wst11.stub.BusinessAgreementWithCoordinatorCompletionStub_2 [com.arjuna.wst11.stub.BusinessAgreementWithCoordinatorCompletionStub_2] - Error persisting participant state
     */
    public boolean saveState(final OutputObjectState oos)
    {
        try
        {
            oos.packString(participant.getId()) ;

            // n.b. just use toString() for the endpoint -- it uses the writeTo() method which calls a suitable marshaller
            final StringWriter sw = new StringWriter() ;
            final XMLStreamWriter writer = SoapUtils.getXMLStreamWriter(sw) ;
            StreamHelper.writeStartElement(writer, QNAME_BACCWS_PARTICIPANT) ;
            String eprefText = participant.getParticipant().toString();
            writer.writeCData(eprefText);
            StreamHelper.writeEndElement(writer, null, null) ;
            writer.close() ;

            oos.packString(sw.toString()) ;

            final State state = participant.getStatus();
            final QName stateName = state.getValue();
            final String ns = stateName.getNamespaceURI();
            final String localPart = stateName.getLocalPart();
            final String prefix = stateName.getPrefix();
            oos.packString(ns != null ? ns : "");
            oos.packString(localPart != null ? localPart : "");
            oos.packString(prefix != null ? prefix : "");

            return true ;
        }
        catch (final Throwable th)
        {
            WSTLogger.arjLoggerI18N.error("com.arjuna.wst11.stub.BusinessAgreementWithCoordinatorCompletionStub_2", th) ;
            return false ;
        }
    }

    /**
     * @message com.arjuna.wst11.stub.BusinessAgreementWithCoordinatorCompletionStub_3 [com.arjuna.wst11.stub.BusinessAgreementWithCoordinatorCompletionStub_3] - Error restoring participant state
     */
    public boolean restoreState(final InputObjectState ios)
    {
        try
        {
            final String id = ios.unpackString();
            final String eprValue = ios.unpackString();

            final XMLStreamReader reader = SoapUtils.getXMLStreamReader(new StringReader(eprValue)) ;
            StreamHelper.checkNextStartTag(reader, QNAME_BACCWS_PARTICIPANT) ;
            String eprefText = reader.getElementText();
            StreamSource source = new StreamSource(new StringReader(eprefText));
            final W3CEndpointReference endpointReference = new W3CEndpointReference(source);

            String ns = ios.unpackString();
            final String localPart = ios.unpackString();
            String prefix = ios.unpackString();
            if ("".equals(ns)) {
                ns = null;
            }
            if ("".equals(prefix)) {
                prefix = null;
            }

            QName statename = new QName(ns, localPart, prefix);
            State state = State.toState11(statename);

            // if we already have an engine from a previous recovery scan or because
            // we had a heuristic outcome then reuse it with luck it will have been committed
            // or aborted between the last scan and this one
            // note that whatever happens it will not have been removed from the table
            // because it is marked as recovered
            participant = (CoordinatorCompletionCoordinatorEngine) CoordinatorCompletionCoordinatorProcessor.getProcessor().getCoordinator(id);
            if (participant == null) {
                participant = new CoordinatorCompletionCoordinatorEngine(id, endpointReference, state, true);
            }
            return true ;
        }
        catch (final Throwable th)
        {
            WSTLogger.arjLoggerI18N.error("com.arjuna.wst11.stub.BusinessAgreementWithCoordinatorCompletionStub_3", th) ;
            return false ;
        }
    }
}
