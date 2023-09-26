/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarjtx.processors;

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wst11.BusinessActivityTerminator;
import com.arjuna.webservices.SoapFault;

/**
 * The Terminator Participant processor.
 * @author kevin
 */
public abstract class TerminationCoordinatorProcessor
{
    /**
     * The participant processor.
     */
    private static TerminationCoordinatorProcessor PROCESSOR ;

    /**
     * Get the processor.
     * @return The processor.
     */
    public static TerminationCoordinatorProcessor getProcessor()
    {
        return PROCESSOR ;
    }

    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static TerminationCoordinatorProcessor setProcessor(final TerminationCoordinatorProcessor processor)
    {
        final TerminationCoordinatorProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final BusinessActivityTerminator participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final BusinessActivityTerminator participant) ;

    /**
     * Get the participant with the specified identifier.
     * @param instanceIdentifier The participant identifier.
     * @return The participant or null if not known.
     */
    public abstract BusinessActivityTerminator getParticipant(final InstanceIdentifier instanceIdentifier) ;

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancel(final NotificationType cancel, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Close.
     * @param close The close notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void close(final NotificationType close, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Complete.
     * @param complete The complete notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void complete(final NotificationType complete, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * handle a soap fault sent by the participant.
     * @param soapFault The soap fault
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final MAP map, final ArjunaContext arjunaContext);
}