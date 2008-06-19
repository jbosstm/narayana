package com.arjuna.webservices11.wsat.processors;

import com.arjuna.webservices11.wsat.CoordinatorInboundEvents;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices.SoapFault;

import javax.xml.ws.addressing.AddressingProperties;

import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**
 * The Coordinator processor.
 * @author kevin
 */
public abstract class CoordinatorProcessor
{
    /**
     * The coordinator processor.
     */
    private static CoordinatorProcessor PROCESSOR ;

    /**
     * Get the processor singleton.
     * @return The singleton.
     */
    public static synchronized CoordinatorProcessor getProcessor()
    {
        return PROCESSOR;
    }

    /**
     * Set the processor singleton.
     * @param processor The processor.
     * @return The previous singleton.
     */
    public static synchronized CoordinatorProcessor setProcessor(final CoordinatorProcessor processor)
    {
        final CoordinatorProcessor origProcessor = PROCESSOR;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Activate the coordinator.
     * @param coordinator The coordinator.
     * @param identifier The identifier.
     */
    public abstract void activateCoordinator(final CoordinatorInboundEvents coordinator, final String identifier) ;

    /**
     * Deactivate the coordinator.
     * @param coordinator The coordinator.
     * @param leaveGhost true if a ghost activation entry should be left to indicate that the
     * coordinator exists in a log entry and will be recovered at some later date
     */
    public abstract void deactivateCoordinator(final CoordinatorInboundEvents coordinator, boolean leaveGhost) ;

    /**
     * Aborted.
     * @param aborted The aborted notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void aborted(final Notification aborted, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * Committed.
     * @param committed The committed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void committed(final Notification committed, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * Prepared.
     * @param prepared The prepared notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void prepared(final Notification prepared, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * Read only.
     * @param readOnly The read only notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void readOnly(final Notification readOnly, final AddressingProperties addressingProperties,
        final ArjunaContext arjunaContext) ;

    /**
     * SOAP fault.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final AddressingProperties addressingContext,
        final ArjunaContext arjunaContext) ;
}
