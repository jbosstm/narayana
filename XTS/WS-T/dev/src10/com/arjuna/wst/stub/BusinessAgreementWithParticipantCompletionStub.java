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
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithParticipantCompletionStub.java,v 1.1.2.2 2004/06/18 15:06:09 nmcl Exp $
 */

package com.arjuna.wst.stub;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.soap.SoapUtils;
import com.arjuna.webservices.util.StreamHelper;
import com.arjuna.webservices.wsba.State;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.PersistableParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine;

public class BusinessAgreementWithParticipantCompletionStub implements BusinessAgreementWithParticipantCompletionParticipant, PersistableParticipant
{
    private static final QName QNAME_BAPC_PARTICIPANT = new QName("bapcParticipant") ;
    
    private ParticipantCompletionCoordinatorEngine participant ;
    
    public BusinessAgreementWithParticipantCompletionStub (final ParticipantCompletionCoordinatorEngine participant)
        throws Exception
    {
        this.participant = participant ;
    }

    public synchronized void close ()
        throws WrongStateException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> illegal state
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
        throws WrongStateException, SystemException
    {
        /*
         * Active -> illegal state
         * Canceling -> no response
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
        
        if (state == State.STATE_CANCELING)
        {
            throw new SystemException() ;
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
         * Completed -> illegal state
         * Closing -> illegal state
         * Compensating -> no answer
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
        else if (state == State.STATE_FAULTING_COMPENSATING)
        {
            throw new FaultedException() ;
        }
        else if (state != State.STATE_ENDED)
        {
            throw new WrongStateException() ;
        }
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
     * @message com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub_2 [com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub_2] - Error persisting participant state
     */
    public boolean saveState(final OutputObjectState oos)
    {
        try
        {
            oos.packString(participant.getId()) ;
            
            final StringWriter sw = new StringWriter() ;
            final XMLStreamWriter writer = SoapUtils.getXMLStreamWriter(sw) ;
            StreamHelper.writeStartElement(writer, QNAME_BAPC_PARTICIPANT) ;
            participant.getParticipant().writeContent(writer) ;
            StreamHelper.writeEndElement(writer, null, null) ;
            writer.close() ;
            
            oos.packString(writer.toString()) ;
            
            return true ;
        }
        catch (final Throwable th)
        {
            WSTLogger.arjLoggerI18N.error("com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub_2", th) ;
            return false ;
        }
    }
    
    /**
     * @message com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub_3 [com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub_3] - Error restoring participant state
     */
    public boolean restoreState(final InputObjectState ios)
    {
        // KEV - rework
        return false ;
//        try
//        {
//            final String id = ios.unpackString() ;
//            final String eprValue = ios.unpackString() ;
//            
//            final XMLStreamReader reader = SoapUtils.getXMLStreamReader(new StringReader(eprValue)) ;
//            StreamHelper.checkNextStartTag(reader, QNAME_BAPC_PARTICIPANT) ;
//            final EndpointReferenceType endpointReferenceType = new EndpointReferenceType(reader) ;
//            
//            _id = id ;
//            _businessAgreementWithParticipantCompletionParticipant = endpointReferenceType ;
//            return true ;
//        }
//        catch (final Throwable th)
//        {
//            WSTLogger.arjLoggerI18N.error("com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub_3", th) ;
//            return false ;
//        }
    }
}
