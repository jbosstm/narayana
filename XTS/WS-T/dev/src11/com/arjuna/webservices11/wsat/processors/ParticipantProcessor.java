package com.arjuna.webservices11.wsat.processors;

import com.arjuna.webservices11.wsat.ParticipantInboundEvents;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices.SoapFault;

import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**
 * The Participant processor.
 * @author kevin
 */
public abstract class ParticipantProcessor
{
    /**
     * The participant processor.
     */
    private static ParticipantProcessor PROCESSOR ;

    /**
     * Get the processor.
     * @return The processor.
     */
    public static synchronized ParticipantProcessor getProcessor()
    {
        return PROCESSOR;
    }

    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static synchronized ParticipantProcessor setProcessor(final ParticipantProcessor processor)
    {
        final ParticipantProcessor origProcessor = PROCESSOR;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final ParticipantInboundEvents participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final ParticipantInboundEvents participant) ;

    /**
     * Check whether a participant with the given id is currently active
     * @param identifier The identifier.
     */
    public abstract boolean isActive(final String identifier) ;

    /**
     * Commit.
     * @param commit The commit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void commit(final Notification commit, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Prepare.
     * @param prepare The prepare notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void prepare(final Notification prepare, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void rollback(final Notification rollback, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * SOAP Fault.
     * @param soapFault The SOAP fault notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final MAP map,
        final ArjunaContext arjunaContext) ;
}
