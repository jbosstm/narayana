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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: ParticipantStub.java,v 1.1.2.1 2005/11/22 10:35:28 kconner Exp $
 */

package com.arjuna.wst.stub;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.soap.SoapUtils;
import com.arjuna.webservices.util.StreamHelper;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsat.State;
import com.arjuna.wst.Aborted;
import com.arjuna.wst.Participant;
import com.arjuna.wst.PersistableParticipant;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.messaging.engines.CoordinatorEngine;
import com.arjuna.wst.messaging.CoordinatorProcessorImpl;

public class ParticipantStub implements Participant, PersistableParticipant
{
    private static final QName QNAME_TWO_PC_PARTICIPANT = new QName("twoPCParticipant") ;
    private CoordinatorEngine coordinator ;

    public ParticipantStub(final String id, final boolean durable, final EndpointReferenceType twoPCParticipant)
        throws Exception
    {
        // id will be supplied as null during recovery in which case we can delay creation
        // of the coordinator until restore_state is called

        if (id != null) {
            coordinator = new CoordinatorEngine(id, durable, twoPCParticipant) ;
        }
    }

    public Vote prepare()
        throws WrongStateException, SystemException
    {
        /*
         * null - aborted or read only
         * Active - illegal state
         * Preparing - no answer
         * Prepared - illegal state
         * PreparedSuccess - prepared
         * Committing - illegal state
         * Aborting - aborting
         */
        final State state = coordinator.prepare() ;
        if (state == State.STATE_PREPARED_SUCCESS)
        {
            return new Prepared() ;
        }
        else if (state == State.STATE_ABORTING)
        {
            return new Aborted() ;
        }
        else if (state == null)
        {
            if (coordinator.isReadOnly())
            {
                return new ReadOnly() ;
            }
            else
            {
                return new Aborted() ;
            }
        }
        else if (state == State.STATE_PREPARING)
        {
            // typically means no response from the remote end.
            // throw a comm exception to distinguish this case from the
            // one where the remote end itself threw a SystemException.

            throw new SystemCommunicationException() ;
        }
        else
        {
            throw new WrongStateException() ;
        }
    }

    public void commit()
        throws WrongStateException, SystemException
    {
        /*
         * null - committed
         * Active - illegal state
         * Preparing - illegal state
         * Prepared - illegal state
         * PreparedSuccess - illegal state
         * Committing - no response
         * Aborting - illegal state
         */
        final State state = coordinator.commit() ;
        if (state != null)
        {
            if (state == State.STATE_COMMITTING)
            {
                // typically means no response from the remote end.
                // throw a comm exception to distinguish this case from the
                // one where the remote end itself threw a SystemException.
                throw new SystemCommunicationException();
            }
            else
            {
                throw new WrongStateException() ;
            }
        }
    }

    public void rollback()
        throws WrongStateException, SystemException
    {
        /*
         * null - aborted
         * Active - illegal state
         * Preparing - illegal state
         * Prepared - illegal state
         * PreparedSuccess - illegal state
         * Committing - illegal state
         * Aborting - no response
         */
        final State state = coordinator.rollback() ;
        if (state != null)
        {
            if (state == State.STATE_ABORTING)
            {
                throw new SystemException() ;
            }
            else
            {
                throw new WrongStateException() ;
            }
        }
    }

    public void unknown()
        throws SystemException
    {
        error() ;
    }

    public void error()
        throws SystemException
    {
        try
        {
            rollback() ;
        }
        catch (final WrongStateException wse) {} // ignore
    }

    /**
     * @message com.arjuna.wst.stub.ParticipantStub_1 [com.arjuna.wst.stub.ParticipantStub_1] - Error persisting participant state
     */
    public boolean saveState(final OutputObjectState oos)
    {
        try
        {
            oos.packString(coordinator.getId()) ;
            oos.packBoolean(coordinator.isDurable()) ;

            final StringWriter sw = new StringWriter() ;
            final XMLStreamWriter writer = SoapUtils.getXMLStreamWriter(sw) ;
            StreamHelper.writeStartElement(writer, QNAME_TWO_PC_PARTICIPANT) ;
            coordinator.getParticipant().writeContent(writer) ;
            StreamHelper.writeEndElement(writer, null, null) ;
            writer.close() ;
            sw.close();

            String tmp = writer.toString();
            String swString = sw.toString();
            oos.packString(swString) ;

            return true ;
        }
        catch (final Throwable th)
        {
            WSTLogger.arjLoggerI18N.error("com.arjuna.wst.stub.ParticipantStub_1", th) ;
            return false ;
        }
    }

    /**
     * @message com.arjuna.wst.stub.ParticipantStub_2 [com.arjuna.wst.stub.ParticipantStub_2] - Error restoring participant state
     */
    public boolean restoreState(final InputObjectState ios)
    {
        try
        {
            final String id = ios.unpackString() ;
            final boolean durable = ios.unpackBoolean() ;
            final String eprValue = ios.unpackString() ;

            final XMLStreamReader reader = SoapUtils.getXMLStreamReader(new StringReader(eprValue)) ;
            StreamHelper.checkNextStartTag(reader, QNAME_TWO_PC_PARTICIPANT) ;
            final EndpointReferenceType endpointReferenceType = new EndpointReferenceType(reader) ;
            // if we already have a coordinator from a previous recovery scan then reuse it
            // with luck it will have been committed between the last scan and this one
            coordinator = (CoordinatorEngine)CoordinatorProcessorImpl.getProcessor().getCoordinator(id);
            if (coordinator == null) {
                // no entry found so recreate one which is at the prepared stage
                coordinator = new CoordinatorEngine(id, durable, endpointReferenceType, true, State.STATE_PREPARED_SUCCESS) ;
            }
            return true ;
        }
        catch (final Throwable th)
        {
            WSTLogger.arjLoggerI18N.error("com.arjuna.wst.stub.ParticipantStub_2", th) ;
            return false ;
        }
    }
}
