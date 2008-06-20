package com.arjuna.wst11.stub;

import com.arjuna.wst.*;
import com.arjuna.wst.stub.SystemCommunicationException;
import com.arjuna.wst11.messaging.engines.CoordinatorEngine;
import com.arjuna.wst11.messaging.CoordinatorProcessorImpl;
import com.arjuna.webservices11.wsat.State;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices11.util.StreamHelper;
import com.arjuna.webservices.soap.SoapUtils;
import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;
import java.io.StringReader;

public class ParticipantStub implements Participant, PersistableParticipant
{
    private static final QName QNAME_TWO_PC_PARTICIPANT = new QName("twoPCParticipant") ;
    private CoordinatorEngine coordinator ;

    public ParticipantStub(final String id, final boolean durable, final W3CEndpointReference twoPCParticipant)
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
            throw new SystemException() ;
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
     * @message com.arjuna.wst11.stub.ParticipantStub_1 [com.arjuna.wst11.stub.ParticipantStub_1] - Error persisting participant state
     */
    public boolean saveState(final OutputObjectState oos)
    {
        try
        {
            oos.packString(coordinator.getId()) ;
            oos.packBoolean(coordinator.isDurable()) ;

            // n.b. just use toString() for the endpoint -- it uses the writeTo() method which calls a suitable marshaller
            final StringWriter sw = new StringWriter() ;
            final XMLStreamWriter writer = SoapUtils.getXMLStreamWriter(sw) ;
            StreamHelper.writeStartElement(writer, QNAME_TWO_PC_PARTICIPANT) ;
            String eprefText = coordinator.getParticipant().toString();
            writer.writeCData(eprefText);
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
            WSTLogger.arjLoggerI18N.error("com.arjuna.wst11.stub.ParticipantStub_1", th) ;
            return false ;
        }
    }

    /**
     * @message com.arjuna.wst11.stub.ParticipantStub_2 [com.arjuna.wst11.stub.ParticipantStub_2] - Error restoring participant state
     */
    public boolean restoreState(final InputObjectState ios)
    {
        try
        {
            final String id = ios.unpackString() ;
            final boolean durable = ios.unpackBoolean() ;
            final String eprValue = ios.unpackString() ;

            // this should successfully reverse the save process
            final XMLStreamReader reader = SoapUtils.getXMLStreamReader(new StringReader(eprValue)) ;
            StreamHelper.checkNextStartTag(reader, QNAME_TWO_PC_PARTICIPANT) ;
            String eprefText = reader.getElementText();
            StreamSource source = new StreamSource(new StringReader(eprefText));
            final W3CEndpointReference endpointReference = new W3CEndpointReference(source);
            // if we already have a coordinator from a previous recovery scan then reuse it
            // with luck it will have been committed between the last scan and this one
            coordinator = (CoordinatorEngine)CoordinatorProcessorImpl.getProcessor().getCoordinator(id);
            if (coordinator == null) {
                // no entry found so recreate one which is at the prepared stage
                coordinator = new CoordinatorEngine(id, durable, endpointReference, true, State.STATE_PREPARED_SUCCESS) ;
            }
            return true ;
        }
        catch (final Throwable th)
        {
            WSTLogger.arjLoggerI18N.error("com.arjuna.wst11.stub.ParticipantStub_2", th) ;
            return false ;
        }
    }
}
