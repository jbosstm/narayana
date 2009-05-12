package com.arjuna.wst11.stub;

import com.arjuna.wst.*;
import com.arjuna.wst.stub.SystemCommunicationException;
import com.arjuna.wst11.messaging.engines.CoordinatorEngine;
import com.arjuna.wst11.messaging.CoordinatorProcessorImpl;
import com.arjuna.webservices11.wsat.State;
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
                throw new SystemCommunicationException() ;
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
            State state = coordinator.getState();
            // participants in the heuristic list may get saved in any state
            if (state == State.STATE_ACTIVE) {
                oos.packInt(0);
            } else if (state == State.STATE_PREPARING) {
                oos.packInt(1);
            } else if (state == State.STATE_PREPARED ||
                        state == State.STATE_PREPARED_SUCCESS) {
                oos.packInt(2);
            } else if (state == State.STATE_ABORTING) {
                oos.packInt(3);
            } else { // COMMITTING or none
                oos.packInt(4);
            }

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
        State state;
        try
        {
            final String id = ios.unpackString() ;
            final boolean durable = ios.unpackBoolean() ;
            final int stateTag = ios.unpackInt();
            switch (stateTag)
            {
                case 0:
                    state = State.STATE_ACTIVE;
                    break;
                case 1:
                    state = State.STATE_PREPARING;
                    break;
                case 2:
                    state = State.STATE_PREPARED_SUCCESS;
                    break;
                case 3:
                    state = State.STATE_ABORTING;
                    break;
                default:
                    state = State.STATE_COMMITTING;
                    break;
            }
            final String eprValue = ios.unpackString() ;

            // this should successfully reverse the save process
            final XMLStreamReader reader = SoapUtils.getXMLStreamReader(new StringReader(eprValue)) ;
            StreamHelper.checkNextStartTag(reader, QNAME_TWO_PC_PARTICIPANT) ;
            String eprefText = reader.getElementText();
            StreamSource source = new StreamSource(new StringReader(eprefText));
            final W3CEndpointReference endpointReference = new W3CEndpointReference(source);
            // if we already have a coordinator from a previous recovery scan or because
            // we had a heuristic outcoe then reuse it with luck it will have been committed
            // or aborted between the last scan and this one
            // note that whatever happens it will not have been removed from the table
            // because it is marked as recovered
            coordinator = (CoordinatorEngine)CoordinatorProcessorImpl.getProcessor().getCoordinator(id);
            if (coordinator == null) {
                // no entry found so recreate one with the saved state
                coordinator = new CoordinatorEngine(id, durable, endpointReference, true, state) ;
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
