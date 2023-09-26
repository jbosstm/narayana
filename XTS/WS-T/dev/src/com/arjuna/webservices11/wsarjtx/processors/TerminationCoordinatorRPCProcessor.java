/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarjtx.processors;

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.wst11.BusinessActivityTerminator;
import org.jboss.ws.api.addressing.MAP;

/**
 * The Terminator Participant processor.
 * @author kevin
 */
public abstract class TerminationCoordinatorRPCProcessor
{
    /**
     * The participant processor.
     */
    private static TerminationCoordinatorRPCProcessor PROCESSOR ;

    /**
     * Get the processor.
     * @return The processor.
     */
    public static TerminationCoordinatorRPCProcessor getProcessor()
    {
        return PROCESSOR ;
    }

    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static TerminationCoordinatorRPCProcessor setProcessor(final TerminationCoordinatorRPCProcessor processor)
    {
        final TerminationCoordinatorRPCProcessor origProcessor = PROCESSOR ;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Cancel.
     * @param cancel The cancel notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancel(final NotificationType cancel, final MAP map,
        final ArjunaContext arjunaContext);

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
}